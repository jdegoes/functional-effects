package net.degoes.zio

object ConsoleInput extends zio.App {
  import java.io.IOException

  import zio._
  import zio.console._
  import zio.stream._

  /**
   * EXERCISE
   *
   * Using `ZStream.fromEffect` and `getStrLn`, construct a stream that
   * will emit a single string, taken from the console.
   */
  val singleRead: ZStream[Console, IOException, String] = ???

  /**
   * Using `ZStream#forever`, take the `singleRead` stream, and turn it into
   * a stream that repeats forever.
   */
  val consoleInput: ZStream[Console, IOException, String] = ???

  sealed trait Command
  object Command {
    case object Quit                    extends Command
    case class Unknown(command: String) extends Command
  }

  val commandInput = consoleInput.map(_.trim.toLowerCase).map[Command] {
    case "quit" => Command.Quit
    case x      => Command.Unknown(x)
  }

  def run(args: List[String]) =
    commandInput
      .tap(command => putStrLn(s"You entered: ${command}"))
      .takeUntil(_ == Command.Quit)
      .runDrain
      .ignore as ExitCode.success
}

object FileStream extends zio.App {
  import zio._
  import zio.console._
  import zio.stream._
  import zio.blocking._

  import java.nio.file.Path

  /**
   * EXERCISE
   *
   * Using `ZStream.fromFile`, construct a stream of bytes from a file.
   */
  def open(file: String): ZStream[Blocking, Throwable, Byte] = ???

  def run(args: List[String]) =
    (args match {
      case file :: Nil => open(file).foreach(byte => putStr(byte.toString()))
      case _           => putStrLn("Expected file name!")
    }).exitCode
}

/**
 * Streams are lazy, and potentially infinite. Unless you pull values from a stream, nothing
 * happens.
 */
object StreamForeach extends zio.App {
  import zio._
  import zio.console._
  import zio.stream._
  import zio.blocking._

  import java.nio.file.Path

  val fibonacci = ZStream.unfold((0, 1)) {
    case (s1, s2) =>
      val s3 = s1 + s2

      Some((s1 + s2), (s2, s3))
  }

  /**
   * EXERCISE
   *
   * Using `ZStream#take`, take the first 100 numbers from the `fibonacci` stream.
   */
  lazy val first100: ZStream[Any, Nothing, Int] = ???

  /**
   * EXERCISE
   *
   * Using `ZStream#foreach`, which is one of the ways to "run" a stream, and `putStrLn`, print out
   * each of the first 100 fibonacci numbers.
   */
  def run(args: List[String]) =
    ???
}

object StreamRunCollect extends zio.App {
  import zio._
  import zio.console._
  import zio.stream._
  import zio.blocking._

  import java.nio.file.Path

  val fibonacci = ZStream.unfold((0, 1)) {
    case (s1, s2) =>
      val s3 = s1 + s2

      Some((s1 + s2), (s2, s3))
  }

  val first100: ZStream[Any, Nothing, Int] = fibonacci.take(100)

  /**
   * EXERCISE
   *
   * Using `ZStream#runCollect`, which is one of the ways to "run" a stream, collect all of the
   * numbers from the stream `first100`, and print them out using `putStrLn`.
   */
  def run(args: List[String]) =
    ???
}

/**
 * Transducers convert one or more elements of one type into one or more elements of another type.
 * Transducers can be stacked onto streams, to transform their element type in a stateful way from
 * one type to another. Tranducers are used for encoding data, decoding data, encryption, and
 * many other purposes.
 */
object FileTransducer extends zio.App {
  import zio._
  import zio.console._
  import zio.stream._
  import zio.blocking._

  import java.nio.file.Path

  /**
   * EXERCISE
   *
   * Using the `open` function you wrote in `FileStream` and `ZTransducer.utf8Decode`, construct a
   * stream of strings by using the `ZStream#>>>` method.
   */
  def open(file: String): ZStream[Blocking, Throwable, String] = {
    val _ = FileStream.open(file)

    ???
  }

  def run(args: List[String]) =
    (args match {
      case file :: Nil => open(file).foreach(putStr(_))
      case _           => putStrLn("Expected file name!")
    }).exitCode
}

/**
 * Sinks consume input. They are the opposite of streams, which produce input. Sinks can be
 * resourceful, for example, like a file that must be opened, written to, and closed. Sinks
 * can run forever, or run for a while and then terminate with a typed value.
 *
 * Sinks can be used for parsing, because they can consume some input that matches what they are
 * looking for (or fail trying), and then terminate with the value that they parse.
 */
object FileSink extends zio.App {
  import zio._
  import zio.console._
  import zio.stream._
  import zio.blocking._

  import java.nio.file.Path

  /**
   * EXERCISE
   *
   * Using the `open` function you wrote in `FileStream`, and `ZSink.fromFile`, implement a method
   * to copy a file from one location to another.
   */
  def copy(source: String, dest: String): ZIO[Blocking, Throwable, Any] = {
    val _ = FileStream.open(source)

    ???
  }

  def run(args: List[String]) =
    (args match {
      case source :: dest :: Nil => copy(source, dest)
      case _                     => putStrLn("Expected source and dest name!")
    }).exitCode
}

/**
 * Sinks can also be used for aggregation.
 */
object FileSinkMapReduce extends zio.App {
  import zio._
  import zio.console._
  import zio.stream._
  import zio.blocking._

  import java.nio.file.Path

  /**
   * EXERCISE
   *
   * Using `ZSink.fold`, create a custom sink that counts words in a file, and use that, together
   * with the other functionality you created or learned about, to implement a word count program.
   */
  def wordCount(file: String): ZIO[Blocking, Throwable, Map[String, Int]] = ???

  def run(args: List[String]) =
    (args match {
      case file :: Nil => wordCount(file).flatMap(map => putStrLn(map.mkString))
      case _           => putStrLn("Expected name of file to word count!")
    }).exitCode
}
