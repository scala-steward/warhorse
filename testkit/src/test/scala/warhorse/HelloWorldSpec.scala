package warhorse

import zio.test._
import zio.test.Assertion._
import zio.test.environment._

import Hello._

object HelloWorldSpec extends DefaultRunnableSpec {
  val spec = suite("HelloWorldSpec")(
    testM("sayHello correctly displays output") {
      for {
        _      <- sayHello
        output <- TestConsole.output
      } yield assert(output)(equalTo(Vector("Hello, World!\n")))
    }
  )
}
