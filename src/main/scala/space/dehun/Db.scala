package space.dehun

import cats._
import cats.implicits._
import cats.effect._
import space.dehun.DB.User

object DB {
  case class User(userId:String, nickName:String, age:Int)
}

class DB {
  import DB._
  def queryUser(userId:String):IO[User] = IO {
    Console.println(s"db:querying user ${userId}")
    User("123", "mr. abc", 32)
  }

  def storeUser(newUser:User):IO[Unit] = IO {
    Console.println(s"db:storing user ${newUser}")
  }
}

case class DbT[F[_]:Monad, A](runDb:DB => F[A])
object DbT {
  implicit def dbTMonadInstance[F[_]:Monad] = new Monad[DbT[F, ?]] {
    override def pure[A](x: A): DbT[F, A] =
      DbT(_ => Monad[F].pure(x))
    override def flatMap[A, B](fa: DbT[F, A])(fx: A => DbT[F, B]): DbT[F, B] =
      DbT(db => fa.runDb(db).flatMap(x => fx(x).runDb(db)))
    override def tailRecM[A, B](a: A)(fx: A => DbT[F, Either[A, B]]): DbT[F, B] =
      DbT(db => Monad[F].tailRecM(a)(x => fx(x).runDb(db)))
  }
}

trait MonadDb[F[_]] {
  def queryUser(userId:String):F[User]
  def storeUser(user:User):F[Unit]
}
