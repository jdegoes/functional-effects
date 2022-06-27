package net.degoes.zio

import zio._
import java.io.IOException

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

object ErrorConstructor extends ZIOAppDefault {

  /**
   * EXERCISE
   *
   * Using `ZIO.fail`, construct an effect that models failure with any
   * string value, such as "Uh oh!". Explain the type signature of the
   * effect.
   */
  val failed: ZIO[Any, String, Nothing] = ???

  val run =
    failed.foldZIO(Console.printLine(_), Console.printLine(_))
}

object ErrorRecoveryOrElse extends ZIOAppDefault {

  val failed = ZIO.fail("Uh oh!")

  /**
   * EXERCISE
   *
   * Using `ZIO#orElse` have the `run` function compose the preceding `failed`
   * effect with another effect.
   */
  val run =
    ???
}

object ErrorShortCircuit extends ZIOAppDefault {
  val failed: ZIO[Any, Any, Unit] =
    for {
      _ <- Console.printLine("About to fail...")
      _ <- ZIO.fail("Uh oh!")
      _ <- Console.printLine("This will NEVER be printed!")
    } yield ()

  /**
   * EXERCISE
   *
   * Using `ZIO#orElse`, compose the `failed` effect with another effect that
   * succeeds with an exit code.
   */
  val run =
    ???
}

object ErrorRecoveryFold extends ZIOAppDefault {

  val failed = ZIO.fail("Uh oh!")

  /**
   * EXERCISE
   *
   * Using `ZIO#fold`, map both failure and success values of `failed` into
   * the unit value.
   */
  val run =
    ???
}

object ErrorRecoveryCatchAll extends ZIOAppDefault {

  val failed: ZIO[Any, String, Nothing] = ZIO.fail("Uh oh!")

  /**
   * EXERCISE
   *
   * Using `ZIO#catchAll`, catch all errors in `failed` and print them out to
   * the console using `Console.printLine`.
   */
  val run =
    ???
}

object ErrorRecoveryFoldZIO extends ZIOAppDefault {

  val failed: ZIO[Any, String, String] = ZIO.fail("Uh oh!")

  /**
   * EXERCISE
   *
   * Using `ZIO#foldZIO`, print out the success or failure value of `failed`
   * by using `Console.printLine`.
   */
  val run =
    ???
}

object ErrorRecoveryEither extends ZIOAppDefault {

  val failed: ZIO[Any, String, Int] = ZIO.fail("Uh oh!")

  /**
   * EXERCISE
   *
   * Using `ZIO#either`, surface the error of `failed` into the success
   * channel, and then map the `Either[String, Int]` into an exit code.
   */
  val run =
    ???
}

object ErrorRecoveryIgnore extends ZIOAppDefault {

  val failed: ZIO[Any, String, Int] = ZIO.fail("Uh oh!")

  /**
   * EXERCISE
   *
   * Using `ZIO#ignore`, simply ignore the failure of `failed`.
   */
  val run =
    ???
}

object ErrorRefinement1 extends ZIOAppDefault {
  import java.io.IOException
  import scala.io.StdIn.readLine

  val broadReadLine: IO[Throwable, String] = ZIO.attempt(scala.io.StdIn.readLine())

  /**
   * EXERCISE
   *
   * Using `ZIO#refineToOrDie`, narrow the error type of `broadReadLine` into
   * an `IOException`:
   */
  val myReadLine: IO[IOException, String] = ???

  def myPrintLn(line: String): UIO[Unit] = ZIO.succeed(println(line))

  val run =
    (for {
      _    <- myPrintLn("What is your name?")
      name <- myReadLine
      _    <- myPrintLn(s"Good to meet you, ${name}!")
    } yield ())
}

object ErrorRefinement2 extends ZIOAppDefault {

  import java.io.IOException
  import java.util.concurrent.TimeUnit

  /**
   * EXERCISE
   *
   * Create an effect that will get a `Duration` from the user, by prompting
   * the user to enter a decimal number of seconds. Use `refineToOrDie` to
   * narrow the error type as necessary.
   */
  lazy val getAlarmDuration: ZIO[Any, IOException, Duration] = {
    def parseDuration(input: String): IO[NumberFormatException, Duration] =
      ???

    def fallback(input: String): ZIO[Any, IOException, Duration] =
      Console.printLine(s"The input ${input} is not valid.") *> getAlarmDuration

    for {
      _        <- Console.printLine("Please enter the number of seconds to sleep: ")
      input    <- Console.readLine
      duration <- parseDuration(input) orElse fallback(input)
    } yield duration
  }

  /**
   * EXERCISE
   *
   * Create a program that asks the user for a number of seconds to sleep,
   * sleeps the specified number of seconds using `ZIO.sleep(d)`, and then
   * prints out a wakeup alarm message, like "Time to wakeup!!!".
   */
  val run =
    ???
}

object ZIOFinally extends ZIOAppDefault {

  /**
   * EXERCISE
   *
   * Using `ZIO#ensuring`, attach an effect to the `tickingBomb`
   * effect, which will be executed whether `tickingBomb` succeeds
   * or fails. Print out a message to the console saying "Executed".
   */
  lazy val tickingBomb2 = tickingBomb

  /**
   * EXERCISE
   *
   * See if you can break the guarantee of `ZIO#ensuring` so that
   * "Executed" is not printed out.
   */
  val tickingBomb =
    ZIO.sleep(1.second) *> ZIO.fail("Boom!")

  val run = tickingBomb2
}

object SequentialCause extends ZIOAppDefault {

  val failed1 = Cause.fail("Uh oh 1")
  val failed2 = Cause.fail("Uh oh 2")

  /**
   * EXERCISE
   *
   * Using `Cause.++`, form a sequential cause by composing `failed1`
   * and `failed2`.
   */
  lazy val composed = ???

  /**
   * EXERCISE
   *
   * Using `Cause.prettyPrint`, dump out `composed` to the console.
   */
  val run =
    ???
}

object ParalellCause extends ZIOAppDefault {

  val failed1 = Cause.fail("Uh oh 1")
  val failed2 = Cause.fail("Uh oh 2")

  /**
   * EXERCISE
   *
   * Using `Cause.&&`, form a parallel cause by composing `failed1`
   * and `failed2`.
   */
  lazy val composed = ???

  /**
   * EXERCISE
   *
   * Using `Cause.prettyPrint`, dump out `composed` to the console.
   */
  val run =
    ???
}

object Sandbox extends ZIOAppDefault {

  val failed1    = ZIO.fail("Uh oh 1")
  val failed2    = ZIO.fail("Uh oh 2")
  val finalizer1 = ZIO.fail(new Exception("Finalizing 1!")).orDie
  val finalizer2 = ZIO.fail(new Exception("Finalizing 2!")).orDie

  val composed = ZIO.uninterruptible {
    (failed1 ensuring finalizer1) zipPar (failed2 ensuring finalizer2)
  }

  /**
   * EXERCISE
   *
   * Using `ZIO#sandbox`, sandbox the `composed` effect and print out the
   * resulting `Cause` value to the console using `Console.printLine`.
   */
  val run =
    ???
}
