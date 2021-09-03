package net.degoes.zio

import zio._

object AccessEnvironment extends App {
  import zio.Console._

  final case class Config(server: String, port: Int)

  /**
   * EXERCISE
   *
   * Using `ZIO.access`, access a `Config` type from the environment, and
   * extract the `server` field from it.
   */
  val accessServer: ZIO[Config, Nothing, String] = ???

  /**
   * EXERCISE
   *
   * Using `ZIO.access`, access a `Config` type from the environment, and
   * extract the `port` field from it.
   */
  val accessPort: ZIO[Config, Nothing, Int] = ???

  def run(args: List[String]) = {
    val config = Config("localhost", 7878)

    (for {
      server <- accessServer
      port   <- accessPort
      _      <- UIO(println(s"Configuration: ${server}:${port}"))
    } yield ExitCode.success).provide(config)
  }
}

object ProvideEnvironment extends App {
  import zio.Console._

  final case class Config(server: String, port: Int)

  final case class DatabaseConnection() {
    def query(query: String): Task[Int] = Task(42)
  }

  val getServer: ZIO[Has[Config], Nothing, String] =
    ZIO.service[Config].map(_.server)

  val useDatabaseConnection: ZIO[Has[DatabaseConnection], Throwable, Int] =
    ZIO.serviceWith[DatabaseConnection](_.query("SELECT * FROM USERS"))

  /**
   * EXERCISE
   *
   * Compose both the `getServer` and `useDatabaseConnection` effects together.
   * In order to do this successfully, you will have to use `ZIO#provide` to
   * give them the environment that they need in order to run, then they can
   * be composed because their environment types will be compatible.
   */
  def run(args: List[String]) = {
    val config = Config("localhost", 7878)

    ???
  }
}

object CakeEnvironment extends App {
  import zio.Console._
  import java.io.IOException

  type MyFx = Logging with Files

  trait Logging {
    val logging: Logging.Service
  }
  object Logging {
    trait Service {
      def log(line: String): UIO[Unit]
    }
    def log(line: String) = ZIO.accessM[Logging](_.logging.log(line))
  }
  trait Files {
    val files: Files.Service
  }
  object Files {
    trait Service {
      def read(file: String): IO[IOException, String]
    }
    def read(file: String) = ZIO.accessM[Files](_.files.read(file))
  }

  val effect =
    for {
      file <- Files.read("build.sbt")
      _    <- Logging.log(file)
    } yield ()

  /**
   * EXERCISE
   *
   * Run `effect` by using `ZIO#provide` to give it what it needs. You will
   * have to build a value (the environment) of the required type
   * (`Files with Logging`).
   */
  def run(args: List[String]) =
    effect
      .provide(???)
      .exitCode
}

/**
 * Although there are no requirements on how the ZIO environment may be used to pass context
 * around in an application, for easier composition, ZIO includes a data type called `Has`,
 * which represents a map from a type to an object that satisfies that type. Sometimes, this is
 * called a "Has Map" or more precisely, a "type-indexed map".
 */
object HasMap extends App {
  trait Logging
  object Logging extends Logging

  trait Database
  object Database extends Database

  trait Cache
  object Cache extends Cache

  val hasLogging = Has(Logging: Logging)

  val hasDatabase = Has(Database: Database)

  val hasCache = Has(Cache: Cache)

  /**
   * EXERCISE
   *
   * Using the `++` operator on `Has`, combine the three maps (`hasLogging`, `hasDatabase`, and
   * `hasCache`) into a single map that has all three objects.
   */
  val allThree: Has[Database] with Has[Cache] with Has[Logging] = ???

  /**
   * EXERCISE
   *
   * Using `Has#get`, which can retrieve an object stored in the map, retrieve the logging,
   * database, and cache objects from `allThree`. Note that you will have to specify the type
   * parameter, as it cannot be inferred (the map needs to know which of the objects you want to
   * retrieve, and that can be specified only by type).
   */
  lazy val logging  = ???
  lazy val database = ???
  lazy val cache    = ???

  def run(args: List[String]) = ???
}

/**
 * In ZIO, layers are essentially wrappers around constructors for services in your application.
 * Services provide functionality like persistence or logging or authentication, and they are used
 * by business logic.
 *
 * A layer is a lot like a constructor, except that a constructor can only "construct" a single
 * service. Layers can construct many services. In addition, this construction can be resourceful,
 * and even asynchronous or concurrent.
 *
 * Layers bring more power and compositionality to constructors. Although you don't have to use
 * them to benefit from ZIO environment, they can make it easier to assemble applications out of
 * modules without having to do any wiring, and with great support for testability.
 *
 * ZIO services like `Clock` and `System` are all designed to work well with layers.
 */
object LayerEnvironment extends App {
  import zio.Console._
  import java.io.IOException

  type MyFx = Has[Logging] with Has[Files]

  trait Files {
    def read(file: String): IO[IOException, String]
  }
  object Files extends Accessible[Files] {

    /**
     * EXERCISE
     *
     * Using `ZLayer.succeed`, create a layer that implements the `Files`
     * service.
     */
    val live: ZLayer[Any, Nothing, Has[Files]] = ???

    def read(file: String) = Files(_.read(file))
  }

  trait Logging {
    def log(line: String): UIO[Unit]
  }
  object Logging extends Accessible[Logging] {

    /**
     * EXERCISE
     *
     * Using `ZLayer.fromFunction`, create a layer that requires `Console`
     * and uses the console to provide a logging service.
     */
    val live: ZLayer[Has[Console], Nothing, Has[Logging]] = ???

    def log(line: String) = Logging(_.log(line))
  }

  /**
   * EXERCISE
   *
   * Discover the inferred type of `effect`, and write it out explicitly.
   */
  val effect =
    for {
      file <- Files.read("build.sbt")
      _    <- Logging.log(file)
    } yield ()

  def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    /**
      * EXERCISE 
      * 
      * Create a layer using `ZLayer.wire` and specifying all the pieces that go into the layer.
      */
    val fullLayer: ZLayer[Any, Nothing, Has[Files] with Has[Logging]] = ??? // ZLayer.wire[Has[Files] with Has[Logging]](???)

    /**
      * EXERCISE
      * 
      * Using `ZIO#inject`, inject the full layer into the effect to remove its dependencies.
      */
    val effect2: ZIO[Any,IOException,Unit] = ???

    /**
     * EXERCISE
     *
     * Run `effect` by using `ZIO#inject` to give it what it needs. You will have
     * to give it a list of services that implement its required dependencies.
     */
    // effect
    //   .inject(???)
    //   .exitCode
    ???
  }
}
