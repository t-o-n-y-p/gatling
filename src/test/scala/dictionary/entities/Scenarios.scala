package dictionary.entities

import io.gatling.core.Predef.*
import io.gatling.http.Predef.*
import io.gatling.core.structure.ScenarioBuilder
import scala.concurrent.duration._

object Scenarios {

  val dictionaryInterval: FiniteDuration = 12.seconds
  val dictionaryAdminInterval: FiniteDuration = dictionaryInterval * 10

  val dictionary: ScenarioBuilder =
    scenario("dictionary")
      .forever(
        pace(dictionaryInterval)
          .feed(Feeders.search)
          .exec(Actions.searchStartsWith)
          .randomSwitch(
            90.0 -> exec(Actions.searchStartsWith),
            10.0 -> feed(Feeders.users).feed(Feeders.words).exec(Actions.loginAndSend)
          )
          .exec(flushSessionCookies)
          .exec(flushHttpCache)
      )

  val dictionaryAdmins: ScenarioBuilder =
    scenario("dictionary admins")
      .feed(Feeders.admins)
      .feed(Feeders.filters)
      .exec(Actions.adminLogin)
      .forever(
        pace(dictionaryAdminInterval)
          .exec(Actions.adminSearch)
          .exec(Actions.adminActions)
          .exec(flushSessionCookies)
          .exec(flushHttpCache)
      )

}
