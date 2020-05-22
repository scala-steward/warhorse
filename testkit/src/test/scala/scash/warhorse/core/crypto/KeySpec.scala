package scash.warhorse.core.crypto

import scash.warhorse.gen
import zio.test.DefaultRunnableSpec
import zio.test._
import scash.warhorse.util._
import scash.warhorse.core._
import scodec.bits.ByteVector

object KeySpec extends DefaultRunnableSpec {
  val spec = suite("KeySpec")(
    suite("PrivateKeySpec")(
      test("empty")(assert(ByteVector.empty.decode[PrivateKey])(failure)),
      test("fail zero")(assert(PrivateKey(PrivateKey.zero))(failure)),
      test("fail max")(assert(PrivateKey(PrivateKey.max))(failure)),
      testM("fail bytes")(check(gen.byteVectorN(33))(b => assert(PrivateKey(b))(failure)))
    ),
    suite("PublicKeySpec")(
      test("empty")(assert(ByteVector.empty.decode[PublicKey])(failure)),
      testM("invalid")(
        check(gen.byteVectorBounded(35, 70))(b => assert((0x05.toByte +: b).decode[PublicKey])(failure))
      ),
      testM("valid prefix invalid size compressed")(
        check(gen.byteVectorBounded(1, 31), Gen.elements(0x02.toByte, 0x03.toByte))((bb, b) =>
          assert((b +: bb).decode[PublicKey])(failure)
        )
      ),
      testM("valid prefix invalid size uncompressed")(
        check(gen.byteVectorBounded(1, 63))(b => assert((0x04.toByte +: b).decode[PublicKey])(failure))
      ),
      testM("invalid")(
        check(gen.byteVectorBounded(35, 70))(b => assert((0x05.toByte +: b).decode[PublicKey])(failure))
      ),
      testM("fail too small")(check(gen.byteVectorN(30))(b => assert(PublicKey(b))(failure))),
      testM("fail too large")(check(gen.byteVectorN(66))(b => assert(PublicKey(b))(failure)))
    )
  )
}
