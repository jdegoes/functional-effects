package net.degoes.zio

import zio._

object Sharding extends App {
  import zio.console._

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

  def run(args: List[String]) = {
    def makeWorker(ref: Ref[Int]): Int => ZIO[Console, String, Unit] =
      (work: Int) =>
        for {
          count <- ref.get
          _ <- if (count < 100) putStrLn(s"Worker is processing item ${work} after ${count}")
              else ZIO.fail(s"Uh oh, failed processing ${work} after ${count}")
          _ <- ref.update(_ + 1)
        } yield ()

    for {
      queue <- Queue.bounded[Int](100)
      ref   <- Ref.make(0)
      _     <- queue.offer(1).forever.fork
      error <- shard(queue, 10, makeWorker(ref))
      _     <- putStrLn(s"Failed with ${error}")
    } yield ExitCode.success
  }
}
