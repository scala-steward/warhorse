package scash.warhorse.core.crypto

import io.circe.Decoder

import org.scash.secp256k1

import scash.warhorse.core.crypto
import scash.warhorse.gen
import scash.warhorse.util._

import scodec.bits.ByteVector

import zio.test.Assertion._
import zio.test._

object ECDSASpec extends DefaultRunnableSpec {
  val spec = suite("ECDSASpec")(
    testM("sign") {
      checkM(gen.keyPair, gen.sha256Bytes) {
        case ((priv, pub), msg) =>
          val sig = crypto.sign[ECDSA](msg, priv).require
          assertM(secp256k1.verifyECDSA(msg.toArray, sig.bytes.toArray, pub.toArray))(isTrue)
      }
    },
    testM("verify") {
      checkM(gen.keyPair, gen.sha256Bytes) {
        case ((priv, pub), msg) =>
          val ver =
            secp256k1
              .signECDSA(msg.toArray, priv.toArray)
              .map(s => crypto.verify[ECDSA](msg, ByteVector(s), pub))
          assertM(ver)(success(true))
      }
    },
    testM("sign&verify") {
      check(gen.keyPair, gen.sha256Bytes) {
        case ((priv, pub), msg) =>
          val ans = for {
            sig <- crypto.sign[ECDSA](msg, priv)
            ver <- crypto.verify[ECDSA](msg, sig.bytes, pub)
          } yield ver
          assert(ans)(success(true))
      }
    },
    testM("deterministic")(
      // dataset from https://bitcointalk.org/index.php?topic=285142.msg3299061#msg3299061
      jsonFromCSVM[TestVectorECDSA]("ecdsa.json") { data =>
        check(gen.sha256Bytes(data.msgStr)) { msg =>
          val ans = for {
            sec <- PrivateKey(data.privHex)
            pub <- sec.genPublicKey
            sig <- crypto.sign[ECDSA](msg, sec)
            ver <- crypto.verify[ECDSA](msg, sig.bytes, pub)
          } yield (sig, ver)
          assert(ans)(success((Signature(data.exp), true)))

        }
      }
    ),
    testM("fail msg sign size")(
      check(gen.byteVectorN(30), gen.privKey)((msg, priv) => assert(crypto.sign[ECDSA](msg, priv))(failure))
    ),
    testM("fail msg verify size")(
      check(gen.byteVectorN(30), gen.pubkey)((msg, pub) => assert(crypto.verify[ECDSA](msg, msg, pub))(failure))
    ),
    testM("fail sig verify size")(
      check(gen.byteVectorN(32), gen.byteVectorN(521), gen.pubkey)((msg, sig, pub) =>
        assert(crypto.verify[ECDSA](msg, sig, pub))(failure)
      )
    )
  )

  case class TestVectorECDSA(privHex: ByteVector, msgStr: String, exp: ByteVector)

  implicit val ecdsaDecoder: Decoder[TestVectorECDSA] = rowCoder(data =>
    TestVectorECDSA(
      ByteVector.fromValidHex(data(0)).padLeft(32),
      data(1),
      ECDSA
        .compact2Der(ByteVector.fromValidHex(data(2)))
        .getOrElse(ByteVector.fromValidHex(data(2)))
    )
  )
}
