package scash.warhorse.gen

import scash.warhorse._

//TODO: change to fixed constant scriptSig
trait ScriptGen {

  def scriptSig = gen.byteVectorBounded(1, 100)

  def scriptPubKey = gen.byteVectorBounded(1, 100)
}
