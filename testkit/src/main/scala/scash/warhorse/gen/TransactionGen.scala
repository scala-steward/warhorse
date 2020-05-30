package scash.warhorse.gen
import scash.warhorse._
import scash.warhorse.core.blockchain.{ Transaction, TransactionInput, TransactionOutPoint, TransactionOutput }
import scash.warhorse.core.number.{ Int32, Uint32 }
import zio.random.Random
import zio.test.{ Gen, Sized }

//TODO: replace for more realistic transaction
trait TransactionGen {

  def outpoint: Gen[Random with Sized, TransactionOutPoint] =
    for {
      txId <- gen.doubleSha256
      vout <- gen.uint32(Uint32.one, Uint32(500))
    } yield TransactionOutPoint(txId, vout)

  def input: Gen[Random with Sized, TransactionInput] =
    for {
      outPoint  <- outpoint
      scriptSig <- gen.scriptSig
      sequence  <- Gen.const(Uint32(429467295))
    } yield TransactionInput(outPoint, scriptSig, sequence)

  def inputs: Gen[Random with Sized, List[TransactionInput]] = Gen.listOfBounded(1, 1000)(input)

  def outputs: Gen[Random with Sized, List[TransactionOutput]] = Gen.listOfBounded(1, 1000)(output)

  def output =
    for {
      coins  <- gen.posInt64
      pubkey <- gen.scriptPubKey
    } yield TransactionOutput(coins, pubkey)

  def transaction =
    for {
      version  <- Gen.elements(Int32(1), Int32(2))
      in       <- inputs
      out      <- outputs
      lockTime <- gen.uint32
    } yield Transaction(version, in, out, lockTime)
}
