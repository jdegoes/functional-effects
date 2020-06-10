package net.degoes.zio

import zio._

object ParallelFib extends App {
  import zio.console._

  /**
   * EXERCISE
   *
   * Rewrite this implementation to compute nth fibanacci number in parallel.
   */
  def fib(n: Int): UIO[BigInt] = {
    def loop(n: Int, original: Int): UIO[BigInt] =
      if (n <= 1) UIO(n)
      else
        UIO.unit.flatMap { _ =>
          (loop(n - 1, original) zipWith loop(n - 2, original))(_ + _)
        }

    loop(n, n)
  }

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    for {
      _ <- putStrLn(
            "What number of the fibonacci sequence should we calculate?"
          )
      n <- getStrLn.orDie.flatMap(input => ZIO(input.toInt)).eventually
      f <- fib(n)
      _ <- putStrLn(s"fib(${n}) = ${f}")
    } yield 0
}

object AlarmAppImproved extends App {
  import zio.console._
  import zio.duration._
  import java.io.IOException
  import java.util.concurrent.TimeUnit

  lazy val getAlarmDuration: ZIO[Console, IOException, Duration] = {
    def parseDuration(input: String): IO[NumberFormatException, Duration] =
      ZIO
        .effect(
          Duration((input.toDouble * 1000.0).toLong, TimeUnit.MILLISECONDS)
        )
        .refineToOrDie[NumberFormatException]

    val fallback = putStrLn("You didn't enter the number of seconds!") *> getAlarmDuration

    for {
      _        <- putStrLn("Please enter the number of seconds to sleep: ")
      input    <- getStrLn
      duration <- parseDuration(input) orElse fallback
    } yield duration
  }

  /**
   * EXERCISE
   *
   * Create a program that asks the user for a number of seconds to sleep,
   * sleeps the specified number of seconds using ZIO.sleep(d), concurrently
   * prints a dot every second that the alarm is sleeping for, and then
   * prints out a wakeup alarm message, like "Time to wakeup!!!".
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    ???
}

object ComputePi extends App {
  import zio.random._
  import zio.console._
  import zio.clock._
  import zio.duration._
  import zio.stm._

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
   * EXERCISE
   *
   * Build a multi-fiber program that estimates the value of `pi`. Print out
   * ongoing estimates continuously until the estimation is complete.
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    def insideDelta(point: (Double, Double)): Int =
      (if (insideCircle(point._1, point._2)) 1 else 0)

    def makeEstimator(piState: PiState) = {
      import piState.{ inside, total }

      for {
        point <- randomPoint
        _     <- total.update(_ + 1) *> inside.update(_ + insideDelta(point))
      } yield ()
    }

    def makeWorker(piState: PiState) = makeEstimator(piState).forever

    def makeStatusReporter(piState: PiState) =
      (for {
        total  <- piState.total.get
        inside <- piState.inside.get
        _      <- putStrLn(estimatePi(inside, total).toString)
        _      <- ZIO.sleep(1.second)
      } yield ()).forever

    for {
      id      <- ZIO.fiberId
      dumps   <- Fiber.dump
      _       <- ZIO.foreach(dumps) { _.prettyPrintM.flatMap(putStrLn(_)) }
      _       <- putStrLn(s"${id.toString()}: Enter any input to exit...")
      piState <- (Ref.make(0L) zipWith Ref.make(0L))(PiState(_, _))
      workers <- ZIO.forkAll(List.fill(4)(makeWorker(piState)))
      fiber   <- makeStatusReporter(piState).fork.map(_ zip workers)
      _       <- getStrLn.orDie *> fiber.interrupt
    } yield 0
  }
}

object StmSwap extends App {
  import zio.console._
  import zio.stm._

  /**
   * EXERCISE
   *
   * Demonstrate the following code does not reliably swap two values in the
   * presence of concurrency.
   */
  def exampleRef = {
    def swap[A](ref1: Ref[A], ref2: Ref[A]): UIO[Unit] =
      for {
        v1 <- ref1.get
        v2 <- ref2.get
        _  <- ref2.set(v1)
        _  <- ref1.set(v2)
      } yield ()

    for {
      ref1   <- Ref.make(100)
      ref2   <- Ref.make(0)
      fiber1 <- swap(ref1, ref2).repeat(Schedule.recurs(100)).fork
      fiber2 <- swap(ref2, ref1).repeat(Schedule.recurs(100)).fork
      _      <- (fiber1 zip fiber2).join
      value  <- (ref1.get zipWith ref2.get)(_ + _)
    } yield value
  }

  /**
   * EXERCISE
   *
   * Using `STM`, implement a safe version of the swap function.
   */
  def exampleStm = {
    def swap[A](ref1: TRef[A], ref2: TRef[A]): UIO[Unit] =
      (for {
        v1 <- ref1.get
        v2 <- ref2.get
        _  <- ref2.set(v1)
        _  <- ref1.set(v2)
      } yield ()).commit

    for {
      ref1   <- TRef.make(100).commit
      ref2   <- TRef.make(0).commit
      fiber1 <- swap(ref1, ref2).repeat(Schedule.recurs(100)).fork
      fiber2 <- swap(ref2, ref1).repeat(Schedule.recurs(100)).fork
      _      <- (fiber1 zip fiber2).join
      value  <- (ref1.get zipWith ref2.get)(_ + _).commit
    } yield value
  }

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    exampleStm.map(_.toString).flatMap(putStrLn) as 0
}

object StmLock extends App {
  import zio.console._
  import zio.stm._

  /**
   * EXERCISE
   *
   * Using STM, implement a simple binary lock by implementing the creation,
   * acquisition, and release methods.
   */
  class Lock private (tref: TRef[Boolean]) {
    def acquire: UIO[Unit] =
      (for {
        locked <- tref.get
        _      <- STM.check(locked == false)
        _      <- tref.set(true)
      } yield ()).commit

    def release: UIO[Unit] = tref.set(false).commit
  }
  object Lock {
    def make: UIO[Lock] =
      (for {
        tref <- TRef.make(false)
      } yield new Lock(tref)).commit
  }

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    for {
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
    } yield 0
}

object StmQueue extends App {
  import zio.console._
  import zio.stm._
  import scala.collection.immutable.{ Queue => ScalaQueue }

  /**
   * EXERCISE
   *
   * Using STM, implement a async queue with double back-pressuring.
   */
  class Queue[A] private (capacity: Int, queue: TRef[ScalaQueue[A]]) {
    def take: UIO[A] =
      (for {
        tuple <- queue.get.map(_.dequeueOption).collect {
                  case Some(tuple) => tuple
                }
        _ <- queue.set(tuple._2)
      } yield tuple._1).commit

    def offer(a: A): UIO[Unit] =
      (for {
        q <- queue.get
        _ <- STM.check(q.length < capacity)
        _ <- queue.set(q.enqueue(a))
      } yield ()).commit
  }
  object Queue {
    def bounded[A](capacity: Int): UIO[Queue[A]] =
      TRef.make(ScalaQueue.empty[A]).map(new Queue(capacity, _)).commit
  }

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    for {
      queue <- Queue.bounded[Int](10)
      _     <- ZIO.foreach(0 to 100)(i => queue.offer(i)).fork
      _ <- ZIO.foreach(0 to 100)(
            _ => queue.take.flatMap(i => putStrLn(s"Got: ${i}"))
          )
    } yield 0
}

object StmLunchTime extends App {
  import zio.console._
  import zio.stm._

  /**
   * EXERCISE
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
      case object Full     extends State
    }
  }

  /**
   * EXERCISE
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
   * EXERCISE
   *
   * Using STM, implement a method that feeds a single attendee.
   */
  def feedAttendee(t: Table, a: Attendee): STM[Nothing, Unit] =
    for {
      index <- t.findEmptySeat.collect { case Some(index) => index }
      _     <- t.takeSeat(index) *> a.feed *> t.vacateSeat(index)
    } yield ()

  /**
   * EXERCISE
   *
   * Using STM, implement a method that feeds only the starving attendees.
   */
  def feedStarving(table: Table, list: List[Attendee]): UIO[Unit] =
    ???

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    val Attendees = 100
    val TableSize = 5

    for {
      attendees <- ZIO.foreach(0 to Attendees)(
                    i =>
                      TRef
                        .make[Attendee.State](Attendee.State.Starving)
                        .map(Attendee(_))
                        .commit
                  )
      table <- TArray
                .fromIterable(List.fill(TableSize)(false))
                .map(Table(_))
                .commit
      _ <- feedStarving(table, attendees)
    } yield 0
  }
}

object StmPriorityQueue extends App {
  import zio.console._
  import zio.stm._
  import zio.duration._

  /**
   * EXERCISE
   *
   * Using STM, design a priority queue, where smaller integers are assumed
   * to have higher priority than greater integers.
   */
  class PriorityQueue[A] private (
    minLevel: TRef[Option[Int]],
    map: TMap[Int, TQueue[A]]
  ) {
    def offer(a: A, priority: Int): STM[Nothing, Unit] =
      for {
        option <- map.get(priority)
        queue <- option match {
                  case None =>
                    for {
                      tqueue <- TQueue.make[A](Int.MaxValue)
                      _      <- map.put(priority, tqueue)
                    } yield tqueue

                  case Some(value) => STM.succeed(value)
                }
        _ <- queue.offer(a)
        _ <- minLevel.update(
              option => Some(option.fold(priority)(_ min priority))
            )
      } yield ()

    def take: STM[Nothing, A] = {
      val setMinLevel =
        map.keys.flatMap(list => minLevel.set(list.sorted.headOption))

      for {
        level  <- minLevel.get.collect { case Some(level) => level }
        tqueue <- map.get(level).collect { case Some(tqueue) => tqueue }
        head   <- tqueue.take
        size   <- tqueue.size
        _      <- if (size == 0) map.delete(level) *> setMinLevel else STM.unit
      } yield head
    }
  }
  object PriorityQueue {
    def make[A]: STM[Nothing, PriorityQueue[A]] =
      for {
        minLevel <- TRef.make(Option.empty[Int])
        map      <- TMap.empty[Int, TQueue[A]]
      } yield new PriorityQueue(minLevel, map)
  }

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    (for {
      _     <- putStrLn("Enter any key to exit...")
      queue <- PriorityQueue.make[String].commit
      lowPriority = ZIO.foreach(0 to 100) { i =>
        ZIO.sleep(1.millis) *> queue
          .offer(s"Offer: ${i} with priority 3", 3)
          .commit
      }
      highPriority = ZIO.foreach(0 to 100) { i =>
        ZIO.sleep(2.millis) *> queue
          .offer(s"Offer: ${i} with priority 0", 0)
          .commit
      }
      _ <- ZIO.forkAll(List(lowPriority, highPriority)) *> queue.take.commit
            .flatMap(putStrLn(_))
            .forever
            .fork *>
            getStrLn
    } yield 0).fold(_ => 1, _ => 0)
}

object StmReentrantLock extends App {
  import zio.console._
  import zio.stm._

  private final case class WriteLock(
    writeCount: Int,
    readCount: Int,
    fiberId: FiberId
  )
  private final class ReadLock private (readers: Map[Fiber.Id, Int]) {
    def total: Int = readers.values.sum

    def noOtherHolder(fiberId: FiberId): Boolean =
      readers.size == 0 || (readers.size == 1 && readers.contains(fiberId))

    def readLocks(fiberId: FiberId): Int =
      readers.get(fiberId).fold(0)(identity)

    def adjust(fiberId: FiberId, adjust: Int): ReadLock = {
      val total = readLocks(fiberId)

      val newTotal = total + adjust

      new ReadLock(
        readers =
          if (newTotal == 0) readers - fiberId
          else readers.updated(fiberId, newTotal)
      )
    }
  }
  private object ReadLock {
    val empty: ReadLock = new ReadLock(Map())

    def apply(fiberId: Fiber.Id, count: Int): ReadLock =
      if (count <= 0) empty else new ReadLock(Map(fiberId -> count))
  }

  /**
   * EXERCISE
   *
   * Using STM, implement a reentrant read/write lock.
   */
  class ReentrantReadWriteLock(data: TRef[Either[ReadLock, WriteLock]]) {
    def writeLocks: UIO[Int] = data.get.map(_.fold(_ => 0, _.writeCount)).commit

    def writeLocked: UIO[Boolean] = writeLocks.map(_ > 0)

    def readLocks: UIO[Int] = data.get.map(_.fold(_.total, _.readCount)).commit

    def readLocked: UIO[Boolean] = readLocks.map(_ > 0)

    val read: Managed[Nothing, Int] = ???

    val write: Managed[Nothing, Int] = ???
  }
  object ReentrantReadWriteLock {
    def make: UIO[ReentrantReadWriteLock] =
      TRef
        .make[Either[ReadLock, WriteLock]](Left(ReadLock.empty))
        .map(tref => new ReentrantReadWriteLock(tref))
        .commit
  }

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = ???
}

object StmDiningPhilosophers extends App {
  import zio.console._
  import zio.stm._

  sealed trait Fork
  val Fork = new Fork {}

  final case class Placement(
    left: TRef[Option[Fork]],
    right: TRef[Option[Fork]]
  )

  final case class Roundtable(seats: Vector[Placement])

  /**
   * EXERCISE
   *
   * Using STM, implement the logic of a philosopher to take not one fork, but
   * both forks when they are both available.
   */
  def takeForks(
    left: TRef[Option[Fork]],
    right: TRef[Option[Fork]]
  ): STM[Nothing, (Fork, Fork)] =
    ???

  def putForks(left: TRef[Option[Fork]], right: TRef[Option[Fork]])(
    tuple: (Fork, Fork)
  ) = {
    val (leftFork, rightFork) = tuple

    right.set(Some(rightFork)) *> left.set(Some(leftFork))
  }

  def setupTable(size: Int): ZIO[Any, Nothing, Roundtable] = {
    val makeFork = TRef.make[Option[Fork]](Some(Fork))

    (for {
      allForks0 <- STM.foreach(0 to size) { i =>
                    makeFork
                  }
      allForks = allForks0 ++ List(allForks0(0))
      placements = (allForks zip allForks.drop(1)).map {
        case (l, r) => Placement(l, r)
      }
    } yield Roundtable(placements.toVector)).commit
  }

  def eat(
    philosopher: Int,
    roundtable: Roundtable
  ): ZIO[Console, Nothing, Unit] = {
    val placement = roundtable.seats(philosopher)

    val left  = placement.left
    val right = placement.right

    for {
      forks <- takeForks(left, right).commit
      _     <- putStrLn(s"Philosopher ${philosopher} eating...")
      _     <- putForks(left, right)(forks).commit
      _     <- putStrLn(s"Philosopher ${philosopher} is done eating")
    } yield ()
  }

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    val count = 10

    def eaters(table: Roundtable): Iterable[ZIO[Console, Nothing, Unit]] =
      (0 to count).map { index =>
        eat(index, table)
      }

    for {
      table <- setupTable(count)
      fiber <- ZIO.forkAll(eaters(table))
      _     <- fiber.join
      _     <- putStrLn("All philosophers have eaten!")
    } yield 0
  }
}
