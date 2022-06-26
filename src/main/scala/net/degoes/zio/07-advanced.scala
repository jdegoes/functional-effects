package net.degoes.zio

import zio._
import zio.Executor
import scala.concurrent.ExecutionContext

object PoolLocking extends ZIOAppDefault {
  import zio.Console._

  lazy val dbPool: Executor = Executor.fromExecutionContext(ExecutionContext.global)

  /**
   * EXERCISE
   *
   * Using `ZIO#lock`, write an `onDatabase` combinator that runs the
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

      val id        = thread.getId()
      val name      = thread.getName()
      val groupName = thread.getThreadGroup().getName()

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
    (printLine("Main") *>
      onDatabase {
        printLine("Database") *>
          ZIO.blocking {
            printLine("Blocking")
          } *>
          printLine("Database")
      } *>
      printLine("Main"))
}

object PlatformTweaking {
  // import Console._
  // import zio.internal.Platform

  // /**
  //  * EXERCISE
  //  *
  //  * Modify the default platform by specifying a custom behavior for logging errors.
  //  */
  // lazy val platform = Platform.default.copy(reportFailure = ???)

  // val environment = Runtime.default.environment

  // /**
  //  * EXERCISE
  //  *
  //  * Create a custom runtime using `platform` and `environment`, and use this to
  //  * run an effect.
  //  */
  // lazy val customRuntime: Runtime[Any] = ???
  // def exampleRun                        = customRuntime.unsafeRun(printLine("Test effect"))
}

object Sharding extends ZIOAppDefault {
  import zio.Console._

  /**
   * EXERCISE
   *
   * Create N workers reading from a Queue, if one of them fails, then wait
   * for the other ones to process their current item, but terminate all the
   * workers.
   *
   * Return the first error, or never return, if there is no error.
   */
  def shard[R, E, A](
    queue: Queue[A],
    n: Int,
    worker: A => ZIO[R, E, Unit]
  ): ZIO[R, Nothing, E] = ???

  val run = {
    def makeWorker(ref: Ref[Int]): Int => ZIO[Any, String, Unit] =
      (work: Int) =>
        for {
          count <- ref.get
          _ <- if (count < 100) printLine(s"Worker is processing item ${work} after ${count}").orDie
              else ZIO.fail(s"Uh oh, failed processing ${work} after ${count}")
          _ <- ref.update(_ + 1)
        } yield ()

    (for {
      queue <- Queue.bounded[Int](100)
      ref   <- Ref.make(0)
      _     <- queue.offer(1).forever.fork
      error <- shard(queue, 10, makeWorker(ref))
      _     <- printLine(s"Failed with ${error}")
    } yield ())
  }
}
