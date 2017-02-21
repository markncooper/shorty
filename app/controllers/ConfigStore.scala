package controllers

import java.io.{File, PrintWriter}

import com.brigade.shorty.ShortUrl
import com.typesafe.config.{Config, ConfigFactory}
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization
import com.mixpanel.mixpanelapi.{MessageBuilder, MixpanelAPI}

import scala.collection.mutable.HashMap
import scala.io.Source

class ConfigStore {
  val config = ConfigFactory.load()

  val ShortenerUrlsFilename = "config.json"

  val shortUrls = loadConfig()


  private def loadConfig(): HashMap[String, String] ={
    implicit val formats = org.json4s.DefaultFormats

    val lookup = HashMap.empty[String, String]
    Source.fromFile(ShortenerUrlsFilename).getLines().foreach { jsonEntry =>
      val shortUrl = parse(jsonEntry).extract[ShortUrl]
      lookup.put(shortUrl.shortened, shortUrl.original)
    }
    lookup
  }

  private def setupMixpanel(config: Config): Option[MessageBuilder] = {
    val token = config.getString("mixpanel.token")

    if (token.isEmpty){
      None
    } else {
      Option(new MessageBuilder(token))
    }
  }

  private def saveConfig(): Unit ={
    import org.json4s.native.Serialization.write
    implicit val formats = Serialization.formats(NoTypeHints)

    val pw = new PrintWriter(new File(ShortenerUrlsFilename))

    shortUrls.foreach { case (short, original) =>
      val shortUrl = ShortUrl(original, short)
      val json = write(shortUrl)
      pw.write( s"$json\n" )
    }

    pw.close()
  }

  def add(original: String, shortened: String): Unit = {
    shortUrls.put(shortened, original)
    saveConfig()
  }

  def getOriginalUrl(short: String): Option[String] = {
    shortUrls.get(short)
  }

  def getList(): Seq[ShortUrl] = {
    shortUrls.map { short => ShortUrl(short._2, short._1)}.toSeq
  }

  def delete(shortened: String): Unit = {
    shortUrls.remove(shortened)
  }

}

