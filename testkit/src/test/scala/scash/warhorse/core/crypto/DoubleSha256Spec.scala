package scash.warhorse.core.crypto

import scash.warhorse.TestUtil._
import scash.warhorse.core._
import scash.warhorse.core.crypto.DoubleSha256._
import scash.warhorse.gen
import zio.test.Assertion.equalTo
import zio.test.{ DefaultRunnableSpec, _ }

object DoubleSha256Spec extends DefaultRunnableSpec {
  val spec = suite("DoubleSha256Spec")(
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
