package scash.warhorse.core.crypto

import java.io.ByteArrayOutputStream

import org.bouncycastle.asn1.{ ASN1InputStream, ASN1Integer, DERSequenceGenerator, DLSequence }
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.params.{ ECPrivateKeyParameters, ECPublicKeyParameters }
import org.bouncycastle.crypto.signers.{ HMacDSAKCalculator, ECDSASigner => BCSigner }
import scash.warhorse.{ Err, Result }
import scash.warhorse.Result.{ Failure, Successful }
import scodec.bits.ByteVector
import scash.warhorse.core._

case class ECDSA()

object ECDSA {
  class ECDSASigner(e: ECCurve[Secp256k1]) extends Signer[ECDSA] {
    private def lowS(s: BigInt): BigInt =
      if (s.compareTo(e.domain.getN.shiftRight(1)) <= 0) s
      else e.domain.getN.subtract(s.bigInteger)

    def sign(msg: ByteVector, privkey: PrivateKey): Result[Signature] =
      if (msg.size != 32) Failure(Err.BoundsError("ECDSA Sign", "msg must be exactly 32 bytes", s"msg ${msg.size}"))
      else {
        val signer     = new BCSigner(new HMacDSAKCalculator(new SHA256Digest))
        val pkeyParams = new ECPrivateKeyParameters(BigInt(privkey.hex, 16).bigInteger, e.domain)
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
        val pkeypoint  = e.domain.getCurve.decodePoint(pubkey.bytes.toArray)
        val pkeyparams = new ECPublicKeyParameters(pkeypoint, e.domain)
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
