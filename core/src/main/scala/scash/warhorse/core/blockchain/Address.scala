package scash.warhorse.core.blockchain

import scash.warhorse.Result.Successful
import scash.warhorse.core.typeclass.Serde
import scodec.DecodeResult
import scodec.bits.ByteVector

sealed trait Address extends Product with Serializable {
  def value: String
}

object Address {
  case class P2PKH(value: String) extends Address
  case class P2SH(value: String)  extends Address

  implicit val addressSerde: Serde[Address] =
    Serde(
      (a: Address) => Successful(ByteVector.fromValidBase58(a.value)),
      (bytes: ByteVector) =>
        LegacyAddress.fromByteVector(bytes.take(25)).map(DecodeResult(_, bytes.drop(25).toBitVector))
    )
}
