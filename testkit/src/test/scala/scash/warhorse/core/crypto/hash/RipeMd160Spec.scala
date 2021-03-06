package scash.warhorse.core.crypto.hash

import scash.warhorse.core.crypto.hash.RipeMd160._
import scash.warhorse.gen
import scash.warhorse.util.failure
import scash.warhorse.core._
import scodec.bits.ByteVector
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
    testM("Compare")(
      check(Gen.anyString)(msg => assert(msg.hash[RipeMd160])(equalTo(ByteVector.view(msg.getBytes).hash[RipeMd160])))
    ),
    suite("Deterministic")(
      test("0")(
        assert("0".hash[RipeMd160].bytes)(
          equalTo(ByteVector.fromValidHex("0xba5ed015715da74cf1e87230ba73d4855edaf6f6"))
        )
      ),
      test("empty str")(
        assert("".hash[RipeMd160].bytes)(
          equalTo(ByteVector.fromValidHex("0x9c1185a5c5e9fc54612808977ee8f548b2258d31"))
        )
      ),
      test("message")(
        assert("very deterministic message".hash[RipeMd160].bytes)(
          equalTo(ByteVector.fromValidHex("0xbfbe0bd1d23b43b2cbaaf3f3c4ab40f9f34c20ef"))
        )
      )
    )
  )
}
