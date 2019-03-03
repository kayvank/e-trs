package kcl

import java.util.concurrent.atomic.AtomicLong
import utils.Global._
import com.amazonaws.services.kinesis.clientlibrary._
import exceptions.KinesisClientLibRetryableException
import interfaces.IRecordProcessorCheckpointer
import scalaz._, Scalaz._
import scala.util.{Failure, Success, Try}
import scalaz.concurrent.Task

trait CheckPoint {
  val cp: IRecordProcessorCheckpointer => Task[Long]

}
object CheckPointFactory {
  def apply(): CheckPoint = new KclCheckPoint(
    maxCpRetries = 7,
    cpCheckpointIntervalMillis=
    cfgVevo.getInt("kinesis.streams.endo.time.minutes.checkpoints") * 60 * 1000
  )
}

class KclCheckPoint(
  maxCpRetries: Int,
  cpCheckpointIntervalMillis: Int) extends CheckPoint {
  val nextCheckPoint: AtomicLong =
    new AtomicLong(System.currentTimeMillis + (cpCheckpointIntervalMillis))

  val checkPoint: IRecordProcessorCheckpointer => \/[Throwable, Long] = checkpointer => {

    def checkPoint(checkpointer: IRecordProcessorCheckpointer,
      iteration: Int): \/[Throwable, Long] = {
      Try(checkpointer.checkpoint()) match {
        case Failure(e) if (e.isInstanceOf[KinesisClientLibRetryableException] && iteration < maxCpRetries) =>
          checkPoint(checkpointer, iteration + 1)
        case Failure(e) => -\/(e)
        case Success(()) =>
          nextCheckPoint.set(System.currentTimeMillis + (cpCheckpointIntervalMillis)) //TODO bad side effect. will clean up
          \/-(System.currentTimeMillis)
      }
    }

    if (nextCheckPoint.get < System.currentTimeMillis)
      checkPoint(checkpointer, 0)
    else nextCheckPoint.get.right
  }

  val cp: IRecordProcessorCheckpointer => Task[Long] = checkpointer => {
    checkPoint(checkpointer) match {
      case \/-(s) => Task(s)
      case -\/(e) => Task.fail(e)
    }
  }
}
