package net.degoes.zio

import zio._
import scala.concurrent.ExecutionContext
import java.io.IOException

object CustomRuntime {
  val defaultEnvironment  = ZEnvironment.empty
  val defaultRuntimeFlags = RuntimeFlags.default
  val defaultFiberRefs    = FiberRefs.empty

  final case class AppConfig(name: String)

  /**
   * EXERCISE
   *
   * Create a custom runtime that bundles a value of type `AppConfig` into the
   * environment.
   */
  lazy val customRuntime = Runtime(defaultEnvironment, defaultFiberRefs, defaultRuntimeFlags)

  val program: ZIO[AppConfig, IOException, Unit] =
    for {
      appConfig <- ZIO.service[AppConfig]
      _         <- Console.printLine(s"Application name is ${appConfig.name}")
      _         <- Console.printLine("What is your name?")
      name      <- Console.readLine
      _         <- Console.printLine(s"Hello, ${name}!")
    } yield ()

  /**
   * EXERCISE
   *
   * Using the `run` method of the custom runtime you created,
   * execute the `program` effect above.
   *
   * NOTE: You will have to use `Unsafe.unsafe { implicit u => ... }`
   * or `Unsafe.unsafe { ... }` (Scala 3) in order to call `run`.
   */
  def main(args: Array[String]): Unit =
    ???
}
object ThreadPool extends ZIOAppDefault {

  lazy val dbPool: Executor = Executor.fromExecutionContext(ExecutionContext.global)

  /**
   * EXERCISE
   *
   * Using `ZIO#onExecutor`, write an `onDatabase` combinator that runs the
   * specified effect on the database thread pool.
   */
  def onDatabase[R, E, A](zio: ZIO[R, E, A]): ZIO[R, E, A] = ???

  /**
   * EXERCISE
   *
   * Implement a combinator to print out thread information before and after
   * executing the specified effect.
   */
  def threadLogged[R, E, A](zio: ZIO[R, E, A]): ZIO[R, E, A] = {
    val log = ZIO.succeed {
      val thread = Thread.currentThread()

      val id        = ???
      val name      = ???
      val groupName = ???

      println(s"Thread($id, $name, $groupName)")
    }

    zio
  }

  /**
   * EXERCISE
   *
   * Use the `threadLogged` combinator around different effects below to
   * determine which threads are executing which effects.
   */
  val run =
    (Console.printLine("Main") *>
      onDatabase {
        Console.printLine("Database") *>
          ZIO.blocking {
            Console.printLine("Blocking")
          } *>
          Console.printLine("Database")
      } *>
      Console.printLine("Main"))
}

object CustomLogger {

  /**
   * EXERCISE
   *
   * Using `ZLogger.simple`, create a logger that dumps text strings to the console
   * using `println`.
   */
  lazy val simpleLogger: ZLogger[String, Unit] = ???

  /**
   * EXERCISE
   *
   * Create a layer that will install your simple logger using Runtime.addLogger.
   */
  lazy val withCustomLogger: ZLayer[Any, Nothing, Unit] = ???

  /**
   * EXERCISE
   *
   * Using `ZIO#provide`, inject the custom logger into the following effect
   * and verify your logger is being used.
   */
  val run =
    ZIO.log("Hello World!")
}
