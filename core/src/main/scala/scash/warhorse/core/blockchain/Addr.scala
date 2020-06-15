package scash.warhorse.core.blockchain

import scash.warhorse.Result
import scash.warhorse.core.crypto.PublicKey
import scash.warhorse.core.crypto.hash.{ Hash160, Hasher }

import scodec.bits.ByteVector

trait Addr[A] {

  def p2pkh(net: Net, hash: Hash160): P2PKH

  def p2sh(net: Net, hash: Hash160): P2SH

  def p2pkh(net: Net, pubKey: PublicKey): P2PKH      =
    p2pkh(net, Hasher[Hash160].hash(pubKey))

  //TODO: redeemScript
  def p2sh(net: Net, redeemScript: ByteVector): P2SH =
    p2sh(net, Hasher[Hash160].hash(redeemScript))

  def decode(string: String): Result[Address]
}

object Addr {
  def apply[A](implicit a: Addr[A]): Addr[A] = a
}
