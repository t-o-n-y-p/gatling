package dictionary.entities

import com.github.nscala_time.time.Imports.*
import io.gatling.core.Predef.*
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef.*
import io.gatling.http.request.builder.HttpRequestBuilder
import io.gatling.jsonpath.JsonPath

import java.util.UUID

object Actions {

  val searchStartsWith: ChainBuilder =
    exec { session =>
      val word = session("search").as[String]
      val parts = for (n <- Seq(1, 2, 3)) yield word.take(n)
      session.set("parts", parts)
    }
      .foreach("#{parts}", "prefix") {
        exec(
          http("Search with prefix")
            .post(":8080/api/v1/meaning/search")
            .asJson
            .body(StringBody(
              f"""{
                 |\"requestId\":\"${UUID.randomUUID().toString}\",
                 |\"meaningFilter\":{
                 |  \"word\":\"#{prefix}\",
                 |  \"mode\":\"startsWith\",
                 |  \"approved\":true
                 |}
                 |}""".stripMargin))
            .check(status is 200)
            .check(
              jsonPath("$.result").is("success"),
              jsonPath("$.errors").notExists,
              jsonPath("$.meanings").exists
            )
        )
      }

  val loginAndSend: ChainBuilder =
    exec(
      login("User login")
        .formParam("username", attribute("username"))
        .formParam("password", attribute("userpassword"))
        .check(status is 200)
        .check(jsonPath("$.access_token").saveAs("token"))
    )
      .exec(
        http("Send new record").
          post(":8080/api/v1/meaning/create")
          .asJson
          .header("Authorization", "Bearer #{token}")
          .body(StringBody(
            f"""{
               |\"requestId\":\"${UUID.randomUUID().toString}\",
               |\"meaning\":{
               |  \"word\":\"#{word}\",
               |  \"value\":\"#{value}\",
               |  \"proposedBy\":\"#{username}\"
               |}
               |}""".stripMargin))
          .check(status is 200)
          .check(
            jsonPath("$.result").is("success"),
            jsonPath("$.errors").notExists,
            jsonPath("$.meaning.id").exists
          )
      )

  val adminLogin: HttpRequestBuilder =
    login("Admin login")
      .formParam("username", attribute("adminname"))
      .formParam("password", attribute("adminpassword"))
      .check(status is 200)
      .check(jsonPath("$.access_token").saveAs("token"))

  val adminSearch: ChainBuilder =
    exec(
      http("Admin search")
        .post(":8080/api/v1/meaning/search")
        .asJson
        .body(StringBody(
          f"""{
             |\"requestId\":\"${UUID.randomUUID().toString}\",
             |\"meaningFilter\":{
             |  \"approved\":false
             |}
             |}""".stripMargin))
        .check(status is 200)
        .check(
          jsonPath("$.result").is("success"),
          jsonPath("$.errors").notExists
        )
        .check(
          jmesPath("meanings[?starts_with(word,'#{filter}')==`true`].id")
            .ofType[Seq[Any]]
            .withDefault(Seq.empty)
            .saveAs("ids"),
          jmesPath("meanings[?starts_with(word,'#{filter}')==`true`].version")
            .ofType[Seq[Any]]
            .withDefault(Seq.empty)
            .saveAs("versions")
        )
    )
      .exec { session =>
        val count = session("ids").as[Seq[Any]].size
        session.set(
          "interval",
          if count < 2 then 0 else (Scenarios.dictionaryAdminInterval.toSeconds - count) / count
        )
      }

  val adminActions: ChainBuilder =
    asLongAs(session => session("ids").as[Seq[Any]].nonEmpty) {
      exec { session =>
        val id = session("ids").as[Seq[Any]].head
        val version = session("versions").as[Seq[Any]].head
        session.setAll(("id", id), ("version", version))
      }
        .randomSwitch(
          50.0 -> update(),
          50.0 -> delete()
        )
        .exec { session =>
          val ids = session("ids").as[Seq[Any]]
          val versions = session("versions").as[Seq[Any]]
          session.setAll(("ids", ids.drop(1)), ("versions", versions.drop(1)))
        }
        .pause("#{interval}")
    }

  private def update(): ChainBuilder =
    exec(
      http("Approve record")
        .post(":8080/api/v1/meaning/update")
        .asJson
        .header("Authorization", "Bearer #{token}")
        .body(StringBody(
          f"""{
             |\"requestId\":\"${UUID.randomUUID().toString}\",
             |\"meaning\":{
             |  \"id\":\"#{id}\",
             |  \"version\":\"#{version}\",
             |  \"approved\":true
             |}
             |}""".stripMargin))
        .check(status is 200)
        .check(
          jsonPath("$.result").is("success"),
          jsonPath("$.errors").notExists
        )
    )

  private def delete(): ChainBuilder =
    exec(
      http("Decline record")
        .post(":8080/api/v1/meaning/delete")
        .asJson
        .header("Authorization", "Bearer #{token}")
        .body(StringBody(
          f"""{
             |\"requestId\":\"${UUID.randomUUID().toString}\",
             |\"meaning\":{
             |  \"id\":\"#{id}\",
             |  \"version\":\"#{version}\"
             |}
             |}""".stripMargin))
        .check(status is 200)
        .check(
          jsonPath("$.result").is("success"),
          jsonPath("$.errors").notExists
        )
    )

  private def login(message: String): HttpRequestBuilder =
    http(message)
      .post(":8081/auth/realms/dictionary-meanings/protocol/openid-connect/token")
      .asFormUrlEncoded
      .formParam("client_id", string("dictionary-meanings-service"))
      .formParam("grant_type", string("password"))

  private def string(s: String): Session => String =
    (session: Session) => s

  private def attribute(s: String): Session => String =
    (session: Session) => session(s).as[String]

}
