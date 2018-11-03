package space.dehun

import cats._
import cats.implicits._
import cats.effect.{Effect, IO}


object AppStack {
  type Layer1[A] = NetT[IO, A]
  type Layer2[A] = DbT[Layer1, A]
  type Layer3[A] = LogT[Layer2, A]
  type AppStack[A] = Layer3[A]
  val AppStack = LogT

  implicit val appStackMonadInstance = LogT.logTMonadInstance[Layer3]

  implicit val monadLog = new MonadLog[AppStack] {
    override def log(msg: String): AppStack[Unit] =
      LogT[Layer2, Unit](logger => DbT[Layer1, Unit](db => NetT[IO, Unit](net => IO.pure[Unit](logger.log(msg)))))
  }

  implicit val monadDb = new MonadDb[AppStack] {
    override def queryUser(userId: String): AppStack[DB.User] =
      LogT[Layer2, DB.User](logger => DbT[Layer1, DB.User](db => NetT[IO, DB.User](net => db.queryUser(userId))))

    override def storeUser(newUser: DB.User): AppStack[Unit] =
      LogT[Layer2, Unit](logger => DbT[Layer1, Unit](db => NetT[IO, Unit](net => db.storeUser(newUser))))
  }

  implicit val monadNet = new MonadNet[AppStack] {
    override def notifyUserChange(user: DB.User): AppStack[Unit] =
      LogT[Layer2, Unit](logger => DbT[Layer1, Unit](db => NetT[IO, Unit](net => net.notifyUserChange(user))))
  }
}

object BetterThanIoMain extends App {
  import AppStack._

  def foo[F[_]:Monad:MonadLog:MonadDb](x:Int):F[DB.User] = for {
    user <- implicitly[MonadDb[F]].queryUser(x.toString)
    _ <- implicitly[MonadLog[F]].log(s"got user ${user}")
    _ <- implicitly[MonadDb[F]].storeUser(user.copy(age=user.age + 1))
  } yield user

  def bar[F[_]:Monad:MonadLog:MonadNet](user:DB.User):F[Int] = for {
    _ <- implicitly[MonadLog[F]].log("lets notify user change!")
    _ <- implicitly[MonadNet[F]].notifyUserChange(user)
  } yield user.age

  override def main(args: Array[String]): Unit = {
    val logger:Logger = new Logger()
    val db:DB = new DB()
    val net:Net = new Net()

    ( foo[AppStack](12) >>= bar[AppStack] )
      .runLog(logger).runDb(db).runNet(net).unsafeRunSync()
  }
}
