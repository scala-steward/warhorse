package scash.warhorse.core.blockchain

import scash.warhorse.Result

sealed trait Address            extends Product with Serializable {
  def value: String
}

case class P2PKH(value: String) extends Address
case class P2SH(value: String)  extends Address

object Address {
  def apply(addr: String): Result[Address] =
    Addr[LegacyAddr].decode(addr) orElse Addr[CashAddr].decode(addr)
}
