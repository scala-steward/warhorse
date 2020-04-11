package scash.warhorse.core.crypto

import scash.warhorse.core.SerdeUtil._
import scash.warhorse.core._
import scash.warhorse.core.crypto.Sha256._
import scash.warhorse.gen
import zio.test.Assertion.equalTo
import zio.test.{ DefaultRunnableSpec, _ }

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
    ),
    testM("DoubleSha256")(
      check(gen.byteVectorBounded(30, 33))(b =>
        if (b.size == 32) assert(b.decodeExact_[DoubleSha256].bytes)(equalTo(b))
        else assert(b.decodeExact[DoubleSha256])(failure)
      )
    ),
    testM("DoubleSha256B")(
      check(gen.byteVectorBounded(30, 33))(b =>
        if (b.size == 32) assert(b.decodeExact_[DoubleSha256B].bytes)(equalTo(b))
        else assert(b.decodeExact[DoubleSha256B])(failure)
      )
    )
  )
}
