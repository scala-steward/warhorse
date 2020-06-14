package scash.warhorse.core.blockchain

import zio.ZIO
import zio.test.DefaultRunnableSpec
import zio.test._

import scash.warhorse.util._

object CashAddrSpec extends DefaultRunnableSpec {

  val fromLegacyList = List(
    ("1BpEi6DfDAUFd7GtittLSdBeYJvcoaVggu", "bitcoincash:qpm2qsznhks23z7629mms6s4cwef74vcwvy22gdx6a"),
    ("1KXrWXciRDZUpQwQmuM1DbwsKDLYAYsVLR", "bitcoincash:qr95sy3j9xwd2ap32xkykttr4cvcu7as4y0qverfuy"),
    ("16w1D5WRVKJuZUsSRzdLp9w3YGcgoxDXb", "bitcoincash:qqq3728yw0y47sqn6l2na30mcw6zm78dzqre909m2r"),
    ("3CWFddi6m4ndiGyKqzYvsFYagqDLPVMTzC", "bitcoincash:ppm2qsznhks23z7629mms6s4cwef74vcwvn0h829pq"),
    ("3LDsS579y7sruadqu11beEJoTjdFiFCdX4", "bitcoincash:pr95sy3j9xwd2ap32xkykttr4cvcu7as4yc93ky28e"),
    ("31nwvkZwyPdgzjBJZXfDmSWsC4ZLKpYyUw", "bitcoincash:pqq3728yw0y47sqn6l2na30mcw6zm78dzq5ucqzc37")
  )

  val spec = suite("CashAddrSpec")(
    testM("fromLegacyAddr") {
      ZIO
        .foreach(fromLegacyList) {
          case (legStr, cashaddr) =>
            ZIO.succeed(
              assert(LegacyAddr.fromString(legStr).map(CashAddr.fromLegacyAddr(_).value))(success(cashaddr))
            )
        }
        .map(BoolAlgebra.collectAll(_).get)
    }
  )

}
