package space.dehun

import cats.Monad
import cats._
import cats.implicits._
import cats.effect._

class Net {
  def notifyUserChange(user:DB.User):IO[Unit] = IO {
    Console.println(s"net:notifying user change ${user}")
  }
}

case class NetT[F[_]:Monad, A](runNet:Net => F[A])

object NetT {
  implicit def netTMonadInstance[F[_]:Monad] = new Monad[NetT[F, ?]] {
    override def pure[A](x: A): NetT[F, A] =
      NetT(net => Monad[F].pure(x))
    override def flatMap[A, B](fa: NetT[F, A])(fx: A => NetT[F, B]): NetT[F, B] =
      NetT(net => fa.runNet(net).flatMap(x => fx(x).runNet(net)))
    override def tailRecM[A, B](a: A)(fx: A => NetT[F, Either[A, B]]): NetT[F, B] =
      NetT(net => Monad[F].tailRecM(a)(x => fx(x).runNet(net)))
  }
}

trait MonadNet[F[_]] {
  def notifyUserChange(user:DB.User):F[Unit]
}
