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

  implicit val txValidDecoder: Decoder[List[ValidAddressTest]] =
    csvDecoder.map(dataset =>
      dataset.map(str => ValidAddressTest(str(0), ByteVector.fromValidHex(str(1)), Net(str(2))))
    )

  implicit val txInvalidlistDecoder: Decoder[List[String]] =
    csvDecoder.map(dataset => dataset.map(str => str(0)))

  val spec = suite("AddressSpec")(
    suite("symmetry")(
      testM("p2pkh")(check(gen.p2pkh)(addr => assert(addr.bytes.decode[Address])(success(addr)))),
      testM("ps2h")(check(gen.p2sh)(addr => assert(addr.bytes.decode[Address])(success(addr))))
    ),
    suite("fromBase58")(
      testM("fail")(
        parseJsonfromFile[List[String]]("key_io_invalid.json")
          .flatMap(r => ZIO.foreach(r.require)(base58 => ZIO.succeed(assert(Address(base58))(failure))))
          .map(BoolAlgebra.collectAll(_).get)
      ),
      testM("success")(
        parseJsonfromFile[List[ValidAddressTest]]("valid_legacy_address.json")
          .flatMap(r =>
            ZIO.foreach(r.require) { test =>
              val ans      = Address(test.addr)
              val expected = ans.map {
                case Address.P2PKH(_) =>
                  LegacyAddress.p2pkh(test.chain, test.hash.drop(3).dropRight(2).decodeExact_[Hash160])
                case Address.P2SH(_)  =>
                  LegacyAddress.p2sh(test.chain, test.hash.drop(2).dropRight(1).decodeExact_[Hash160])
              }
              ZIO.succeed(
                assert(ans.map(_.value))(success(test.addr)) &&
                  assert(ans)(equalTo(expected))
              )
            }
          )
          .map(BoolAlgebra.collectAll(_).get)
      )
    )
  )
}
