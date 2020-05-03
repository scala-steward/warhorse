package scash.warhorse.core.crypto

import java.security.Security

import org.bouncycastle.crypto.Digest
import org.bouncycastle.jce.provider.BouncyCastleProvider
import scodec.bits.ByteVector

package object hash {
  Security.insertProviderAt(new BouncyCastleProvider(), 1)

  def genHash(digest: Digest)(input: ByteVector): ByteVector = {
    digest.update(input.toArray, 0, input.length.toInt)
    val out = new Array[Byte](digest.getDigestSize)
    digest.doFinal(out, 0)
    ByteVector.view(out)
  }
}
