package scash.warhorse.core.crypto

import org.scash.secp256k1
import scash.warhorse.util._
import scodec.bits.ByteVector
import zio.test.DefaultRunnableSpec
import zio.test._
import zio.test.Assertion._
import scash.warhorse.gen

object KeyGenSpec extends DefaultRunnableSpec {
  val spec = suite("KeyGenSpec")(
    test("PrivateKey success") {
      val sec  = ByteVector.fromValidHex("67E56582298859DDAE725F972992A07C6C4FB9F62A8FFF58CE3CA926A1063530")
      val psec = PrivateKey(sec)
      assert(psec)(successResult(psec))
    },
    test("PrivateKey fail") {
      val sec  = ByteVector.fromValidHex("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF")
      val psec = PrivateKey(sec)
      assert(psec)(failure)
    },
    test("PublicKey success") {
      val sec       = ByteVector.fromValidHex("67E56582298859DDAE725F972992A07C6C4FB9F62A8FFF58CE3CA926A1063530")
      val resultArr = PrivateKey(sec).flatMap(_.genPublicKey)
      val expected  =
        "04C591A8FF19AC9C4E4E5793673B83123437E975285E7B442F4EE2654DFFCA5E2D2103ED494718C697AC9AEBCFD19612E224DB46661011863ED2FC54E71861E2A6"
      val ans       = PublicKey(ByteVector.fromValidHex(expected))
      assert(resultArr)(equalTo(ans))
    },
    testM("native vs lib gen pubkeys")(checkM(gen.privKey) { priv =>
      secp256k1.computePubKey(priv.b.toArray).map { pub =>
        val nativePubKey = PublicKey(ByteVector(pub))
        val pubKey       = priv.genPublicKey
        assert(pubKey)(successResult(nativePubKey))
      }
    }),
    testM("native vs lib gen pubkeys compressed")(checkM(gen.privKey) { priv =>
      secp256k1.computePubKey(priv.b.toArray).map { pub =>
        val nativePubKeyCompressed = PublicKey(ByteVector(pub)).map(_.compress)
        val pubKey                 = priv.genPublicKeyCompressed
        assert(nativePubKeyCompressed)(successResult(pubKey))
      }
    })
  )
}
