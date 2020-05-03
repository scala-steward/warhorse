package scash.warhorse.core.crypto

import org.scash.secp256k1

import zio.test.DefaultRunnableSpec
import zio.test._

import scodec.bits._

import scash.warhorse.core._
import scash.warhorse.gen
import scash.warhorse.util._

object SchnorrSpec extends DefaultRunnableSpec {

  val spec = suite("SchnorrSpec")(
    testM("verify") {
      checkM(gen.keyPair, gen.sha256) {
        case ((priv, pub), msg) =>
          val ver =
            secp256k1
              .signSchnorr(msg.toArray, priv.bytes.toArray)
              .map(s => crypto.verify[Schnorr](msg, ByteVector(s), pub))
          assertM(ver)(success(true))
      }
    }
  )
}
