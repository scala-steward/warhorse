package scash.warhorse.core.blockchain

import scodec.bits.ByteVector

import io.circe.Decoder

import scash.warhorse.util.parseJsonfromFile
import scash.warhorse.core._
import scash.warhorse.core.crypto.hash.DoubleSha256B
import scash.warhorse.util._

import zio.ZIO
import zio.test.DefaultRunnableSpec
import zio.test._

object TransactionSpec extends DefaultRunnableSpec {
  case class TxTest(hex: ByteVector, txidB: DoubleSha256B)

  implicit val txlistDecoder: Decoder[List[TxTest]] =
    csvDecoder.map(dataset =>
      dataset.map(str =>
        TxTest(ByteVector.fromValidHex(str(0)), ByteVector.fromValidHex(str(1)).decode_[DoubleSha256B])
      )
    )

  val spec = suite("TransactionSpec")(
    testM("symmetry fromHex") {
      parseJsonfromFile[List[TxTest]]("txhex.json")
        .flatMap(r =>
          ZIO.foreach(r.require)(txt =>
            ZIO.succeed {
              val tx = txt.hex.decode[Transaction]
              assert(tx.map(_.txIdB))(success(txt.txidB)) &&
              assert(tx.map(_.txId))(success(DoubleSha256B.toLittleEndian(txt.txidB))) &&
              assert(tx.map(_.bytes))(success(txt.hex))
            }
          )
        )
        .map(BoolAlgebra.collectAll(_).get)
    }
  )
}
