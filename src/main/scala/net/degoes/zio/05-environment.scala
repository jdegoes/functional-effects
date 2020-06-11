package net.degoes.zio

import zio._

object AccessEnvironment extends App {
  import zio.console._

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
    } yield 0).provide(config)
  }
}

object ProvideEnvironment extends App {
  import zio.console._

  final case class Config(server: String, port: Int)

  final case class DatabaseConnection() {
    def query(query: String): Task[Int] = Task(42)
  }

  val getServer: ZIO[Config, Nothing, String] =
    ZIO.access[Config](_.server)

  def useDatabaseConnection: ZIO[DatabaseConnection, Throwable, Int] =
    ZIO.accessM[DatabaseConnection](_.query("SELECT * FROM USERS"))

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

object CustomEnvironment extends App {
  import zio.console._
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
    (for {
      file <- Files.read("build.sbt")
      _    <- Logging.log(file)
    } yield ())

  /**
   * EXERCISE
   *
   * Run `effect` by using `ZIO#provide` to give it what it needs. You will
   * have to build a value (the environment) of the required type
   * (`Files with Logging`).
   */
  def run(args: List[String]) =
    effect
      .provide(new Files with Logging {
        val logging =
          new Logging.Service {
            def log(line: String): UIO[Unit] = UIO(println(line))
          }
        val files =
          new Files.Service {
            def read(file: String): IO[IOException, String] =
              Task(scala.io.Source.fromFile(file).mkString)
                .refineToOrDie[IOException]
          }
      })
      .fold(_ => 1, _ => 0)
}
