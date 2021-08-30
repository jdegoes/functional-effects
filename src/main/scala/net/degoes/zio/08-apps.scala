package net.degoes.zio

import zio._
import java.text.NumberFormat
import java.nio.charset.StandardCharsets

object SimpleActor extends App {
  import zio.Console._
  import zio.stm._

  sealed trait Command
  case object ReadTemperature                       extends Command
  final case class AdjustTemperature(value: Double) extends Command

  type TemperatureActor = Command => Task[Double]

  /**
   * EXERCISE
   *
   * Using ZIO Queue and Promise, implement the logic necessary to create an
   * actor as a function from `Command` to `Task[Double]`.
   */
  def makeActor(initialTemperature: Double): UIO[TemperatureActor] = {
    type Bundle = (Command, Promise[Nothing, Double])

    ???
  }

  def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    val temperatures = (0 to 100).map(_.toDouble)

    (for {
      actor <- makeActor(0)
      _ <- ZIO.foreachPar(temperatures) { temp =>
            actor(AdjustTemperature(temp))
          }
      temp <- actor(ReadTemperature)
      _    <- printLine(s"Final temperature is ${temp}")
    } yield ()).exitCode
  }
}

object parallel_web_crawler {
  import zio.Console._
  import zio.Clock._

  trait Web {
    def getURL(url: URL): IO[Exception, String]
  }
  object Web {

    /**
     * EXERCISE
     *
     * Implement a layer for `Web` that uses the `ZIO.attemptBlockingIO` combinator
     * to safely wrap `Source.fromURL` into a functional effect.
     */
    val live: ZLayer[Any, Nothing, Web] = ???
  }

  /**
   * EXERCISE
   *
   * Using `ZIO.accessM`, delegate to the `Web` module's `getURL` function.
   */
  def getURL(url: URL): ZIO[Has[Web], Exception, String] = ???

  final case class CrawlState[+E](visited: Set[URL], errors: List[E]) {
    final def visitAll(urls: Set[URL]): CrawlState[E] = copy(visited = visited ++ urls)

    final def logError[E1 >: E](e: E1): CrawlState[E1] = copy(errors = e :: errors)
  }

  /**
   * EXERCISE
   *
   * Implement the `crawl` function using the helpers provided in this object.
   *
   * {{{
   * def getURL(url: URL): ZIO[Blocking, Exception, String]
   * def extractURLs(root: URL, html: String): List[URL]
   * }}}
   */
  def crawl[E](
    seeds: Set[URL],
    router: URL => Set[URL],
    processor: (URL, String) => IO[E, Unit]
  ): ZIO[Has[Web] with Has[Clock], Nothing, List[E]] = {
    val emptySet = ZIO.succeed(Set.empty[URL])

    def loop(seeds: Set[URL], ref: Ref[CrawlState[E]]): ZIO[Has[Web] with Has[Clock], Nothing, Unit] =
      if (seeds.isEmpty) ZIO.unit
      else ???

    for {
      ref   <- Ref.make[CrawlState[E]](CrawlState(seeds, Nil))
      _     <- loop(seeds, ref)
      state <- ref.get
    } yield state.errors
  }

  /**
   * A data structure representing a structured URL, with a smart constructor.
   */
  final case class URL private (parsed: io.lemonlabs.uri.Url) {
    import io.lemonlabs.uri._

    final def relative(page: String): Option[URL] =
      scala.util
        .Try(parsed.path match {
          case Path(parts) =>
            val whole = parts.dropRight(1) :+ page.dropWhile(_ == '/')

            parsed.withPath(UrlPath(whole))
        })
        .toOption
        .map(new URL(_))

    def url: String = parsed.toString

    override def equals(a: Any): Boolean = a match {
      case that: URL => this.url == that.url
      case _         => false
    }

    override def hashCode: Int = url.hashCode
  }

  object URL {
    import io.lemonlabs.uri._

    def make(url: String): Option[URL] =
      scala.util.Try(AbsoluteUrl.parse(url)).toOption match {
        case None         => None
        case Some(parsed) => Some(new URL(parsed))
      }
  }

  /**
   * A function that extracts URLs from a given web page.
   */
  def extractURLs(root: URL, html: String): List[URL] = {
    val pattern = "href=[\"\']([^\"\']+)[\"\']".r

    scala.util
      .Try({
        val matches = (for (m <- pattern.findAllMatchIn(html)) yield m.group(1)).toList

        for {
          m   <- matches
          url <- URL.make(m).toList ++ root.relative(m).toList
        } yield url
      })
      .getOrElse(Nil)
  }

  object test {
    val Home          = URL.make("http://zio.dev").get
    val Index         = URL.make("http://zio.dev/index.html").get
    val ScaladocIndex = URL.make("http://zio.dev/scaladoc/index.html").get
    val About         = URL.make("http://zio.dev/about").get

    val SiteIndex =
      Map(
        Home          -> """<html><body><a href="index.html">Home</a><a href="/scaladoc/index.html">Scaladocs</a></body></html>""",
        Index         -> """<html><body><a href="index.html">Home</a><a href="/scaladoc/index.html">Scaladocs</a></body></html>""",
        ScaladocIndex -> """<html><body><a href="index.html">Home</a><a href="/about">About</a></body></html>""",
        About         -> """<html><body><a href="home.html">Home</a><a href="http://google.com">Google</a></body></html>"""
      )

    /**
     * EXERCISE
     *
     * Implement a test layer using the SiteIndex data.
     */
    val testLayer: ZLayer[Any, Nothing, Web] = ???

    val TestRouter: URL => Set[URL] =
      url => if (url.parsed.apexDomain == Some("zio.dev")) Set(url) else Set()

    val Processor: (URL, String) => IO[Unit, List[(URL, String)]] =
      (url, html) => IO.succeed(List(url -> html))
  }

  /**
   * EXERCISE
   *
   * Run your test crawler using the test data, supplying it the custom layer
   * it needs.
   */
  def run(args: List[String]) =
    printLine("Hello World!").exitCode
}

object Hangman extends App {
  import Dictionary.Dictionary
  import zio.Console._
  import zio.Random._
  import java.io.IOException

  /**
   * EXERCISE
   *
   * Implement an effect that gets a single, lower-case character from
   * the user.
   */
  lazy val getChoice: ZIO[Has[Console], IOException, Char] = ???

  /**
   * EXERCISE
   *
   * Implement an effect that prompts the user for their name, and
   * returns it.
   */
  lazy val getName: ZIO[Has[Console], IOException, String] = ???

  /**
   * EXERCISE
   *
   * Implement an effect that chooses a random word from the dictionary.
   * The dictionary is `Dictionary.Dictionary`.
   */
  lazy val chooseWord: ZIO[Has[Random], Nothing, String] = ???

  /**
   * EXERCISE
   *
   * Implement the main game loop, which gets choices from the user until
   * the game is won or lost.
   */
  def gameLoop(oldState: State): ZIO[Has[Console], IOException, Unit] = ???

  def renderState(state: State): ZIO[Has[Console], IOException, Unit] = {

    /**
     *
     *  f     n  c  t  o
     *  -  -  -  -  -  -  -
     *
     *  Guesses: a, z, y, x
     *
     */
    val word =
      state.word.toList
        .map(c => if (state.guesses.contains(c)) s" $c " else "   ")
        .mkString("")

    val line = List.fill(state.word.length)(" - ").mkString("")

    val guesses = " Guesses: " + state.guesses.mkString(", ")

    val text = word + "\n" + line + "\n\n" + guesses + "\n"

    printLine(text)
  }

  final case class State(name: String, guesses: Set[Char], word: String) {
    final def failures: Int = (guesses -- word.toSet).size

    final def playerLost: Boolean = failures > 10

    final def playerWon: Boolean = (word.toSet -- guesses).isEmpty

    final def addChar(char: Char): State = copy(guesses = guesses + char)
  }

  sealed trait GuessResult
  object GuessResult {
    case object Won       extends GuessResult
    case object Lost      extends GuessResult
    case object Correct   extends GuessResult
    case object Incorrect extends GuessResult
    case object Unchanged extends GuessResult
  }

  def analyzeNewInput(
    oldState: State,
    newState: State,
    char: Char
  ): GuessResult =
    if (oldState.guesses.contains(char)) GuessResult.Unchanged
    else if (newState.playerWon) GuessResult.Won
    else if (newState.playerLost) GuessResult.Lost
    else if (oldState.word.contains(char)) GuessResult.Correct
    else GuessResult.Incorrect

  /**
   * EXERCISE
   *
   * Execute the main function and verify your program works as intended.
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    for {
      name  <- getName
      word  <- chooseWord
      state = State(name, Set(), word)
      _     <- renderState(state)
      _     <- gameLoop(state)
    } yield ()
  }.exitCode
}

object TicTacToe extends App {
  import zio.Console._

  sealed trait Mark {
    final def renderChar: Char = this match {
      case Mark.X => 'X'
      case Mark.O => 'O'
    }
    final def render: String = renderChar.toString
  }
  object Mark {
    case object X extends Mark
    case object O extends Mark
  }

  final case class Board private (value: Vector[Vector[Option[Mark]]]) {

    /**
     * Retrieves the mark at the specified row/col.
     */
    final def get(row: Int, col: Int): Option[Mark] =
      value.lift(row).flatMap(_.lift(col)).flatten

    /**
     * Places a mark on the board at the specified row/col.
     */
    final def place(row: Int, col: Int, mark: Mark): Option[Board] =
      if (row >= 0 && col >= 0 && row < 3 && col < 3)
        Some(
          copy(value = value.updated(row, value(row).updated(col, Some(mark))))
        )
      else None

    /**
     * Renders the board to a string.
     */
    def render: String =
      value
        .map(_.map(_.fold(" ")(_.render)).mkString(" ", " | ", " "))
        .mkString("\n---|---|---\n")

    /**
     * Returns which mark won the game, if any.
     */
    final def won: Option[Mark] =
      if (wonBy(Mark.X)) Some(Mark.X)
      else if (wonBy(Mark.O)) Some(Mark.O)
      else None

    private final def wonBy(mark: Mark): Boolean =
      wonBy(0, 0, 1, 1, mark) ||
        wonBy(0, 2, 1, -1, mark) ||
        wonBy(0, 0, 0, 1, mark) ||
        wonBy(1, 0, 0, 1, mark) ||
        wonBy(2, 0, 0, 1, mark) ||
        wonBy(0, 0, 1, 0, mark) ||
        wonBy(0, 1, 1, 0, mark) ||
        wonBy(0, 2, 1, 0, mark)

    private final def wonBy(
      row0: Int,
      col0: Int,
      rowInc: Int,
      colInc: Int,
      mark: Mark
    ): Boolean =
      extractLine(row0, col0, rowInc, colInc).collect { case Some(v) => v }.toList == List
        .fill(3)(mark)

    private final def extractLine(
      row0: Int,
      col0: Int,
      rowInc: Int,
      colInc: Int
    ): Iterable[Option[Mark]] =
      for {
        i <- 0 to 2
      } yield value(row0 + rowInc * i)(col0 + colInc * i)
  }
  object Board {
    final val empty = new Board(Vector.fill(3)(Vector.fill(3)(None)))

    def fromChars(
      first: Iterable[Char],
      second: Iterable[Char],
      third: Iterable[Char]
    ): Option[Board] =
      if (first.size != 3 || second.size != 3 || third.size != 3) None
      else {
        def toMark(char: Char): Option[Mark] =
          if (char.toLower == 'x') Some(Mark.X)
          else if (char.toLower == 'o') Some(Mark.O)
          else None

        Some(
          new Board(
            Vector(
              first.map(toMark).toVector,
              second.map(toMark).toVector,
              third.map(toMark).toVector
            )
          )
        )
      }
  }

  val TestBoard = Board
    .fromChars(
      List(' ', 'O', 'X'),
      List('O', 'X', 'O'),
      List('X', ' ', ' ')
    )
    .get
    .render

  /**
   * EXERCISE
   *
   * Implement a game of tic-tac-toe, where the player gets to play against a
   * computer opponent.
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    printLine(TestBoard).exitCode
}
