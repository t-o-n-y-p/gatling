package gatling.entities

import gatling.entities.Feeders
import io.gatling.core.Predef.*
import io.gatling.http.Predef.*
import io.gatling.core.structure.ScenarioBuilder
import scala.concurrent.duration._

object Scenarios {

  val webToursInterval: FiniteDuration = 22.seconds
  val webToursRegressionInterval: FiniteDuration = 35.seconds

  val webTours: ScenarioBuilder =
    scenario("webTours")
      .forever(
        pace(webToursInterval)
          .feed(Feeders.users)
          .exec(Actions.openWebTours)
          .exec(Actions.login)
          .exec(Actions.navToFlights)
          .exec(Actions.findFlights)
          .exec(Actions.reserveFlight)
          .exec(Actions.buyFlight)
          .exec(Actions.openWebTours)
          .exec(flushSessionCookies)
          .exec(flushHttpCache)
      )

  val webToursRegression: ScenarioBuilder =
    scenario("webToursRegression")
      .forever(
        pace(webToursRegressionInterval)
          .feed(Feeders.users)
          .exec(Actions.openWebTours)
          .exec(Actions.login)
          .exec(Actions.navToFlights)
          .exec(Actions.findFlights)
          .exec(Actions.reserveFlight)
          .exec(Actions.buyFlight)
          .exec(Actions.openWebTours)
          .exec(Actions.navToFlights)
          .exec(Actions.findFlights)
          .exec(Actions.reserveFlight)
          .exec(Actions.buyFlight)
          .exec(flushSessionCookies)
          .exec(flushHttpCache)
      )

}
