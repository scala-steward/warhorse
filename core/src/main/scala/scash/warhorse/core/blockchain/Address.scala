package scash.warhorse.core.blockchain

import scash.warhorse.{ Err, Result }
import scash.warhorse.Result.{ Failure, Successful }
import scash.warhorse.core.typeclass.Serde
import scodec.DecodeResult
import scodec.bits.ByteVector

sealed trait Address            extends Product with Serializable {
  def value: String
}

case class P2PKH(value: String) extends Address
case class P2SH(value: String)  extends Address

object Address {
  def asP2PKH(addr: Address): Option[P2PKH] =
    addr match {
      case p: P2PKH => Some(p)
      case _: P2SH  => None
    }

  def apply(addr: String): Result[Address] =
    if (addr.length < 26 || addr.length > 35)
      Failure(Err.BoundsError("Address", "26 <= addr.length <= 35 s", addr.length.toString))
    else LegacyAddr.fromString(addr)

  implicit val addressSerde: Serde[Address] =
    Serde(
      (a: Address) => Successful(ByteVector.fromValidBase58(a.value)),
      (bytes: ByteVector) => LegacyAddr.fromByteVector(bytes.take(25)).map(DecodeResult(_, bytes.drop(25).toBitVector))
    )
}
