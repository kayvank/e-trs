package kcl

import com.amazonaws.services.kinesis.AmazonKinesis
import com.typesafe.scalalogging.LazyLogging
import scalaz.concurrent.Task
import utils.CustomExecutor._

object KinesisPublisher extends LazyLogging {
  import KplBatchProtocol._
  import KplModle._
  final val kinesisBatchLimit = 500 - 2

  def publish(records: Vector[KplRecord]):
      AmazonKinesis => Task[PublishResult] = client =>
  for {
    t1 <- Task.gatherUnordered(
      records.grouped(kinesisBatchLimit).toList.map(x =>
        Task(client.putRecords(x.asPutRecordSRequest))))
    t2 <- Task(t1.map(r => r.getFailedRecordCount).foldLeft(0)(_ + _))
  } yield (
    PublishResult(t2, records.size)
  )
  
  def publish(record: KplRecord): AmazonKinesis => Task[Int] =
    client => for {
      t1 <- Task(client.putRecord(record.asPutRecordRequest) )
      _ <- Task(logger.info(s"publishing telemetry =: ${record}"))
      _ <- Task(logger.info(s"publishing telemetry to kinesis.sequenceNumber=: ${t1.getSequenceNumber}"))
      t2 <- Task(1)
    } yield(t2)

}
