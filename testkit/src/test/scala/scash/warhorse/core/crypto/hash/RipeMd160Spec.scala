package scash.warhorse.core.crypto.hash

import scash.warhorse.core.crypto.hash.RipeMd160._
import scash.warhorse.gen
import scash.warhorse.util.failure
import scash.warhorse.core._
import zio.test.Assertion.equalTo

import zio.test._

object RipeMd160Spec extends DefaultRunnableSpec {
  val spec = suite("RipeMd160Spec")(
    testM("RipeMd160")(
      check(gen.byteVectorBounded(18, 21)) { b =>
        if (b.size == 20) assert(b.decodeExact_[RipeMd160].bytes)(equalTo(b))
        else assert(b.decodeExact[RipeMd160])(failure)
      }
    ),
    testM("RipeMd160B")(
      check(gen.byteVectorBounded(19, 21))(b =>
        if (b.size == 20) assert(b.decodeExact_[RipeMd160B].bytes)(equalTo(b))
        else assert(b.decodeExact[RipeMd160B])(failure)
      )
    ),
    testM("RipeMd160Sha256")(
      check(gen.byteVectorBounded(19, 21))(b =>
        if (b.size == 20) assert(b.decodeExact_[RipeMd160Sha256].bytes)(equalTo(b))
        else assert(b.decodeExact[RipeMd160Sha256])(failure)
      )
    ),
    testM("RipeMd160Sha256B")(
      check(gen.byteVectorBounded(19, 21))(b =>
        if (b.size == 20) assert(b.decodeExact_[RipeMd160Sha256B].bytes)(equalTo(b))
        else assert(b.decodeExact[RipeMd160Sha256B])(failure)
      )
    )
  )
}
