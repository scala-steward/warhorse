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

  val fromLegacyList = List(
    ("1BpEi6DfDAUFd7GtittLSdBeYJvcoaVggu", "bitcoincash:qpm2qsznhks23z7629mms6s4cwef74vcwvy22gdx6a"),
    ("1KXrWXciRDZUpQwQmuM1DbwsKDLYAYsVLR", "bitcoincash:qr95sy3j9xwd2ap32xkykttr4cvcu7as4y0qverfuy"),
    ("16w1D5WRVKJuZUsSRzdLp9w3YGcgoxDXb", "bitcoincash:qqq3728yw0y47sqn6l2na30mcw6zm78dzqre909m2r"),
    ("3CWFddi6m4ndiGyKqzYvsFYagqDLPVMTzC", "bitcoincash:ppm2qsznhks23z7629mms6s4cwef74vcwvn0h829pq"),
    ("3LDsS579y7sruadqu11beEJoTjdFiFCdX4", "bitcoincash:pr95sy3j9xwd2ap32xkykttr4cvcu7as4yc93ky28e"),
    ("31nwvkZwyPdgzjBJZXfDmSWsC4ZLKpYyUw", "bitcoincash:pqq3728yw0y47sqn6l2na30mcw6zm78dzq5ucqzc37")
  )

  val spec = suite("AddressSpec")(
    suite("symmetry")(
      testM("p2pkh")(check(gen.p2pkh)(addr => assert(Addr[LegacyAddr].decode(addr.value))(success(addr)))),
      testM("ps2h")(check(gen.p2sh)(addr => assert(Addr[LegacyAddr].decode(addr.value))(success(addr)))),
      testM("addrp2pkh")(check(gen.addrp2pkh)(addr => assert(Addr[CashAddr].decode(addr.value))(success(addr)))),
      testM("addrp2sh")(check(gen.addrp2sh)(addr => assert(Addr[CashAddr].decode(addr.value))(success(addr)))),
      testM("address")(check(gen.address)(addr => assert(Address(addr.value))(success(addr))))
    ),
    suite("fromString")(
      testM("fail")(
        jsonFromCSV[InvalidAddressTest]("key_io_invalid.json")(base58 => assert(Address(base58.addr))(failure))
      ),
      testM("success")(
        jsonFromCSV[ValidAddressTest]("valid_legacy_address.json") { test =>
          val ans      = Address(test.addr)
          val expected = ans.map {
            case P2PKH(_) =>
              Addr[LegacyAddr].p2pkh(test.chain, test.hash.drop(3).dropRight(2).decodeExact_[Hash160])
            case P2SH(_)  =>
              Addr[LegacyAddr].p2sh(test.chain, test.hash.drop(2).dropRight(1).decodeExact_[Hash160])
          }
          assert(ans.map(_.value))(success(test.addr)) && assert(ans)(equalTo(expected))
        }
      )
    ),
    testM("fromLegacyAddr") {
      ZIO
        .foreach(fromLegacyList) {
          case (legStr, cashaddr) =>
            ZIO.succeed(
              assert(Addr[LegacyAddr].decode(legStr).map(CashAddr.fromLegacyAddr(_).value))(success(cashaddr))
            )
        }
        .map(BoolAlgebra.collectAll(_).get)
    }
  )
}
