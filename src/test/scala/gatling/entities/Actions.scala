package gatling.entities

import com.github.nscala_time.time.Imports._
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder

object Actions {

  val OpenMainPage = "Open main page"
  val Login = "Login"
  val NavToFlights = "Nav to flights"
  val FindFlights = "Find flights"
  val ReservedFlights = "Reserve flight"
  val BuyFlight = "Buy flight"

  val openWebTours: ChainBuilder =
    group(OpenMainPage) {
      exec(
        http("/webtours/")
          .get("/webtours/")
          .check(status is 200)
      )
        .exec(
          http("/webtours/header.html")
            .get("/webtours/header.html")
            .check(status is 200)
        )
        .exec(
          http("/cgi-bin/welcome.pl")
            .get("/cgi-bin/welcome.pl")
            .queryParam("signOff", string("true"))
            .check(status is 200)
        )
        .exec(
          http("/cgi-bin/nav.pl")
            .get("/cgi-bin/nav.pl")
            .queryParam("in", string("home"))
            .check(status is 200)
            .check(regex("userSession\" value=\"(.*?)\"").saveAs("token"))
        )
        .exec(
          http("/WebTours/home.html")
            .get("/WebTours/home.html")
            .check(status in Seq(200, 304))
        )
    }

  val login: ChainBuilder =
    group(Login) {
      exec(
        http("post /cgi-bin/login.pl")
          .post("/cgi-bin/login.pl")
          .formParam("userSession", attribute("token"))
          .formParam("username", attribute("login"))
          .formParam("password", attribute("password"))
          .formParam("login.x", string("58"))
          .formParam("login.y", string("11"))
          .formParam("JSFormSubmit", string("off"))
          .check(status is 200)
      )
        .exec(
          http("/cgi-bin/nav.pl")
            .get("/cgi-bin/nav.pl")
            .queryParam("page", string("menu"))
            .queryParam("in", string("home"))
            .check(status is 200)
        )
        .exec(
          http("get /cgi-bin/login.pl")
            .get("/cgi-bin/login.pl")
            .queryParam("intro", string("true"))
            .check(status is 200)
        )
    }

  val navToFlights: ChainBuilder =
    group(NavToFlights) {
      exec(
        http("/cgi-bin/welcome.pl")
          .get("/cgi-bin/welcome.pl")
          .queryParam("page", string("search"))
          .check(status is 200)
      )
        .exec(
          http("/cgi-bin/nav.pl")
            .get("/cgi-bin/nav.pl")
            .queryParam("page", string("menu"))
            .queryParam("in", string("flights"))
            .check(status is 200)
        )
        .exec(
          http("/cgi-bin/reservations.pl")
            .get("/cgi-bin/reservations.pl")
            .queryParam("page", string("welcome"))
            .check(status is 200)
            .check(
              css("[name='depart'] > option")
                .findRandom.saveAs("departCity"))
            .check(
              css("[name='arrive'] > option")
                .findRandom.saveAs("arriveCity"))
        )
    }

  val findFlights: HttpRequestBuilder =
    http(FindFlights)
      .post("/cgi-bin/reservations.pl")
      .formParam("advanceDiscount", string("0"))
      .formParam("depart", attribute("departCity"))
      .formParam("departDate", date(DateTime.now() + 2.months))
      .formParam("arrive", attribute("arriveCity"))
      .formParam("returnDate", date(DateTime.now() + 2.months + 1.day))
      .formParam("numPassengers", string("1"))
      .formParam("seatPref", string("None"))
      .formParam("seatType", string("Coach"))
      .formParam("findFlights.x", string("53"))
      .formParam("findFlights.y", string("11"))
      .multivaluedFormParam(".cgifields", string(Seq("roundtrip", "seatType", "seatPref")))
      .check(status is 200)
      .check(regex("name=\"outboundFlight\" value=\"(.*?)\"").findRandom.saveAs("outboundFlight"))

  val reserveFlight: HttpRequestBuilder =
    http(ReservedFlights)
      .post("/cgi-bin/reservations.pl")
      .formParam("outboundFlight", attribute("outboundFlight"))
      .formParam("numPassengers", string("1"))
      .formParam("advanceDiscount", string("0"))
      .formParam("seatType", string("Coach"))
      .formParam("seatPref", string("None"))
      .formParam("reserveFlights.x", string("48"))
      .formParam("reserveFlights.y", string("12"))
      .check(status is 200)

  val buyFlight: HttpRequestBuilder =
    http(BuyFlight)
      .post("/cgi-bin/reservations.pl")
      .formParam("firstName", string("Tony"))
      .formParam("lastName", string("P"))
      .formParam("address1", string("Street"))
      .formParam("address2", string("Address"))
      .formParam("pass1", string("Tony P"))
      .formParam("creditCard", string("123"))
      .formParam("expDate", string("01/99"))
      .formParam("oldCCOption", empty())
      .formParam("outboundFlight", attribute("outboundFlight"))
      .formParam("returnFlight", empty())
      .formParam("numPassengers", string("1"))
      .formParam("advanceDiscount", string("0"))
      .formParam("seatType", string("Coach"))
      .formParam("seatPref", string("None"))
      .formParam("JSFormSubmit", string("off"))
      .formParam("buyFlights.x", string("66"))
      .formParam("buyFlights.y", string("12"))
      .multivaluedFormParam(".cgifields", string(Seq("saveCC")))
      .check(status is 200)

  private def empty(): Session => String = string("")

  private def string(s: String): Session => String =
    (session: Session) => s

  private def string(s: Seq[String]): Session => Seq[String] =
    (session: Session) => s

  private def attribute(s: String): Session => String =
    (session: Session) => session(s).as[String]

  private def date(date: DateTime): Session => String =
    (session: Session) => f"${date.month.get()}%02d/${date.day.get()}%02d/${date.year.get()}%04d"
}
