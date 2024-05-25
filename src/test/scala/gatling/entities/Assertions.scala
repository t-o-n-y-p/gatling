package gatling.entities

import io.gatling.core.Predef._
import io.gatling.core.assertion.AssertionSupport

object Assertions {
  
  def responseTimePercentile90(groupName: String): Assertion =
    details(groupName).responseTime.percentile(90.0).lte(3000)
  
}
