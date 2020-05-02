package scash.warhorse.core.crypto

import scash.warhorse.{ Err, Result }
import scash.warhorse.Result.{ Failure, Successful }
import scash.warhorse.core.typeclass.Serde
import scash.warhorse.core._
import scodec.DecodeResult
import scodec.bits.ByteVector

case class PrivateKey(b: ByteVector)

object PrivateKey {

  def apply(b: ByteVector): Result[PrivateKey] =
    if (b.size != 32) Failure(Err.BoundsError("PrivateKey", s"exactly 32 bytes", s"size ${b.size}"))
    else inRange(b.toBigInt, _ => Successful(new PrivateKey(b)))

  def apply(bigInt: BigInt): Result[PrivateKey] =
    inRange(bigInt, b => apply(b.toByteVector.dropWhile(_ == 0.toByte).padLeft(32)))

  implicit val sha256Serde: Serde[PrivateKey] = Serde[PrivateKey](
    (a: PrivateKey) => Successful(a.b),
    (b: ByteVector) => apply(b.take(32)).map(DecodeResult(_, b.drop(32).bits))
  )

  val max  = BigInt("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16)
  val zero = BigInt(0)

  implicit class PrivateKeyOps(p: PrivateKey) {
    def genPublicKey: Result[PublicKey] = crypto.genPubkey(p)

    def genPublicKeyCompressed: Result[PublicKey] = crypto.genPubkeyCompressed(p)
  }

  private def inRange(bigInt: BigInt, f: BigInt => Result[PrivateKey]): Result[PrivateKey] =
    if (bigInt > zero && bigInt < max) f(bigInt)
    else Failure(Err.BoundsError("PrivateKey", s"needs > $zero && < $max", bigInt.toHex))
}
