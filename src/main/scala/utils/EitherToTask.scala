package utils

import scalaz._, Scalaz._
import scalaz.concurrent.Task
import scalaz.{-\/, \/-}


object EitherToTask {
  implicit def asTask[A](e: \/[Exception, A]): Task[A] =
    e match {
      case \/-(a) => Task(a)
      case -\/(ex) => Task.fail(ex)
    }
}

