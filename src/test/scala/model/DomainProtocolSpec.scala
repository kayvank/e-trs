package model

import org.specs2.mutable.Specification
import io.circe._
import syntax._
import parser._
import Decoder._
import generic.auto._
import optics.JsonPath._
import com.typesafe.scalalogging.LazyLogging
import scala.io.Source
import DomainProtocol._
import cats.syntax.either._

class DomainProtocolSpec extends   Specification with LazyLogging {
  "Domain Protocols Specification".title

  val endoString =
    Source.fromInputStream(getClass.getResourceAsStream("/ec-telemetry-endo.json")).mkString

  "Transform String to Endo-Json" >> {
    val endoJson = parse(endoString).toOption.get
    val computed = endoString.asJsonEndo.toOption
    computed.isDefined 
  }


  "Transform Endo-json to Telemetry object" >> {
    val endoJson = parse(endoString).toOption.get
    val computed = endoJson.asEndo
    logger.info(s"computed endo-from-json= ${computed}")
    computed.toOption.isDefined
  }

  "Transform Endo-json to EndoTelemetry object" >> {
    val endoJson = parse(endoString).toOption.get
    val computed = endoJson.asET
    logger.info(s"computed telmetry= ${computed}")
    computed.toOption.isDefined
  }

}
