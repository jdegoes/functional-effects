package net.degoes.zio

object Streams {
  import zio.console._
  import zio.stream._

  val stream1 = Stream(1, 2, 3)

  val stream2 = ZStream.fromEffect(getStrLn).forever

  sealed trait Command
  object Command {
    case object Quit                    extends Command
    case class Unknown(command: String) extends Command
  }

  val stream3 = stream2.map[Command] {
    case "quit" => Command.Quit
    case x      => Command.Unknown(x)
  }

  def run(args: List[String]) = ???
}
