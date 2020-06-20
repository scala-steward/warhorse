package scash.warhorse.core.crypto

import java.io.ByteArrayOutputStream
import java.math.BigInteger

import org.bouncycastle.asn1.{ ASN1InputStream, ASN1Integer, DERSequenceGenerator, DLSequence }
import org.bouncycastle.crypto.params.{ ECDomainParameters, ECPrivateKeyParameters, ECPublicKeyParameters }
import org.bouncycastle.crypto.signers.{ ECDSASigner => BCSigner }
import scash.warhorse.{ Err, Result }
import scash.warhorse.Result.{ Failure, Successful }
import scodec.bits.ByteVector
import scash.warhorse.core._

sealed trait ECDSA

object ECDSA {
  class ECDSASigner(ecc: ECCurve[Secp256k1]) extends Signer[ECDSA] {
    private val domain = new ECDomainParameters(ecc.curve, ecc.G, ecc.N)

    private def lowS(s: BigInteger): BigInt =
      if (s.compareTo(ecc.N.shiftRight(1)) <= 0) s
      else ecc.N.subtract(s)

    def sign(msg: ByteVector, privkey: PrivateKey): Result[Signature] =
      if (msg.size != 32) Failure(Err.BoundsError("ECDSA Sign", "msg must be exactly 32 bytes", s"msg ${msg.size}"))
      else {
        val signer     = new BCSigner(nonceRFC6979)
        val pkeyParams = new ECPrivateKeyParameters(privkey.toBigInteger, domain)
        signer.init(true, pkeyParams)
        val Array(r, s) = signer.generateSignature(msg.toArray)
        (lowS _ andThen (derEncoding(r, _)) andThen (Signature(_)) andThen (Successful(_)))(s)
      }

    def verify(msg: ByteVector, sig: ByteVector, pubkey: PublicKey): Result[Boolean] =
      if (msg.size != 32 || sig.size > 520)
        Failure(
          Err.BoundsError(
            "ECDSA Verify",
            "msg must be 32 bytes and sig size less than 520 bytes",
            s"msg: ${msg.size} sig: ${sig.length}"
          )
        )
      else {
        val pkeypoint  = ecc.curve.decodePoint(pubkey.toArray)
        val pkeyparams = new ECPublicKeyParameters(pkeypoint, domain)
        val signer     = new BCSigner
        signer.init(false, pkeyparams)

        val ans1 = new ASN1InputStream(sig.toArray)
        val seq  = ans1.readObject().asInstanceOf[DLSequence]
        val r    = seq.getObjectAt(0).asInstanceOf[ASN1Integer].getPositiveValue
        val s    = seq.getObjectAt(1).asInstanceOf[ASN1Integer].getPositiveValue
        Successful(signer.verifySignature(msg.toArray, r, s))
      }
  }

  implicit val ecdsaSigner = new ECDSASigner(Secp256k1.secp256K1Curve)

  private def derEncoding(r: BigInt, s: BigInt): ByteVector = {
    val bos = new ByteArrayOutputStream(73)
    val seq = new DERSequenceGenerator(bos)
    seq.addObject(new ASN1Integer(r.bigInteger))
    seq.addObject(new ASN1Integer(s.bigInteger))
    seq.close()
    bos.toByteArray.toByteVector
  }

  def compact2Der(b: ByteVector): Result[ByteVector] =
    if (b.size != 64) Failure(Err.BoundsError("compact2der", "64 bytes long", s"size ${b.size}"))
    else {
      val (r, s) = b.splitAt(32)
      Successful(derEncoding(r.toBigInt, s.toBigInt))
    }
}
