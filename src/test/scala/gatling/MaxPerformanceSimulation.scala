package gatling

import gatling.entities.Actions._
import gatling.entities.Assertions._
import gatling.entities.{Assertions, Protocols, Scenarios}
import io.gatling.core.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration._

class MaxPerformanceSimulation extends Simulation {

  setUp(
    Scenarios.webTours.inject(
      incrementConcurrentUsers(7)
        .times(10)
        .eachLevelLasting(2.minutes)
        .separatedByRampsLasting(Scenarios.webToursInterval)
    ),
  )
    .maxDuration(20.minutes + Scenarios.webToursInterval * 10)
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
