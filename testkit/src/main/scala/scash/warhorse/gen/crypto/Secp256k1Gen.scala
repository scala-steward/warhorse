package scash.warhorse.gen.crypto

import scash.warhorse.core._
import scash.warhorse.core.crypto.{ PrivateKey, PublicKey }
import zio.random
import zio.test.Gen

trait Secp256k1Gen {

  def privKey: Gen[Any, PrivateKey] = Gen.const(crypto.genPrivkey.require)

  def pubkey: Gen[random.Random, PublicKey] = keyPair.map(_._2)

  def pubKey(p: PrivateKey): Gen[Any, PublicKey] = Gen.const(crypto.genPubkey(p).require)

  def pubKeyCompressed(p: PrivateKey): Gen[Any, PublicKey] = Gen.const(crypto.genPubkeyCompressed(p).require)

  def keyPair: Gen[random.Random, (PrivateKey, PublicKey)] =
    for {
      priv <- privKey
      pub  <- Gen.oneOf(pubKeyCompressed(priv), pubKey(priv))
    } yield (priv, pub)

}
