package scash.warhorse.core.crypto.hash

import scash.warhorse.core.crypto.hash.Sha256._
import scash.warhorse.gen
import scash.warhorse.util.failure
import scash.warhorse.core._
import scodec.bits.ByteVector
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
    ),
    testM("Compare")(
      check(Gen.anyString)(msg =>
        assert(Hasher[Sha256].hash(msg))(equalTo(Hasher[Sha256].hash(ByteVector.view(msg.getBytes))))
      )
    ),
    suite("Deterministic")(
      test("0")(
        assert(Hasher[Sha256].hash("0").bytes)(
          equalTo(ByteVector.fromValidHex("5feceb66ffc86f38d952786c6d696c79c2dbc239dd4e91b46729d73a27fb57e9"))
        )
      ),
      test("empty str")(
        assert(Hasher[Sha256].hash("").bytes)(
          equalTo(ByteVector.fromValidHex("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"))
        )
      ),
      test("message")(
        assert(Hasher[Sha256].hash("very deterministic message").bytes)(
          equalTo(ByteVector.fromValidHex("eccb9f36b2bbb8c3a0d76274bce423ff7744c37ee4a5bb89ed9da4da7564ab1d"))
        )
      )
    )
  )
}
