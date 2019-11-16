package example.server

import com.typesafe.config.ConfigFactory


object Main extends App {
  val defaultConfig = ConfigFactory.load()
  defaultConfig.getString("server.style") match {
    case "typed" => MainTyped.run(defaultConfig)
    case "classic" => MainClassic.run(defaultConfig)
    case _ => println("server.style only supports typed or classic")
  }
}
