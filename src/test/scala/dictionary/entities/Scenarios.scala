package dictionary.entities

import io.gatling.core.Predef.*
import io.gatling.http.Predef.*
import io.gatling.core.structure.ScenarioBuilder
import scala.concurrent.duration._

object Scenarios {

  val dictionaryInterval: FiniteDuration = 3.seconds

  val dictionary: ScenarioBuilder =
    scenario("dictionary")
      .forever(
        pace(dictionaryInterval)
          .feed(Feeders.search)
          .exec { session =>
            val word = session("search").as[String]
            val parts = for (n <- Seq(1, 2, 3)) yield word.take(n)
            val newSession = session.set("parts", parts)
            newSession
          }
          .exec(Actions.searchStartsWith)
          .exec(flushSessionCookies)
          .exec(flushHttpCache)
      )

}
