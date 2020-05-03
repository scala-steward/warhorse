package scash.warhorse.gen.crypto

import org.bouncycastle.crypto.digests.SHA256Digest

import scash.warhorse.core.crypto.hash
import scash.warhorse.gen

import scodec.bits.ByteVector

import zio.test.{ Gen, Sized }
import zio.random.Random

trait CryptoGen {

  def sha256: Gen[Random with Sized, ByteVector] =
    for {
      msg  <- gen.randomMessage
      hash <- sha256(msg)
    } yield hash

  def sha256(str: String): Gen[Any, ByteVector] =
    Gen.const(hash.genHash(new SHA256Digest)(ByteVector(str.getBytes)))
}
