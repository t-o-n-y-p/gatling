package dictionary

import dictionary.entities.Actions._
import dictionary.entities.{Protocols, Scenarios}
import io.gatling.core.Predef.*
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration.*

class ReliabilitySimulation extends Simulation {

  setUp(
    Scenarios.dictionary.inject(
      rampUsers(1).during(Scenarios.dictionaryInterval),
      nothingFor(5.minutes)
    ),
    Scenarios.dictionaryAdmins.inject(
      rampUsers(34).during(Scenarios.dictionaryInterval),
      nothingFor(5.minutes)
    ),
  )
    .maxDuration(5.minutes + Scenarios.dictionaryInterval)
    .protocols(Protocols.dictionary)

}
