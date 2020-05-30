package scash.warhorse

import scash.warhorse.gen.crypto.{ CryptoGen, IntGen, Secp256k1Gen, UintGen }
import zio.test.Gen

package object gen
    extends NumGen
    with IntGen
    with UintGen
    with ByteVectorGen
    with Secp256k1Gen
    with CryptoGen
    with ScriptGen
    with TransactionGen {
  def randomMessage = Gen.string1(Gen.anyChar)
}
