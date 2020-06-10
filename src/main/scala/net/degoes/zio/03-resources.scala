package net.degoes.zio

import zio._
import java.text.NumberFormat
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

object Cat extends App {
  import zio.console._
  import zio.blocking._
  import java.io.IOException

  /**
   * EXERCISE
   *
   * Implement a function to read a file on the blocking thread pool, storing
   * the result into a string.
   */
  def readFile(file: String): ZIO[Blocking, IOException, String] =
    ???

  /**
   * EXERCISE
   *
   * Implement a version of the command-line utility "cat", which dumps the
   * contents of the specified file to standard output.
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    ???
}

object SourceManaged extends App {
  import zio.console._
  import zio.blocking._
  import zio.duration._
  import java.io.IOException

  import scala.io.Source

  final class ZioSource private (private val source: Source) {
    def execute[T](f: Source => T): ZIO[Blocking, IOException, T] =
      effectBlocking(f(source)).refineToOrDie[IOException]
  }
  object ZioSource {

    /**
     * EXERCISE
     *
     * Use the `ZManaged.make` constructor to make a managed data type that
     * will automatically acquire and release the resource when it is used.
     */
    def make(file: String): ZManaged[Blocking, IOException, ZioSource] = {
      // An effect that acquires the resource:
      val open = effectBlocking(new ZioSource(Source.fromFile(file)))
        .refineToOrDie[IOException]

      // A function that, when given the resource, returns an effect that
      // releases the resource:
      val close: ZioSource => ZIO[Blocking, Nothing, Unit] =
        _.execute(_.close()).orDie

      ???
    }
  }

  /**
   * EXERCISE
   *
   * Implement a function to read a file on the blocking thread pool, storing
   * the result into a string.
   */
  def readFiles(
    files: List[String]
  ): ZIO[Blocking with Console, IOException, Unit] =
    ???

  /**
   * EXERCISE
   *
   * Implement a function that prints out all files specified on the
   * command-line. Only print out contents from these files if they
   * can all be opened simultaneously. Otherwise, don't print out
   * anything except an error message.
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    ???
}

object CatIncremental extends App {
  import zio.console._
  import zio.blocking._
  import java.io.{ FileInputStream, IOException, InputStream }

  /**
   * BONUS EXERCISE
   *
   * Implement a `blockingIO` combinator to use in subsequent exercises.
   */
  def blockingIO[A](a: => A): ZIO[Blocking, IOException, A] =
    ???

  /**
   * EXERCISE
   *
   * Implement all missing methods of `FileHandle`. Be sure to do all work on
   * the blocking thread pool.
   */
  final case class FileHandle private (private val is: InputStream) {
    final def close: ZIO[Blocking, IOException, Unit] = ???

    final def read: ZIO[Blocking, IOException, Option[Chunk[Byte]]] =
      ???
  }
  object FileHandle {
    final def open(file: String): ZIO[Blocking, IOException, FileHandle] =
      ???
  }

  def cat(fh: FileHandle): ZIO[Blocking with Console, IOException, Unit] =
    ???

  /**
   * EXERCISE
   *
   * Implement an incremental version of the `cat` utility, using `ZIO#bracket`
   * or `ZManaged` to ensure the file is closed in the event of error or
   * interruption.
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    args match {
      case file :: Nil =>
        // (effect timeout 60.seconds) ensuring finalizer
        (FileHandle.open(file).bracket(_.close.ignore)(cat) as 0) orElse ZIO
          .succeed(1)

      case _ => putStrLn("Usage: cat <file>") as 2
    }
}
