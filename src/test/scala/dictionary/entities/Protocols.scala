package dictionary.entities

import io.gatling.core.Predef._
import io.gatling.http.Predef.http
import io.gatling.http.protocol.HttpProtocolBuilder

object Protocols {

  val dictionary: HttpProtocolBuilder =
    http.baseUrl("http://192.168.0.101:8080/api/v1/meaning")
      .contentTypeHeader("application/json")
      .shareConnections

}
