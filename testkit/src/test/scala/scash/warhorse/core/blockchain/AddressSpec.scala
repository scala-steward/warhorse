package scash.warhorse.core.blockchain

import io.circe.Decoder

import scash.warhorse._
import scash.warhorse.core._
import scash.warhorse.core.crypto.hash.Hash160
import scash.warhorse.util._
import scodec.bits.ByteVector
import zio.ZIO
import zio.test.DefaultRunnableSpec
import zio.test.Assertion.equalTo
import zio.test._

object AddressSpec extends DefaultRunnableSpec {
  case class ValidAddressTest(addr: String, hash: ByteVector, chain: Net)
  case class InvalidAddressTest(addr: String)

  implicit val txValidDecoder: Decoder[ValidAddressTest] =
    rowCoder(str => ValidAddressTest(str(0), ByteVector.fromValidHex(str(1)), Net(str(2))))

  implicit val txInvalidlistDecoder: Decoder[InvalidAddressTest] = rowCoder(str => InvalidAddressTest(str(0)))

  val fromLegacyList                    = List(
    ("1BpEi6DfDAUFd7GtittLSdBeYJvcoaVggu", "bitcoincash:qpm2qsznhks23z7629mms6s4cwef74vcwvy22gdx6a"),
    ("1KXrWXciRDZUpQwQmuM1DbwsKDLYAYsVLR", "bitcoincash:qr95sy3j9xwd2ap32xkykttr4cvcu7as4y0qverfuy"),
    ("16w1D5WRVKJuZUsSRzdLp9w3YGcgoxDXb", "bitcoincash:qqq3728yw0y47sqn6l2na30mcw6zm78dzqre909m2r"),
    ("3CWFddi6m4ndiGyKqzYvsFYagqDLPVMTzC", "bitcoincash:ppm2qsznhks23z7629mms6s4cwef74vcwvn0h829pq"),
    ("3LDsS579y7sruadqu11beEJoTjdFiFCdX4", "bitcoincash:pr95sy3j9xwd2ap32xkykttr4cvcu7as4yc93ky28e"),
    ("31nwvkZwyPdgzjBJZXfDmSWsC4ZLKpYyUw", "bitcoincash:pqq3728yw0y47sqn6l2na30mcw6zm78dzq5ucqzc37")
  )

  //Regtest txs in legacy uses the same byte as Testnet which results on
  // loss of information when doing rountrip conversions"
  def toTestNet(addr: Address): Address =
    addr match {
      case P2PKH(RegTest, pubKeyHash)      => P2PKH(TestNet, pubKeyHash)
      case P2SH(RegTest, redeemScriptHash) => P2SH(TestNet, redeemScriptHash)
      case a                               => a
    }

  val spec = suite("AddressSpec")(
    suite("symmetry")(
      testM("p2pkh")(check(gen.p2pkh)(addr => assert(Address(addr.toLegacyAddr))(success(toTestNet(addr))))),
      testM("ps2h")(check(gen.p2sh)(addr => assert(Address(addr.toLegacyAddr))(success(toTestNet(addr))))),
      testM("addrp2pkh")(check(gen.p2pkh)(addr => assert(Address(addr.toCashAddr))(success(addr)))),
      testM("addrp2sh")(check(gen.p2sh)(addr => assert(Address(addr.toCashAddr))(success(addr))))
    ),
    suite("fromString")(
      testM("fail")(
        jsonFromCSV[InvalidAddressTest]("key_io_invalid.json")(base58 => assert(Address(base58.addr))(failure))
      ),
      testM("success Legacy")(
        jsonFromCSV[ValidAddressTest]("valid_legacy_address.json") { test =>
          val ans      = Address(test.addr)
          val net      = if (test.chain == RegTest) TestNet else test.chain
          val expected = ans.map {
            case _: P2PKH => P2PKH(net, test.hash.drop(3).dropRight(2).decodeExact_[Hash160])
            case _: P2SH  => P2SH(net, test.hash.drop(2).dropRight(1).decodeExact_[Hash160])
          }
          assert(ans.map(_.toLegacyAddr))(success(test.addr)) && assert(ans)(equalTo(expected))
        }
      ),
      testM("success Cashaddr")(
        jsonFromCSV[ValidAddressTest]("valid_legacy_address.json") { test =>
          val cashAddr = Address.legacyToCashAddr(test.addr)
          val ans      = cashAddr.flatMap(Address.apply)
          val net      = if (test.chain == RegTest) TestNet else test.chain
          val expected = ans.map {
            case _: P2PKH => P2PKH(net, test.hash.drop(3).dropRight(2).decodeExact_[Hash160])
            case _: P2SH  => P2SH(net, test.hash.drop(2).dropRight(1).decodeExact_[Hash160])
          }
          assert(ans.map(_.toCashAddr))(equalTo(cashAddr)) && assert(ans)(equalTo(expected))
        }
      )
    ),
    testM("Roundtrip address") {
      ZIO
        .foreach(fromLegacyList) {
          case (legStr, cashaddr) =>
            ZIO.succeed(
              assert(Address(legStr).map(_.toLegacyAddr).flatMap(Address.legacyToCashAddr))(success(cashaddr)) &&
                assert(Address(cashaddr).map(_.toCashAddr).flatMap(Address.cashAddrToLegacy))(success(legStr))
            )
        }
        .map(BoolAlgebra.collectAll(_).get)
    }
  )
}
