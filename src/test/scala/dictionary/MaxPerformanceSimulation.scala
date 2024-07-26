package dictionary

import dictionary.entities.Actions._
import dictionary.entities.{Protocols, Scenarios}
import io.gatling.core.Predef.*
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration.*

class MaxPerformanceSimulation extends Simulation {

  private val levelLength = 10.minutes
  private val levels = 15

  setUp(
    Scenarios.dictionary.inject(
      rampConcurrentUsers(0)
        .to(200)
        .during(Scenarios.dictionaryInterval),
      incrementConcurrentUsers(40)
        .times(levels)
        .eachLevelLasting(levelLength)
        .separatedByRampsLasting(Scenarios.dictionaryInterval)
        .startingFrom(200)
    ),
    Scenarios.dictionaryAdmins.inject(
      rampUsers(34).during(Scenarios.dictionaryAdminInterval),
      nothingFor((levelLength + Scenarios.dictionaryInterval) * levels - Scenarios.dictionaryAdminInterval)
    ),
  )
    .maxDuration((levelLength + Scenarios.dictionaryInterval) * levels)
    .protocols(Protocols.dictionary)

}
