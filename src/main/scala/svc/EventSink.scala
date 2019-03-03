package svc

import scala.util._
import com.typesafe.scalalogging.LazyLogging
import scalaz._, Scalaz._
import scalaz.concurrent.Task
import model._
import kcl._
import kcl.Kcl._
import KplModle._
import KinesisPublisher._
import com.amazonaws.services.kinesis.model.{PutRecordsRequest, PutRecordsRequestEntry, Record}
import com.amazonaws.services.kinesis.clientlibrary.types._
import scala.collection.JavaConversions._
import io.circe._

trait EventSink {
  val sink: ProcessRecordsInput => Task[Int]
}
class EndoEventSink extends EventSink with LazyLogging{
  import DomainProtocol._
  val sink: ProcessRecordsInput  => Task[Int] = event => 
    Task.fork( for {
      t1 <- Task.gatherUnordered(event.getRecords.toList.map(r => publishTask(r)) )
      t2 <- Task(t1.filter( _ != 0).size )
  } yield(t2))

  def publishTask(e: Record): Task[Int] = (
    Task.fork( for {
      t1 <- e.asEndoTelemetryRecord
      t2 <- KinesisPublisher.publish(t1)(client)
    } yield(t2)) ).handleWith {
    case e: com.amazonaws.services.kinesis.model.AmazonKinesisException =>
      logger.error(s"Kcl-related exetption: ${e.getMessage}")
      Task(0)
    case e: ParsingFailure =>
      logger.error(s"ParsingFailure in parsing the Json record from kinesis Stream. Exception: ${e.getMessage}")
      Task(0)
    case e: DecodingFailure => 
      Task(0)
    case e: KclToTelemetryException => 
      logger.debug(s"KclToTelemetryException: ${e.getMessage}")
      Task(0)
    case e: Exception =>
      logger.error(s"Unexpected Exception: ${e.getMessage}")
      Task(0)
  }

}

object EventSinkFactory { 
  def apply(): EventSink = {
    new EndoEventSink
  }
}

