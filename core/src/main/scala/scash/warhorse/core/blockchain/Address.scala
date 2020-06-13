package scash.warhorse.core.blockchain

import scash.warhorse.Result
import scash.warhorse.core.typeclass.Serde

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
    LegacyAddr.fromString(addr) //orElse CashAddr.fromString(addr)

  implicit val addressSerde: Serde[Address] =
    LegacyAddr.serde //|| CashAddr.serde
}
