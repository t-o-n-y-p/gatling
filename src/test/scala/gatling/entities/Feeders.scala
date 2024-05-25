package gatling.entities

import io.gatling.core.Predef.*
import io.gatling.core.feeder.BatchableFeederBuilder

object Feeders {

  val users: BatchableFeederBuilder[String] = csv("users.csv").random

}
