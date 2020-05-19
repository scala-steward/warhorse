package scash.warhorse.gen.crypto

import scash.warhorse.core.crypto.hash.{ Hasher, Sha256 }
import scash.warhorse.gen
import scodec.bits.ByteVector
import zio.test.{ Gen, Sized }
import zio.random.Random

trait CryptoGen {

  def sha256Bytes: Gen[Random with Sized, ByteVector] =
    for {
      msg  <- gen.randomMessage
      hash <- sha256Bytes(msg)
    } yield hash

  def sha256Bytes(str: String): Gen[Any, ByteVector] = sha256(str).map(_.bytes)

  def sha256(str: String): Gen[Any, Sha256] = sha256(ByteVector.view(str.getBytes))

  def sha256(bytes: ByteVector): Gen[Any, Sha256] = Gen.const(Hasher[Sha256].hash(bytes))
}
