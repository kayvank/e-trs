import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker
import com.typesafe.scalalogging.LazyLogging
import org.http4s.server.{Server, ServerApp}
import org.http4s.server.blaze.BlazeBuilder
import scalaz._, Scalaz._
import utils._
import Global._
import scalaz.concurrent.Task
import kcl._
import svc._
import api.StatusApi

object Bootstrap extends ServerApp with LazyLogging {

  case class ProgramStatus(s: Server, u: Unit)

  val awsKclworker = new Worker.Builder()
    .recordProcessorFactory(new KclEndoStreamFactory(
       EventSinkFactory.apply().sink,
       CheckPointFactory.apply().cp))
    .config(Kcl.endoKclClient).build

  lazy val runWorkerTask = awsKclworker.run()

  def server(args: List[String]): Task[Server] = {

    import CustomExecutor._
    import ApplicativeTask._

    val serverTask = BlazeBuilder.bindHttp(
      port = cfgVevo.getInt("http.port"),
      host = "0.0.0.0")
      .mountService(StatusApi.service, "/status").start

    T.apply2(
      Task.fork(serverTask)(ec),
      Task.fork(Task.delay(runWorkerTask))(customExecutor))(ProgramStatus(_, _)
    ) map (_.s)
  }
}
