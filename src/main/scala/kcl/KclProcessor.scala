package kcl

import com.amazonaws.services.kinesis.clientlibrary
import clientlibrary.exceptions._
import clientlibrary.interfaces.IRecordProcessorCheckpointer
import clientlibrary.interfaces.v2.IRecordProcessor
import clientlibrary.lib.worker.ShutdownReason
import clientlibrary.types._
import com.typesafe.scalalogging.LazyLogging
import io.circe.DecodingFailure
import utils._
import model._
import scalaz.concurrent.Task

class KclProcessor(
  eventSink: ProcessRecordsInput => Task[Int], 
  checkpoint: IRecordProcessorCheckpointer => Task[Long]
) extends IRecordProcessor
  with LazyLogging {

  import Kcl._

  override def shutdown(shutdownInput: ShutdownInput) = {
    logger.info(s"Ending kcl stream processing!")
    shutdownInput.getShutdownReason match {
      case ShutdownReason.TERMINATE =>
        checkpoint(shutdownInput.getCheckpointer) // dont wait, do checkpoint now
        logger.warn(s"Received Shard:  ${ShutdownReason.TERMINATE} for stream= $endoStreamName}")
      case ShutdownReason.ZOMBIE =>
        logger.warn(s"Received Shard: ${ShutdownReason.ZOMBIE} for stream= $endoStreamName}   ")
      case ShutdownReason.REQUESTED =>
        logger.warn(s"received request for shutdown! for stream= $endoStreamName")
    }
  }

  override def initialize(initializationInput: InitializationInput): Unit = {
    logger.debug(s"kcl stream processing begins")
  }

  override def processRecords(processRecordsInput: ProcessRecordsInput): Unit = {

    import CustomExecutor._

    val recordsProcessedTask: Task[Int] =
      Task.fork(eventSink(processRecordsInput))(customExecutor).handleWith {
        case e: KinesisClientLibRetryableException =>
          logger.warn(s"KinesisClientLibRetryableException.  ${e.getMessage}")
          Task.now(0)
        case e: KinesisClientLibNonRetryableException =>
          logger.error(s"KinesisClientLibNonRetryableException.  ${e.getMessage}")
          Task.now(0)
        case e: Throwable =>
          logger.error(s" ${e}")
          checkpoint(processRecordsInput.getCheckpointer)
          Task.now(0)
      }
    val p = recordsProcessedTask.unsafePerformSync
    logger.info(s"program results(kcl)= ${p}")
  }
}
