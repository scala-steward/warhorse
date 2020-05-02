package scash.warhorse.gen.crypto

import org.bouncycastle.crypto.Digest
import org.bouncycastle.crypto.digests.SHA256Digest
import scodec.bits.ByteVector
import zio.test.{ Gen, Sized }
import scash.warhorse.gen
import zio.random.Random
trait CryptoGen {

  private def hash(digest: Digest)(input: ByteVector): ByteVector = {
    digest.update(input.toArray, 0, input.length.toInt)
    val out = new Array[Byte](digest.getDigestSize)
    digest.doFinal(out, 0)
    ByteVector.view(out)
  }

  def sha256: Gen[Random with Sized, ByteVector] =
    for {
      msg  <- gen.randomMessage
      hash <- sha256(msg)
    } yield hash

  def sha256(str: String): Gen[Any, ByteVector] = Gen.const(hash(new SHA256Digest)(ByteVector(str.getBytes)))
}
