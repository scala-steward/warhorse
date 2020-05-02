package scash.warhorse.core.crypto

import org.bouncycastle.crypto.params.ECDomainParameters

trait ECCurve[A] {
  val domain: ECDomainParameters
}
