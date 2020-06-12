package net.degoes.zio

import zio._
import zio.internal.Executor
import scala.concurrent.ExecutionContext

object PoolLocking extends App {
  import zio.console._

  lazy val dbPool: Executor = Executor.fromExecutionContext(1024)(ExecutionContext.global)

  /**
   * EXERCISE
   *
   * Using `ZIO#lock`, write an `onDatabase` combinator that runs the
   * specified effect on the database thread pool.
   */
  def onDatabase[R, E, A](zio: ZIO[R, E, A]): ZIO[R, E, A] = ???

  /**
   * EXERCISE
   *
   * Implement a combinator to print out thread information before and after
   * executing the specified effect.
   */
  def threadLogged[R, E, A](zio: ZIO[R, E, A]): ZIO[R, E, A] = {
    val log = ZIO.effectTotal {
      val thread = Thread.currentThread()

      val id        = thread.getId()
      val name      = thread.getName()
      val groupName = thread.getThreadGroup().getName()

      println(s"Thread($id, $name, $groupName)")
    }

    zio
  }

  /**
   * EXERCISE
   *
   * Use the `threadLogged` combinator around different effects below to
   * determine which threads are executing which effects.
   */
  def run(args: List[String]) =
    putStrLn("Main") *>
      onDatabase {
        putStrLn("Database") *>
          blocking.blocking {
            putStrLn("Blocking")
          } *>
          putStrLn("Database")
      } *>
      putStrLn("Main") *>
      ZIO.succeed(ExitCode.success)
}

object Sharding extends App {
  import zio.console._

  /**
   * EXERCISE
   *
   * Create N workers reading from a Queue, if one of them fails, then wait
   * for the other ones to process their current item, but terminate all the
   * workers.
   *
   * Return the first error, or never return, if there is no error.
   */
  def shard[R, E, A](
    queue: Queue[A],
    n: Int,
    worker: A => ZIO[R, E, Unit]
  ): ZIO[R, Nothing, E] = ???

  def run(args: List[String]) = {
    def makeWorker(ref: Ref[Int]): Int => ZIO[Console, String, Unit] =
      (work: Int) =>
        for {
          count <- ref.get
          _ <- if (count < 100) putStrLn(s"Worker is processing item ${work} after ${count}")
              else ZIO.fail(s"Uh oh, failed processing ${work} after ${count}")
          _ <- ref.update(_ + 1)
        } yield ()

    for {
      queue <- Queue.bounded[Int](100)
      ref   <- Ref.make(0)
      _     <- queue.offer(1).forever.fork
      error <- shard(queue, 10, makeWorker(ref))
      _     <- putStrLn(s"Failed with ${error}")
    } yield ExitCode.success
  }
}

object parallel_web_crawler {
  import zio.blocking._
  import zio.console._
  import zio.duration._
  import zio.clock._

  type Web = Has[Web.Service]
  object Web {
    trait Service {
      def getURL(url: URL): IO[Exception, String]
    }

    /**
     * EXERCISE
     *
     * Implement a layer for `Web` that uses the `effectBlockingIO` combinator
     * to safely wrap `Source.fromURL` into a functional effect.
     */
    val live: ZLayer[Blocking, Nothing, Web] = ???
  }

  /**
   * EXERCISE
   *
   * Using `ZIO.accessM`, delegate to the `Web` module's `getURL` function.
   */
  def getURL(url: URL): ZIO[Web, Exception, String] = ???

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
  ): ZIO[Web with Clock, Nothing, List[E]] = {
    val emptySet = ZIO.succeed(Set.empty[URL])

    def loop(seeds: Set[URL], ref: Ref[CrawlState[E]]): ZIO[Web with Clock, Nothing, Unit] =
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
    putStrLn("Hello World!").exitCode
}
