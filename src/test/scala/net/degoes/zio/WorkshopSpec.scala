package net.degoes.zio

import zio.{ ExitCode, ZIO }
import zio.test._
import zio.test.environment._
import zio.test.Assertion._
import zio.test.TestAspect.{ ignore, timeout }

object WorkshopSpec extends DefaultRunnableSpec {
  import TicTacToe._

  def spec = suite("WorkshopSpec")()
}
