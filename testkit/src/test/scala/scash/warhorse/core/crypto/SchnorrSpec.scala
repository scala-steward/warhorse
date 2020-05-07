package scash.warhorse.core.crypto

import io.circe.Decoder

import org.scash.secp256k1

import zio.test.DefaultRunnableSpec
import zio.test._
import zio.ZIO
import zio.test.Assertion.isTrue

import scodec.bits._

import scash.warhorse.core._
import scash.warhorse.gen
import scash.warhorse.util._

object SchnorrSpec extends DefaultRunnableSpec {

  val spec = suite("SchnorrSpec")(
    testM("sign") {
      checkM(gen.keyPair, gen.sha256) {
        case ((priv, pub), msg) =>
          val sig = crypto.sign[Schnorr](msg, priv).require
          assertM(secp256k1.verifySchnorr(msg.toArray, sig.bytes.toArray, pub.toArray))(isTrue)
      }
    },
    testM("verify") {
      checkM(gen.keyPair, gen.sha256) {
        case ((priv, pub), msg) =>
          val ver =
            secp256k1
              .signSchnorr(msg.toArray, priv.bytes.toArray)
              .map(s => crypto.verify[Schnorr](msg, ByteVector(s), pub))
          assertM(ver)(success(true))
      }
    },
    testM("sign&verify") {
      check(gen.keyPair, gen.sha256) {
        case ((priv, pub), msg) =>
          val ans = for {
            sig <- crypto.sign[Schnorr](msg, priv)
            ver <- crypto.verify[Schnorr](msg, sig.bytes, pub)
          } yield ver
          assert(ans)(success(true))
      }
    },
    testM("edge cases") {
      // dataset https://github.com/Bitcoin-ABC/bitcoin-abc/blob/master/src/secp256k1/src/modules/schnorr/tests_impl.h
      parseJsonfromFile[List[TestVectorSchnorr]]("schnorr.json")
        .flatMap(r =>
          ZIO.foreach(r.require) { data =>
            val ver = for {
              pub <- PublicKey(data.pubkey)
              ver <- crypto.verify[Schnorr](data.data, data.sig, pub)
            } yield ver
            ZIO.succeed(assert(ver)(success(data.exp)))
          }
        )
        .map(BoolAlgebra.collectAll(_).get)
    },
    test("deterministic") {
      val data = hex"5255683DA567900BFD3E786ED8836A4E7763C221BF1AC20ECE2A5171B9199E8A"
      val sec  = hex"12B004FFF7F4B69EF8650E767F18F11EDE158148B425660723B9F9A66E61F747"
      val exp =
        hex"2C56731AC2F7A7E7F11518FC7722A166B02438924CA9D8B4D111347B81D0717571846DE67AD3D913A8FDF9D8F3F73161A4C48AE81CB183B214765FEB86E255CE"
      val ans = for {
        priv <- PrivateKey(sec)
        pub  <- priv.genPublicKeyCompressed
        sig  <- crypto.sign[Schnorr](data, priv)
        ver  <- crypto.verify[Schnorr](data, sig.bytes, pub)
      } yield (ver, sig)
      assert(ans.map(_._1))(success(true)) &&
      assert(ans.map(_._2.bytes))(success(exp))
    },
    testM("fail msg sign size")(
      check(gen.byteVectorN(30), gen.privKey)((msg, priv) => assert(crypto.sign[Schnorr](msg, priv))(failure))
    ),
    testM("fail msg verify size")(
      check(gen.byteVectorN(30), gen.pubkey)((msg, pub) => assert(crypto.verify[Schnorr](msg, msg, pub))(failure))
    ),
    testM("fail sig verify size")(
      check(gen.byteVectorN(32), gen.byteVectorN(65), gen.pubkey)((msg, sig, pub) =>
        assert(crypto.verify[Schnorr](msg, sig, pub))(failure)
      )
    )
  )

  import Predef._

  case class TestVectorSchnorr(data: ByteVector, sig: ByteVector, pubkey: ByteVector, exp: Boolean, comment: String)

  implicit val ecdsaDecoder: Decoder[List[TestVectorSchnorr]] = csvDecoder.map(dataset =>
    dataset.map { data =>
      TestVectorSchnorr(
        ByteVector.fromValidHex(data(0)),
        ByteVector.fromValidHex(data(1)),
        ByteVector.fromValidHex(data(2)),
        data(3).toBoolean,
        data(4)
      )
    }
  )
}
