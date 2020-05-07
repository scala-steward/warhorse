package scash.warhorse.core.crypto

import java.math.BigInteger

import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.math.ec.ECPoint
import scash.warhorse.{ Err, Result }
import scash.warhorse.Result.{ Failure, Successful }
import scash.warhorse.core._
import scash.warhorse.core.crypto.hash.Sha256
import scodec.bits.ByteVector

import scala.util.Try

sealed trait Schnorr

object Schnorr {
  class SchnorrSigner(ecc: ECCurve[Secp256k1]) extends Signer[Schnorr] {
    val fieldSize = ecc.domain.getCurve.getField.getCharacteristic

    /** Same additional data used in ABC and bchd for generating the same deterministic schnorr signing */
    val additionalData = "Schnorr+SHA256  ".getBytes("UTF-8")

    /** (R.y ^ (P - 1)/ 2) == 1 */
    private def hasSquareY(R: ECPoint) = {
      val j = fieldSize.subtract(BigInteger.ONE).divide(new BigInteger("2"))
      R.getYCoord.toBigInteger.modPow(j, fieldSize).equals(BigInteger.ONE)
    }

    @deprecated("this is not ready yet and its unsafe since its vulnerable to timing attacks", "1.0")
    def sign(msg: ByteVector, privkey: PrivateKey): Result[Signature] =
      if (msg.size != 32) Failure(Err.BoundsError("Schnorr Sign", "msg must be exactly 32 bytes", s"msg ${msg.size}"))
      else {
        val d = privkey.toBigInteger
        val N = ecc.domain.getN
        val G = ecc.domain.getG

        /** Calculate k */
        val nonceFunction = nonceRFC6979
        nonceFunction.init(N, new ECPrivateKeyParameters(d, ecc.domain).getD, msg.toArray, additionalData)
        val k0 = nonceFunction.nextK.mod(N)

        /** R = k * G. Negate nonce if R.y is not a quadratic residue */
        val R = G.multiply(k0).normalize
        val k =
          if (hasSquareY(R)) k0
          else N.subtract(k0)

        /** e = Hash(R.x || compressed(P) || m) mod n */
        val P        = G.multiply(d)
        val pubBytes = P.getEncoded(true).toByteVector
        val rx       = R.getXCoord.getEncoded.toByteVector
        val e        = Sha256.hash(rx ++ pubBytes ++ msg).toBigInteger.mod(N)

        /** s = (k + e * d) mod n */
        val s = e.multiply(d).add(k).mod(N).toUnsignedByteVector

        /** Signature = (R.x, s) */
        val sig = rx ++ s
        Successful(Signature(sig))
      }

    def verify(msg: ByteVector, sig: ByteVector, pubkey: PublicKey): Result[Boolean] =
      if (msg.size != 32 || sig.size != 64)
        Failure(
          Err.BoundsError(
            "Schnorr Verify",
            "msg must be 32 bytes and sig size 64 bytes",
            s"msg: ${msg.size} sig: ${sig.length}"
          )
        )
      else
        Try(ecc.domain.getCurve.decodePoint(pubkey.compress.toArray)).toOption
          .fold(Successful(false)) { P =>
            if (P.isInfinity) Successful(false)
            else {
              val G = ecc.domain.getG
              val N = ecc.domain.getN

              val (r, s) = sig.splitAt(32)
              val rNum   = r.toBigInteger
              val sNum   = s.toBigInteger
              if (rNum.compareTo(fieldSize) >= 0 || sNum.compareTo(N) >= 0) Successful(false)
              else {

                /** e = SHA256(r || compressed(P) || m) mod n */
                val e = Sha256
                  .hash(r ++ P.getEncoded(true).toByteVector ++ msg)
                  .toBigInteger
                  .mod(N)

                /** R = sG - eP */
                val sG = G.multiply(sNum)
                val eP = P.multiply(e)
                val R  = sG.subtract(eP).normalize

                /** Valid if R.x == r */
                if (R.isInfinity) Successful(false)
                else if (!hasSquareY(R)) Successful(false)
                else Successful(R.getXCoord.toBigInteger == rNum)
              }
            }
          }

  }

  implicit val schnorrSigner = new SchnorrSigner(Secp256k1.secp256K1Curve)
}
