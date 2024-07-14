package gatling

import gatling.entities.Actions._
import gatling.entities.Assertions._
import gatling.entities.{Assertions, Protocols, Scenarios}
import io.gatling.core.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration._

class MaxPerformanceSimulation extends Simulation {

  setUp(
    Scenarios.webToursRegression.inject(
      incrementConcurrentUsers(3)
        .times(15)
        .eachLevelLasting(2.minutes)
        .separatedByRampsLasting(Scenarios.webToursRegressionInterval)
    ),
  )
    .maxDuration(30.minutes + Scenarios.webToursRegressionInterval * 15)
    .assertions(
      responseTimePercentile90(OpenMainPage),
      responseTimePercentile90(Login),
      responseTimePercentile90(NavToFlights),
      responseTimePercentile90(FindFlights),
      responseTimePercentile90(ReservedFlights),
      responseTimePercentile90(BuyFlight)
    )
    .protocols(Protocols.webTours)
}
