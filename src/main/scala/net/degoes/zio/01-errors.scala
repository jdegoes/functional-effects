package net.degoes.zio

import zio._

object ErrorConstructor extends App {
  import zio.console._

  /**
   * EXERCISE
   *
   * Using `ZIO.fail`, construct an effect that models failure with any
   * string value, such as "Uh oh!". Explain the type signature of the
   * effect.
   */
  val failed: ZIO[Any, String, Nothing] = ???

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    failed.foldM(putStrLn(_), putStrLn(_)) as 0
}

object ErrorRecoveryOrElse extends App {
  import zio.console._

  val failed = ZIO.fail("Uh oh!")

  /**
   * EXERCISE
   *
   * Using `ZIO#orElse` have the `run` function compose the preceding `failed`
   * effect with another effect that succeeds with a success exit code.
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    ???
}

object ErrorShortCircuit extends App {
  import zio.console._

  val failed =
    putStrLn("About to fail...") *>
      ZIO.fail("Uh oh!") *>
      putStrLn("This will NEVER be printed!")

  /**
   * EXERCISE
   *
   * Using `ZIO#orElse` have the `run` function compose the
   * preceding `failed` effect with another effect that
   * succeeds with an exit code (created with `ZIO.succeed`).
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    ???
}

object ErrorRecoveryFold extends App {
  import zio.console._

  val failed = ZIO.fail("Uh oh!")

  /**
   * EXERCISE
   *
   * Using `ZIO#fold`, map both failure and success values of `failed` into
   * exit codes.
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    ???
}

object ErrorRecoveryCatchAll extends App {
  import zio.console._

  val failed = ZIO.fail("Uh oh!")

  /**
   * EXERCISE
   *
   * Using `ZIO#catchAll`, catch all errors in `failed` and print them out to
   * the console using `putStrLn`.
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    ???
}

object ErrorRecoveryFoldM extends App {
  import zio.console._

  val failed: ZIO[Any, String, String] = ZIO.fail("Uh oh!")

  /**
   * EXERCISE
   *
   * Using `ZIO#foldM`, print out the success or failure value of `failed`
   * by using `putStrLn`.
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    ???
}

object ErrorRecoveryEither extends App {
  import zio.console._

  val failed: ZIO[Any, String, Int] = ZIO.fail("Uh oh!")

  /**
   * EXERCISE
   *
   * Using `ZIO#either`, surface the error of `failed` into the success
   * channel, and then map the `Either[String, Int]` into an exit code.
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    ???
}

object ErrorRecoveryIgnore extends App {
  import zio.console._

  val failed: ZIO[Any, String, Int] = ZIO.fail("Uh oh!")

  /**
   * EXERCISE
   *
   * Using `ZIO#ignore`, simply ignore the failure of `failed`, and then map
   * the resulting unit into a successful exit code.
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    ???
}
