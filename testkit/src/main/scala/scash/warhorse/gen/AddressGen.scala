package scash.warhorse.gen

import scash.warhorse.core.blockchain.{ Address, LegacyAddress }
import zio.random.Random
import zio.test.{ Gen, Sized }

trait AddressGen {

  def p2pkh: Gen[Random with Sized, Address.P2PKH] =
    for {
      net  <- netGenerator
      hash <- hash160
    } yield LegacyAddress.p2pkh(net, hash)

  def p2sh: Gen[Random, Address.P2SH] =
    for {
      net  <- netGenerator
      hash <- scriptPubKey
    } yield LegacyAddress.p2sh(net, hash)

  def legacyAddress: Gen[Random with Sized, Address] = Gen.oneOf(p2pkh, p2sh)
}
