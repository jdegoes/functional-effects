package net.degoes.zio

import zio._
import zio.test._
import zio.test.TestAspect.{ ignore, timeout }

object WorkshopSpec extends ZIOSpecDefault {

  def spec = suite("WorkshopSpec")()
}
