package scash.warhorse.rpc

import zio.Has

package object client {
  type RpcClient = Has[RpcClient.Service]
}
