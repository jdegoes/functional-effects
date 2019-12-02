package net.degoes.zio

import zio._

object HelloWorld extends App {
  import zio.console._

  /**
    * EXERCISE 1
    *
    * Implement a simple "Hello World" program using the effect returned by `putStrLn`.
    */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    ZIO.succeed(0)
}

object ErrorConversion extends App {
  val StdInputFailed = 1

  import zio.console._

  val failed =
    putStrLn("About to fail...") *>
      ZIO.fail("Uh oh!") *>
      putStrLn("This will NEVER be printed!")

  /**
    * EXERCISE 2
    *
    * Using `ZIO#orElse` or `ZIO#fold`, have the `run` function compose the
    * preceding `failed` effect into the effect that `run` returns.
    */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = ???
}

object PromptName extends App {
  val StdInputFailed = 1

  import zio.console._

  /**
    * EXERCISE 3
    *
    * Implement a simple program that asks the user for their name (using
    * `getStrLn`), and then prints it out to the user (using `putStrLn`).
    */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = ???
}

object ZIOTypes {
  type ??? = Nothing

  /**
    * EXERCISE 4
    *
    * Provide definitions for the ZIO type aliases below.
    */
  type Task[+A] = ???
  type UIO[+A] = ???
  type RIO[-R, +A] = ???
  type IO[+E, +A] = ???
  type URIO[-R, +A] = ???
}

object NumberGuesser extends App {
  import zio.console._
  import zio.random._

  def analyzeAnswer(random: Int, guess: String) =
    if (random.toString == guess.trim) putStrLn("You guessed correctly!")
    else putStrLn(s"You did not guess correctly. The answer was ${random}")

  /**
    * EXERCISE 5
    *
    * Choose a random number (using `nextInt`), and then ask the user to guess
    * the number, feeding their response to `analyzeAnswer`, above.
    */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    ???
}

object AlarmApp extends App {
  import zio.console._
  import zio.duration._
  import java.io.IOException

  /**
    * EXERCISE 6
    *
    * Create an effect that will get a `Duration` from the user, by prompting
    * the user to enter a decimal number of seconds.
    */
  lazy val getAlarmDuration: ZIO[Console, IOException, Duration] = {
    def parseDuration(input: String): IO[NumberFormatException, Duration] =
      ???

    def fallback(input: String): ZIO[Console, IOException, Duration] =
      ???

    ???
  }

  /**
    * EXERCISE 7
    *
    * Create a program that asks the user for a number of seconds to sleep,
    * sleeps the specified number of seconds, and then prints out a wakeup
    * alarm message.
    */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    ???
}

object Cat extends App {
  import zio.console._
  import zio.blocking._
  import java.io.IOException

  /**
    * EXERCISE 8
    *
    * Implement a function to read a file on the blocking thread pool, storing
    * the result into a string.
    */
  def readFile(file: String): ZIO[Blocking, IOException, String] = ???

  /**
    * EXERCISE 9
    *
    * Implement a version of the command-line utility "cat", which dumps the
    * contents of the specified file to standard output.
    */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    ???
}

object CatIncremental extends App {
  import zio.console._
  import zio.blocking._
  import java.io._

  /**
    * EXERCISE 10
    *
    * Implement all missing methods of `FileHandle`. Be sure to do all work on
    * the blocking thread pool.
    */
  final case class FileHandle private (private val is: InputStream) {
    final def close: ZIO[Blocking, IOException, Unit] = ???

    final def read: ZIO[Blocking, IOException, Option[Chunk[Byte]]] = ???
  }
  object FileHandle {
    final def open(file: String): ZIO[Blocking, IOException, FileHandle] = ???
  }

  /**
    * EXERCISE 11
    *
    * Implement an incremental version of the `cat` utility, using `ZIO#bracket`
    * or `ZManaged` to ensure the file is closed in the event of error or
    * interruption.
    */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    ???
}

object ComputePi extends App {
  import zio.random._
  import zio.console._

  /**
    * Some state to keep track of all points inside a circle,
    * and total number of points.
    */
  final case class PiState(
      inside: Ref[Long],
      total: Ref[Long]
  )

  /**
    * A function to estimate pi.
    */
  def estimatePi(inside: Long, total: Long): Double =
    (inside.toDouble / total.toDouble) * 4.0

  /**
    * A helper function that determines if a point lies in
    * a circle of 1 radius.
    */
  def insideCircle(x: Double, y: Double): Boolean =
    Math.sqrt(x * x + y * y) <= 1.0

  /**
    * An effect that computes a random (x, y) point.
    */
  val randomPoint: ZIO[Random, Nothing, (Double, Double)] =
    nextDouble zip nextDouble

  /**
    * EXERCISE 12
    *
    * Build a multi-fiber program that estimates the value of `pi`. Print out
    * ongoing estimates continuously until the estimation is complete.
    */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    ???
}

object StmSwap extends App {
  import zio.console._
  import zio.stm._

  /**
    * EXERCISE 13
    *
    * Demonstrate the following code does not reliably swap two values in the
    * presence of concurrency.
    */
  def exampleRef = {
    def swap[A](ref1: Ref[A], ref2: Ref[A]): UIO[Unit] =
      for {
        v1 <- ref1.get
        v2 <- ref2.get
        _ <- ref2.set(v1)
        _ <- ref1.set(v2)
      } yield ()

    for {
      ref1 <- Ref.make(100)
      ref2 <- Ref.make(0)
      fiber1 <- swap(ref1, ref2).repeat(Schedule.recurs(100)).fork
      fiber2 <- swap(ref2, ref1).repeat(Schedule.recurs(100)).fork
      _ <- (fiber1 zip fiber2).join
      value <- (ref1.get zipWith ref2.get)(_ + _)
    } yield value
  }

  /**
    * EXERCISE 14
    *
    * Using `STM`, implement a safe version of the swap function.
    */
  def exampleStm = {
    def swap[A](ref1: TRef[A], ref2: TRef[A]): UIO[Unit] = ???

    for {
      ref1 <- TRef.make(100).commit
      ref2 <- TRef.make(0).commit
      fiber1 <- swap(ref1, ref2).repeat(Schedule.recurs(100)).fork
      fiber2 <- swap(ref2, ref1).repeat(Schedule.recurs(100)).fork
      _ <- (fiber1 zip fiber2).join
      value <- (ref1.get zipWith ref2.get)(_ + _).commit
    } yield value
  }

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    exampleRef.map(_.toString).flatMap(putStrLn).fold(_ => 1, _ => 0)
}

object StmLock extends App {
  import zio.console._
  import zio.stm._

  /**
    * EXERCISE 15
    *
    * Using STM, implement a simple binary lock by implementing the creation,
    * acquisition, and release methods.
    */
  class Lock private (tref: TRef[Boolean]) {
    def acquire: UIO[Unit] = ???
    def release: UIO[Unit] = ???
  }
  object Lock {
    def make: UIO[Lock] = ???
  }

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    (for {
      lock <- Lock.make
      fiber1 <- lock.acquire
        .bracket_(lock.release)(putStrLn("Bob  : I have the lock!"))
        .repeat(Schedule.recurs(10))
        .fork
      fiber2 <- lock.acquire
        .bracket_(lock.release)(putStrLn("Sarah: I have the lock!"))
        .repeat(Schedule.recurs(10))
        .fork
      _ <- (fiber1 zip fiber2).join
    } yield 0) orElse ZIO.succeed(1)
}

object StmLunchTime extends App {
  import zio.console._
  import zio.stm._

  /**
    * EXERCISE 16
    *
    * Using STM, implement the missing methods of Attendee.
    */
  final case class Attendee(state: TRef[Attendee.State]) {
    import Attendee.State._

    def isStarving: STM[Nothing, Boolean] = ???

    def feed: STM[Nothing, Unit] = ???
  }
  object Attendee {
    sealed trait State
    object State {
      case object Starving extends State
      case object Full extends State
    }
  }

  /**
    * EXERCISE 17
    *
    * Using STM, implement the missing methods of Table.
    */
  final case class Table(seats: TArray[Boolean]) {
    def findEmptySeat: STM[Nothing, Option[Int]] =
      seats
        .fold[(Int, Option[Int])]((0, None)) {
          case ((index, z @ Some(_)), _) => (index + 1, z)
          case ((index, None), taken) =>
            (index + 1, if (taken) None else Some(index))
        }
        .map(_._2)

    def takeSeat(index: Int): STM[Nothing, Unit] = ???
    def vacateSeat(index: Int): STM[Nothing, Unit] = ???
  }

  /**
    * EXERCISE 18
    *
    * Using STM, implement a method that feeds a single attendee.
    */
  def feedAttendee(t: Table, a: Attendee): STM[Nothing, Unit] =
    ???

  /**
    * EXERCISE 19
    *
    * Using STM, implement a method that feeds only the starving attendees.
    */
  def feedStarving(table: Table, list: List[Attendee]): UIO[Unit] =
    ???

  /**
    * EXERCISE 20
    *
    * Construct a table and starving attendees and feed them.
    */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    ???
}

object Hangman extends App {
  import zio.console._
  import zio.random._
  import java.io.IOException

  /**
    * EXERCISE 21
    *
    * Implement an effect that gets a single, lower-case character from
    * the user.
    */
  lazy val getChoice: ZIO[Console, IOException, Char] = ???

  /**
    * EXERCISE 22
    *
    * Implement an effect that prompts the user for their name, and
    * returns it.
    */
  lazy val getName: ZIO[Console, IOException, String] = ???

  /**
    * EXERCISE 23
    *
    * Implement an effect that chooses a random word from the dictionary.
    */
  lazy val chooseWord: ZIO[Random, Nothing, String] = ???

  /**
    * EXERCISE 24
    *
    * Implement the main game loop, which gets choices from the user until
    * the game is won or lost.
    */
  def gameLoop(ref: Ref[State]): ZIO[Console, IOException, Unit] = ???

  def renderState(state: State): ZIO[Console, Nothing, Unit] = {

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

    putStrLn(text)
  }

  final case class State(name: String, guesses: Set[Char], word: String) {
    final def failures: Int = (guesses -- word.toSet).size

    final def playerLost: Boolean = failures > 10

    final def playerWon: Boolean = (word.toSet -- guesses).isEmpty

    final def addChar(char: Char): State = copy(guesses = guesses + char)
  }

  sealed trait GuessResult
  object GuessResult {
    case object Won extends GuessResult
    case object Lost extends GuessResult
    case object Correct extends GuessResult
    case object Incorrect extends GuessResult
    case object Unchanged extends GuessResult
  }

  def guessResult(oldState: State, newState: State, char: Char): GuessResult =
    if (oldState.guesses.contains(char)) GuessResult.Unchanged
    else if (newState.playerWon) GuessResult.Won
    else if (newState.playerLost) GuessResult.Lost
    else if (oldState.word.contains(char)) GuessResult.Correct
    else GuessResult.Incorrect

  /**
    * EXERCISE 25
    *
    * Implement hangman using `Dictionary.Dictionary` for the words,
    * and the above helper functions.
    */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    ???
}

/**
  * GRADUATION PROJECT
  *
  * Implement a game of tic tac toe using ZIO, then develop unit tests to
  * demonstrate its correctness and testability.
  */
object TicTacToe extends App {
  import zio.console._

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
        row <- (row0 to (row0 + rowInc * 2))
        col <- (col0 to (col0 + colInc * 2))
      } yield value(row)(col)
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
    * The entry point to the game will be here.
    */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    putStrLn(TestBoard) as 0
}
