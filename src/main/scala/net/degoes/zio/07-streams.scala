package net.degoes.zio

object ConsoleInput {
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

  val commandInput = consoleInput.map[Command] {
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
