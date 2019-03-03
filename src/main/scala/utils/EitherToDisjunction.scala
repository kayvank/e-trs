package utils

import scala.util.Either
import scalaz._, Scalaz._
import scalaz.concurrent.Task
import scalaz.{-\/, \/-}

object EitherToDisjunction {
  implicit def fromEither2zDisjunction[A, B](_either: Either[A, B]) =
    _either match {
      case Right(b) => \/-(b)
      case Left(a) => -\/(a)
    }
}
