package dictionary.entities

import com.github.nscala_time.time.Imports.*
import io.gatling.core.Predef.*
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef.*
import io.gatling.http.request.builder.HttpRequestBuilder

import java.util.UUID

object Actions {

  val searchStartsWith: ChainBuilder =
    foreach("#{parts}", "prefix") {
      exec(
        http("Search with prefix")
          .post("/search")
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

}
