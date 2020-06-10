package net.degoes.zio

import zio._

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
