package net.degoes.zio

import zio._

object SimpleLogging extends ZIOAppDefault {

  /**
   * EXERCISE
   *
   * Add logging using `ZIO.log` around each update of the ref.
   */
  val program =
    for {
      ref   <- Ref.make(0)
      _     <- ZIO.foreachPar(0 to 10)(i => ref.update(_ + i))
      value <- ref.get
    } yield value

  /**
   * EXERCISE
   *
   * Surround `program` in `LogLevel.Error` to change its log level.
   */
  val program2: ZIO[Any, Nothing, Int] = program

  val run = program *> program2
}

object LogSpan extends ZIOAppDefault {

  /**
   * EXERCISE
   *
   * Add a log span of "createUser" to the whole function.
   */
  def createUser(userName: String, passHash: String, salt: Long): ZIO[Any, Nothing, Unit] =
    for {
      _ <- ZIO.log(s"Creating user ${userName}")
    } yield ()

  val run =
    createUser("sherlockholmes", "jkdf67sf6", 21381L)
}
