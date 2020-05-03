package scash.warhorse.core.crypto

import java.math.BigInteger

import scash.warhorse.Result
import scash.warhorse.Result.Successful
import scash.warhorse.core._
import scash.warhorse.core.crypto.hash.Sha256

import scodec.bits.ByteVector

sealed trait Schnorr

object Schnorr {

  /**
   r,s = sig
   P = pubkey
   e = SHA256(r || P || msg)
   sG = r + eP
   r = sG - eP
  **/
  class SchnorrSigner(ecc: ECCurve[Secp256k1]) extends Signer[Schnorr] {

    def sign(msg: ByteVector, privkey: PrivateKey): Result[Signature] = Predef.???

    def verify(msg: ByteVector, sig: ByteVector, pubkey: PublicKey): Result[Boolean] = {
      val fieldSize = ecc.domain.getCurve.getField.getCharacteristic
      val P         = ecc.domain.getCurve.decodePoint(pubkey.compress.toArray)
      if (P == null || P.isInfinity) Successful(false)
      else {
        val (r, s) = sig.splitAt(32)
        val rB     = r.toBigInteger
        val sB     = s.toBigInteger
        if (rB.compareTo(fieldSize) >= 0) Successful(false)
        else if (sB.compareTo(ecc.domain.getN) >= 0) Successful(false)
        else {
          val e = Sha256
            .hash(r ++ P.getEncoded(true).toByteVector ++ msg)
            .toBigInteger
            .mod(ecc.domain.getN)
          val sG = ecc.domain.getG.multiply(sB)
          val eP = P.multiply(e)
          val R  = sG.subtract(eP).normalize
          if (R.isInfinity) Successful(false)
          else {
            val jac = R.getYCoord.toBigInteger
              .modPow(fieldSize.subtract(BigInteger.ONE).divide(BigInt(2L).bigInteger), fieldSize)
            if (jac != BigInteger.ONE) Successful(false)
            else Successful(R.getXCoord.toBigInteger == rB)
          }
        }
      }
    }
  }

  implicit val schnorrSigner = new SchnorrSigner(Secp256k1.secp256K1Curve)
}
