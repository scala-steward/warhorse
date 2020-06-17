package scash.warhorse.gen.crypto

import scash.warhorse.core.crypto.hash.{ DoubleSha256, Hash160, Sha256 }
import scash.warhorse.gen
import scash.warhorse.core._

import scodec.bits.ByteVector
import zio.test.{ Gen, Sized }
import zio.random.Random

trait CryptoGen {

  def hash160: Gen[Random with Sized, Hash160] =
    for {
      msg  <- gen.randomMessage
      hash <- hash160(msg)
    } yield hash

  def hash160Bytes(str: String): Gen[Any, ByteVector] = hash160(str).map(_.bytes)

  def hash160(str: String): Gen[Any, Hash160] = hash160(ByteVector.view(str.getBytes))

  def hash160(bytes: ByteVector): Gen[Any, Hash160] = Gen.const(bytes.hash[Hash160])

  def sha256: Gen[Random with Sized, Sha256] =
    for {
      msg  <- gen.randomMessage
      hash <- sha256(msg)
    } yield hash

  def sha256Bytes: Gen[Random with Sized, ByteVector] = sha256.map(_.bytes)

  def sha256Bytes(str: String): Gen[Any, ByteVector] = sha256(str).map(_.bytes)

  def sha256(str: String): Gen[Any, Sha256] = sha256(ByteVector.view(str.getBytes))

  def sha256(bytes: ByteVector): Gen[Any, Sha256] = Gen.const(bytes.hash[Sha256])

  def doubleSha256(str: String): Gen[Any, DoubleSha256] =
    doubleSha256(ByteVector.view(str.getBytes))

  def doubleSha256: Gen[Random with Sized, DoubleSha256] =
    for {
      msg  <- gen.randomMessage
      hash <- doubleSha256(msg)
    } yield hash

  def doubleSha256(bytes: ByteVector): Gen[Any, DoubleSha256] =
    Gen.const(bytes.hash[DoubleSha256])
}
