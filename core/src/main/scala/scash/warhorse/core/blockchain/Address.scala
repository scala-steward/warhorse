package scash.warhorse.core.blockchain

import scash.warhorse.{ Err, Result }
import scash.warhorse.Result.{ Failure, Successful }
import scash.warhorse.core.typeclass.Serde
import scodec.DecodeResult
import scodec.bits.ByteVector

sealed trait Address extends Product with Serializable {
  def value: String
}

object Address {
  case class P2PKH(value: String) extends Address
  case class P2SH(value: String)  extends Address

  def apply(addr: String): Result[Address] =
    if (addr.length < 26 || addr.length > 35)
      Failure(Err.BoundsError("Address", "26 <= addr.length <= 35 s", addr.length.toString))
    else
      Result
        .fromOption(ByteVector.fromBase58(addr), Err(s"Invalid base58 format for : $addr"))
        .flatMap(addressSerde.decodeValue(_))

  def fromValidBase58(addr: String): Address =
    apply(addr).require

  implicit val addressSerde: Serde[Address] =
    Serde(
      (a: Address) => Successful(ByteVector.fromValidBase58(a.value)),
      (bytes: ByteVector) =>
        LegacyAddress.fromByteVector(bytes.take(25)).map(DecodeResult(_, bytes.drop(25).toBitVector))
    )
}
