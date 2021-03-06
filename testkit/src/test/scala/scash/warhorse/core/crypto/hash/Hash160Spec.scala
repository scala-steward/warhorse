package scash.warhorse.core.crypto.hash

import scash.warhorse.core._
import scash.warhorse.gen
import scash.warhorse.util._

import scodec.bits._
import zio.test.Assertion.equalTo
import zio.test._

object Hash160Spec extends DefaultRunnableSpec {
  val spec = suite("Hash160Spec")(
    testM("Hash160")(
      check(gen.byteVectorBounded(18, 21))(b =>
        if (b.size == 20) assert(b.decodeExact_[Hash160].bytes)(equalTo(b))
        else assert(b.decodeExact[Hash160])(failure)
      )
    ),
    testM("Compare")(
      check(Gen.anyString)(msg => assert(msg.hash[Hash160])(equalTo(ByteVector.view(msg.getBytes).hash[Hash160])))
    ),
    suite("Deterministic")(
      test("0")(
        assert("0".hash[Hash160])(
          equalTo(hex"0x9a44a0242cdfa06345a1d80a190cec35fc2c1caf".decode_[Hash160])
        )
      ),
      test("empty str")(
        assert("".hash[Hash160])(
          equalTo(hex"0xb472a266d0bd89c13706a4132ccfb16f7c3b9fcb".decode_[Hash160])
        )
      ),
      test("message")(
        assert("very deterministic message".hash[Hash160])(
          equalTo(hex"0x4659ecbdea82abb950b131729cb2370fa9b791b0".decode_[Hash160])
        )
      )
    )
  )
}
