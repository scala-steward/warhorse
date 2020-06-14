package scash.warhorse.core.crypto.hash

import java.security.Security

import org.bouncycastle.crypto.Digest
import org.bouncycastle.jce.provider.BouncyCastleProvider

import scash.warhorse.core.typeclass.Serde
import scash.warhorse.core._

import scodec.bits.ByteVector

trait Hasher[A] {
  def hasher: Digest

  def cons(b: ByteVector): A

  def hash[S: Serde](s: S): A = hash(s.bytes)

  def hash(a: ByteVector): A = (genHash(hasher) _ andThen cons)(a)

  def hash(str: String): A = hash(ByteVector(str.getBytes))

  private def genHash(digest: Digest)(input: ByteVector): ByteVector = {
    digest.update(input.toArray, 0, input.length.toInt)
    val out = new Array[Byte](digest.getDigestSize)
    digest.doFinal(out, 0)
    ByteVector.view(out)
  }

}

object Hasher {
  Security.insertProviderAt(new BouncyCastleProvider(), 1)

  def apply[A](implicit h: Hasher[A]): Hasher[A] = h
}
