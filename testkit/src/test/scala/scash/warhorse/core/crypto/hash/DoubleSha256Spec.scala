package scash.warhorse.core.crypto.hash

import scash.warhorse.util._
import scash.warhorse.core._
import scash.warhorse.core.crypto.hash.DoubleSha256._
import scash.warhorse.gen
import scodec.bits.ByteVector
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
    ),
    testM("Compare")(
      check(Gen.anyString)(msg =>
        assert(Hasher[DoubleSha256].hash(msg))(equalTo(Hasher[DoubleSha256].hash(ByteVector.view(msg.getBytes))))
      )
    ),
    suite("Deterministic")(
      test("0")(
        assert(Hasher[DoubleSha256].hash("0").bytes)(
          equalTo(ByteVector.fromValidHex("67050eeb5f95abf57449d92629dcf69f80c26247e207ad006a862d1e4e6498ff"))
        )
      ),
      test("empty str")(
        assert(Hasher[DoubleSha256].hash("").bytes)(
          equalTo(ByteVector.fromValidHex("5df6e0e2761359d30a8275058e299fcc0381534545f55cf43e41983f5d4c9456"))
        )
      ),
      test("message")(
        assert(Hasher[DoubleSha256].hash("very deterministic message").bytes)(
          equalTo(ByteVector.fromValidHex("82f5f11992206dd9ba85999aa0af4c3b739af7dca6ff8d70a8dc178b2192eb56"))
        )
      )
    )
  )
}
