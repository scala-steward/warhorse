package scash.warhorse.core.crypto

import java.math.BigInteger
import java.security.SecureRandom

import org.bouncycastle.crypto.Digest
import org.bouncycastle.crypto.macs.HMac
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.signers.DSAKCalculator
import org.bouncycastle.util.{ Arrays, BigIntegers }

/**
 * The rfc6979 nonce derivation function accepts additional entropy.
 * We are using the same entropy that is used by bitcoin-abc so our test
 * vectors will be compatible.
 *
 * See https://github.com/bitcoincashorg/bitcoincash.org/blob/master/spec/2019-05-15-schnorr.md#recommended-practices-for-secure-signature-generation
 *
 * Its the same implementation as HMacDSAKCalculator with the added optional parameter including an additional k'
 * as indicated in https://tools.ietf.org/html/rfc6979#section-3.6
 * This is a stand alone implementation that purposefuly doesnt use anything else from this library
 * K = HMAC_K(V || 0x00 || bytes(x) || bytes(h1) || k')
 */
class KGenerator(digest: Digest) extends DSAKCalculator {
  val hMac = new HMac(digest)
  val V    = new Array[Byte](hMac.getMacSize)
  val K    = new Array[Byte](hMac.getMacSize)
  var n    = BigInteger.ZERO

  def isDeterministic: Boolean = true

  def init(n: BigInteger, random: SecureRandom): Unit = throw new IllegalStateException("Operation not supported")

  def init(n: BigInteger, d: BigInteger, message: Array[Byte]): Unit =
    initImp(n, d, message)

  def init(n: BigInteger, d: BigInteger, message: Array[Byte], additionalData: Array[Byte]): Unit =
    initImp(n, d, message, additionalData)

  private def initImp(
    N: BigInteger,
    d: BigInteger,
    message: Array[Byte],
    additionalData: Array[Byte] = Array.empty
  ): Unit = {
    n = N
    // Step B
    Arrays.fill(V, 0x01.toByte)

    // Step C
    Arrays.fill(K, 0.toByte)

    //int2octets(x)
    val size = BigIntegers.getUnsignedByteLength(n)
    val x    = new Array[Byte](size)
    val dVal = BigIntegers.asUnsignedByteArray(d)
    System.arraycopy(dVal, 0, x, x.length - dVal.length, dVal.length)

    //bits2octets(h1)
    val m    = new Array[Byte](size)
    var mInt = bitsToInt(message)
    if (mInt.compareTo(n) >= 0) mInt = mInt.subtract(n)
    val mVal = BigIntegers.asUnsignedByteArray(mInt)
    System.arraycopy(mVal, 0, m, m.length - mVal.length, mVal.length)

    //Step D
    hMac.init(new KeyParameter(K))
    hMac.update(V, 0, V.length)
    hMac.update(0x00.toByte)
    hMac.update(x, 0, x.length)
    hMac.update(m, 0, m.length)
    if (additionalData.length != 0) hMac.update(additionalData, 0, additionalData.length)

    //Step E
    hMac.doFinal(K, 0)

    //STEP F
    hMac.init(new KeyParameter(K))
    hMac.update(V, 0, V.length)
    hMac.doFinal(V, 0)
    hMac.update(V, 0, V.length)
    hMac.update(0x01.toByte)
    hMac.update(x, 0, x.length)
    hMac.update(m, 0, m.length)
    if (additionalData.length != 0) hMac.update(additionalData, 0, additionalData.length)

    //STEP G
    hMac.doFinal(K, 0)
    hMac.init(new KeyParameter(K))
    hMac.update(V, 0, V.length)
    hMac.doFinal(V, 0)
    ()
  }

  def nextK: BigInteger = {
    val t = new Array[Byte](BigIntegers.getUnsignedByteLength(n))
    while (true) {
      var tOff = 0
      while (tOff < t.length) {
        hMac.update(V, 0, V.length)
        hMac.doFinal(V, 0)
        val len = Math.min(t.length - tOff, V.length)
        System.arraycopy(V, 0, t, tOff, len)
        tOff += len
      }
      val k    = bitsToInt(t)
      if (k.compareTo(BigInteger.ZERO) > 0 && k.compareTo(n) < 0) return k
      hMac.update(V, 0, V.length)
      hMac.update(0x00.toByte)
      hMac.doFinal(K, 0)
      hMac.init(new KeyParameter(K))
      hMac.update(V, 0, V.length)
      hMac.doFinal(V, 0)
    }
    throw new Exception("This should never happen")
  }

  private def bitsToInt(t: Array[Byte]) = {
    var v = new BigInteger(1, t)
    if (t.length * 8 > n.bitLength) v = v.shiftRight(t.length * 8 - n.bitLength)
    v
  }
}
