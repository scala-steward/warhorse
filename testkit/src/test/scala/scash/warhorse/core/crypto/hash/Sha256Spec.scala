package scash.warhorse.core.crypto.hash

import scash.warhorse.core.crypto.hash.Sha256._
import scash.warhorse.gen
import scash.warhorse.util.failure
import scash.warhorse.core._
import zio.test.Assertion.equalTo
import zio.test._

object Sha256Spec extends DefaultRunnableSpec {
  val spec = suite("Sha256Spec")(
    testM("Sha256")(
      check(gen.byteVectorBounded(30, 33)) { b =>
        if (b.size == 32) assert(b.decodeExact_[Sha256].bytes)(equalTo(b))
        else assert(b.decodeExact[Sha256])(failure)
      }
    ),
    testM("Sha256B")(
      check(gen.byteVectorBounded(30, 33))(b =>
        if (b.size == 32) assert(b.decodeExact_[Sha256B].bytes)(equalTo(b))
        else assert(b.decodeExact[Sha256B])(failure)
      )
    )
  )
}
