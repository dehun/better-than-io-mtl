package space.dehun

import cats._
import cats.effect._
import cats.implicits._

class Logger {
  def log(msg:String):Unit = Console.println(s"log: ${msg}")
}

case class LogT[F[_]:Monad, A](runLog:Logger => F[A])
object LogT {
  implicit def logTMonadInstance[F[_]:Monad] = new Monad[LogT[F, ?]] {
    override def pure[A](x: A): LogT[F, A] =
      LogT(log => Monad[F].pure(x))
    override def flatMap[A, B](fa: LogT[F, A])(fx: A => LogT[F, B]): LogT[F, B] =
      LogT(log => fa.runLog(log).flatMap(x => fx(x).runLog(log)))
    override def tailRecM[A, B](a: A)(fx: A => LogT[F, Either[A, B]]): LogT[F, B] =
      LogT(log => Monad[F].tailRecM(a)(x => fx(x).runLog(log)))
  }
}


trait MonadLog[F[_]] {
  def log(msg:String):F[Unit]
}


