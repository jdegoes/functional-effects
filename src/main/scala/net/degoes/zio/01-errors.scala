package net.degoes.zio

import zio._

/*
 * INTRODUCTION
 *
 * ZIO effects model failure, in a way similar to the Scala data types `Try`
 * and `Either`. Unlike exceptions, ZIO effects are statically-typed, allowing
 * you to determine if and how effects fail by looking at type signatures.
 *
 * ZIO effects have a large number of error-related operators to transform
 * and combine effects. Some of these "catch" errors, while others transform
 * them, and still others combine potentially failing effects with fallback
 * effects.
 *
 * In this section, you will learn about all these operators, as well as the
 * rich underlying model of errors that ZIO uses internally.
 */

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

object ErrorNarrowing extends App {
  import java.io.IOException
  import scala.io.StdIn.readLine
  implicit class Unimplemented[A](v: A) {
    def ? = ???
  }

  val broadReadLine: IO[Throwable, String] = ZIO.effect(scala.io.StdIn.readLine())

  /**
   * EXERCISE
   *
   * Using `ZIO#refineToOrDie`, narrow the error type of `broadReadLine` into
   * an `IOException`:
   */
  val myReadLine: IO[IOException, String] = broadReadLine ?

  def myPrintLn(line: String): UIO[Unit] = UIO(println(line))

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    (for {
      _    <- myPrintLn("What is your name?")
      name <- myReadLine
      _    <- myPrintLn(s"Good to meet you, ${name}")
    } yield 0) orElse ZIO.succeed(1)
}
