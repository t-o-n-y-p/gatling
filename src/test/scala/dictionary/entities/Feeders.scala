package dictionary.entities

import io.gatling.core.Predef.*
import io.gatling.core.feeder.BatchableFeederBuilder

object Feeders {

  val search: BatchableFeederBuilder[String] = csv("search.csv").random
  val users: BatchableFeederBuilder[String] = csv("users.csv").random
  val words: BatchableFeederBuilder[String] = csv("words.csv").queue
  val admins: BatchableFeederBuilder[String] = csv("admins.csv").random
  val filters: BatchableFeederBuilder[String] = csv("filters.csv").queue

  val feedSessionWithSearch: Session => Session =
    (session: Session) => {
      val word = session("search").as[String]
      val parts = for (n <- Seq(1, 2, 3)) yield word.take(n)
      session.set("parts", parts)
    }

}
