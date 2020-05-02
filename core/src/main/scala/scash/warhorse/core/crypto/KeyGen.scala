package scash.warhorse.core.crypto

import scash.warhorse.Result

trait KeyGen[A] {
  def genPrivkey: Result[PrivateKey]

  def genPubkey(publicKey: PrivateKey): Result[PublicKey]

  def genPubkeyCompressed(privateKey: PrivateKey): Result[PublicKey]
}
