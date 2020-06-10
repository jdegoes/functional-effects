package net.degoes.zio

import zio._

object Looping extends App {
  import zio.console._

  /**
   * EXERCISE
   *
   * Implement a `repeat` combinator using `flatMap` and recursion.
   */
  def repeat[R, E, A](n: Int)(effect: ZIO[R, E, A]): ZIO[R, E, A] =
    ???

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    repeat(100)(putStrLn("All work and no play makes Jack a dull boy")) as 0
}

object Interview extends App {
  import zio.console._

  val questions =
    "Where where you born?" ::
      "What color are your eyes?" ::
      "What is your favorite movie?" ::
      "What is your favorite number?" :: Nil

  /**
   * EXERCISE
   *
   * Using `ZIO.foreach`, iterate over all of the `questions`, and for each
   * question, print out the question, and read the answer from the console
   * using `getStrLn`, collecting all of the answers into a list.
   *
   * Print out the answers when done.
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    ???
}
