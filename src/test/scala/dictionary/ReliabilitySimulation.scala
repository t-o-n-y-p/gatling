package dictionary

import dictionary.entities.Actions._
import dictionary.entities.{Protocols, Scenarios}
import io.gatling.core.Predef.*
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration.*

class ReliabilitySimulation extends Simulation {

  private val length = 120.minutes

  setUp(
    Scenarios.dictionary.inject(
      rampUsers(400).during(Scenarios.dictionaryInterval),
      nothingFor(length)
    ),
    Scenarios.dictionaryAdmins.inject(
      rampUsers(34).during(Scenarios.dictionaryAdminInterval),
      nothingFor(length - Scenarios.dictionaryAdminInterval + Scenarios.dictionaryInterval)
    ),
  )
    .maxDuration(length + Scenarios.dictionaryInterval)
    .protocols(Protocols.dictionary)

}
