package scash.warhorse.core.crypto

import java.security.SecureRandom

import org.bouncycastle.jce.ECNamedCurveTable

import scash.warhorse.Result
import scash.warhorse.core._

sealed trait Secp256k1

object Secp256k1 {

  val secp256K1Curve: ECCurve[Secp256k1] = {
    val spec = ECNamedCurveTable.getParameterSpec("secp256k1")
    new ECCurve[Secp256k1] {
      val G     = spec.getG
      val N     = spec.getN
      val curve = spec.getCurve
    }
  }

  implicit val secp256k1KeyGen: KeyGen[Secp256k1] = new KeyGen[Secp256k1] {
    def genPrivkey: Result[PrivateKey] =
      PrivateKey(BigInt(256, new SecureRandom()))

    def genPubkey(privateKey: PrivateKey): Result[PublicKey] =
      genPublicKey(privateKey, false)

    def genPubkeyCompressed(privateKey: PrivateKey): Result[PublicKey] =
      genPublicKey(privateKey, true)

    private def genPublicKey(privateKey: PrivateKey, compressed: Boolean): Result[PublicKey] = {
      val pointQ = secp256K1Curve.G.multiply(privateKey.toBigInteger)
      PublicKey.apply(pointQ.getEncoded(compressed).toByteVector)
    }

  }
}
