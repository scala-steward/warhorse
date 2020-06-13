package scash.warhorse.core.number

import scash.warhorse.gen
import scash.warhorse.core._

import zio.test.DefaultRunnableSpec
import zio.test._
import zio.test.Assertion._
import scodec.bits._

object Uint5Spec extends DefaultRunnableSpec {
  val spec = suite("Uint5Spec")(
    test("cast b")(assert(Uint5.cast('b'))(equalTo(Uint5(2.toByte)))),
    test("cast i")(assert(Uint5.cast('i'))(equalTo(Uint5(9.toByte)))),
    test("cast t")(assert(Uint5.cast('t'))(equalTo(Uint5(20.toByte)))),
    suite("serde")(
      test("toBin Max")(assert(Uint5.max.bits)(equalTo(bin"11111"))),
      test("toBin Zero")(assert(Uint5.zero.bits)(equalTo(bin"00000"))),
      testM("symmetry")(check(gen.uint5)(u => assert(u.bits.decode_[Uint5])(equalTo(u))))
    )
  )

}
