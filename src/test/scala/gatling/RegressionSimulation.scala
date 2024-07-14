package gatling

import gatling.entities.Actions._
import gatling.entities.Assertions.responseTimePercentile90
import gatling.entities.{Protocols, Scenarios}
import io.gatling.core.Predef.*
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration.*

class RegressionSimulation extends Simulation {

  setUp(
    Scenarios.webToursRegression.inject(
      // 150%
      rampUsers(45).during(Scenarios.webToursRegressionInterval),
      nothingFor(30.minutes)
    ),
  )
    .maxDuration(30.minutes + Scenarios.webToursRegressionInterval)
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
