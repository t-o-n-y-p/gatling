package gatling

import gatling.entities.Actions._
import gatling.entities.Assertions.responseTimePercentile90
import gatling.entities.{Protocols, Scenarios}
import io.gatling.core.Predef.*
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration.*

class ReliabilitySimulation extends Simulation {

  setUp(
    Scenarios.webTours.inject(
      rampUsers(56).during(Scenarios.webToursInterval),
      nothingFor(1.hour)
    ),
  )
    .maxDuration(1.hour + Scenarios.webToursInterval)
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
