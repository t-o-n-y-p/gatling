package dictionary.entities

import io.gatling.core.Predef.*
import io.gatling.core.feeder.BatchableFeederBuilder

object Feeders {

  val search: BatchableFeederBuilder[String] = csv("search.csv").random
  /*
  val users: BatchableFeederBuilder[String] = csv("users.csv").random
  val admins: BatchableFeederBuilder[String] = csv("admins.csv").random
  val words: BatchableFeederBuilder[String] = csv("words.csv").circular
   */

}
