package model

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import io.circe._
import parser._
import syntax._
import Decoder._
import generic.auto._
import optics.JsonPath._
import java.nio.ByteBuffer
import com.typesafe.scalalogging.LazyLogging
import com.amazonaws.services.kinesis.model.{PutRecordsRequest, PutRecordsRequestEntry, Record}
import scalaz._, Scalaz._
import scalaz.concurrent.Task
import kcl.KplModle._

object DomainProtocol extends LazyLogging {

  import utils.EitherToDisjunction._
  import utils.ApplicativeTask._
  import utils.EitherToTask._

  implicit class KclStringProtocol(event: Record) {
    def asString: \/[Exception, String] =
      try {
        \/-(new java.lang.String((event.getData.array()), "utf-8"))
      } catch {
        case e: Exception => -\/(e)
      }
  }

  implicit class EndoJsonProtocol(event: String) {
    def asJsonEndo: \/[Exception, Json] = 
      parse(event) match {
        case Left(e) => -\/(KclToTelemetryException(e.getMessage))
        case Right(e) => \/-(e)
    }
  }
  val telemetryOps: Json => \/[Exception, Json] = j =>
  root.event_data.custom.json.getOption(j) match {
    case Some(json) => \/-(json)
    case None => -\/(KclToTelemetryException("No endo.custom reporting data found"))
  }

    implicit class EndoProtocol(event: Json) {
    def asEndo: \/[Exception, Endo] = 
        event.as[Endo] match {
        case Right(e) => \/-(e)
        case Left(e) => -\/(KclToTelemetryException(e.getMessage))
    }
  }

  implicit class EndoTelemetryToByteBuf(event: EndoTelemetry) {
    def asKplRecord: KplRecord = 
      KplRecord(
        data = ByteBuffer.wrap(event.asJson.noSpaces.getBytes))
  }

  implicit class EndoTelemetryKclProtocol(event: Record) {
    def asEndoTelemetryRecord: Task[KplRecord] = for {
        e1 <- event.asString
        e2 <- e1.asJsonEndo
        e0 <- e2.asET
      } yield(e0.asKplRecord)
  }

  implicit class ET(event: Json) {
    def asET: \/[Exception, EndoTelemetry] = (
        event.asEndo |@| telemetryOps(event)) {
        EndoTelemetry(_, _) }
  }
}
