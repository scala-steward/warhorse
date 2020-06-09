package scash.warhorse.gen

import scash.warhorse.core.blockchain.{ Addr, Address, LegacyAddr }
import zio.random.Random
import zio.test.{ Gen, Sized }

trait AddressGen {

  def p2pkh: Gen[Random with Sized, Address] =
    for {
      pubKey <- pubkey
      net    <- netGenerator
    } yield Addr[LegacyAddr].p2pkh(net, pubKey)

  def p2sh: Gen[Random, Address] =
    for {
      net     <- netGenerator
      spubkey <- scriptPubKey
    } yield Addr[LegacyAddr].p2sh(net, spubkey)

  def legacyAddress: Gen[Random with Sized, Address] = Gen.oneOf(p2pkh, p2sh)
}
