package warhorse

import zio.{ console, ZIO }
import zio.console.Console

object Hello {
  def sayHello: ZIO[Console, Nothing, Unit] =
    console.putStrLn("Hello, World!")
}
