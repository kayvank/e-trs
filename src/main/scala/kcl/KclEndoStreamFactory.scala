package kcl

import com.amazonaws.services.kinesis.clientlibrary._
import interfaces.IRecordProcessorCheckpointer
import interfaces.v2.{IRecordProcessor, IRecordProcessorFactory}
import lib.worker.ShutdownReason
import types.{InitializationInput, ProcessRecordsInput, ShutdownInput}
import com.typesafe.scalalogging.LazyLogging
import scalaz.concurrent.Task
import model._

class KclEndoStreamFactory(
  eventSink: ProcessRecordsInput => Task[Int],
  checkpoint: IRecordProcessorCheckpointer => Task[Long])
  extends IRecordProcessorFactory {

  @Override
  def createProcessor: IRecordProcessor = {
    new KclProcessor(eventSink, checkpoint)
  }
}
