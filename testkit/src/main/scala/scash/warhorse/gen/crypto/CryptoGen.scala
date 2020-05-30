package scash.warhorse.gen.crypto

import scash.warhorse.core.crypto.hash.{ DoubleSha256, Hasher, Sha256 }
import scash.warhorse.gen
import scodec.bits.ByteVector
import zio.test.{ Gen, Sized }
import zio.random.Random

trait CryptoGen {

  def sha256: Gen[Random with Sized, Sha256] =
    for {
      msg  <- gen.randomMessage
      hash <- sha256(msg)
    } yield hash

  def sha256Bytes: Gen[Random with Sized, ByteVector] = sha256.map(_.bytes)

  def sha256Bytes(str: String): Gen[Any, ByteVector] = sha256(str).map(_.bytes)

  def sha256(str: String): Gen[Any, Sha256] = sha256(ByteVector.view(str.getBytes))

  def sha256(bytes: ByteVector): Gen[Any, Sha256] = Gen.const(Hasher[Sha256].hash(bytes))

  def doubleSha256(str: String): Gen[Any, DoubleSha256] =
    doubleSha256(ByteVector.view(str.getBytes))

  def doubleSha256: Gen[Random with Sized, DoubleSha256] =
    for {
      msg  <- gen.randomMessage
      hash <- doubleSha256(msg)
    } yield hash

  def doubleSha256(bytes: ByteVector): Gen[Any, DoubleSha256] =
    Gen.const(Hasher[DoubleSha256].hash(bytes))
}
