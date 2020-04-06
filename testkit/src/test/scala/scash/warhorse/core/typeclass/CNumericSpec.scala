package scash.warhorse.core.typeclass

import scash.warhorse.core._
import scash.warhorse.core.number.Uint8

import zio.test.Assertion.equalTo
import zio.test.{ assert, suite, test, DefaultRunnableSpec }

object CNumericSpec extends DefaultRunnableSpec {
  //TODO: Add Laws
  val spec = suite("CNumericSpec")(
    test("add")(assert(Uint8(4) + BigInt(5))(equalTo(Uint8(9))))
  )
}
