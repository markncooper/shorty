package controllers

import javax.inject._

import com.brigade.shorty.ShortUrl
import com.mixpanel.mixpanelapi.{ClientDelivery, MessageBuilder, MixpanelAPI}
import com.typesafe.config.{Config, ConfigFactory}
import org.json.JSONObject
import play.api.mvc._
import views.html
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms.{default, mapping, text}
import play.api.i18n.Messages.Implicits._

@Singleton
class HomeController @Inject() extends Controller {
  private val globalConfig = ConfigFactory.load()
  private val configStore = new ConfigStore()
  private val mpMessageBuilder: Option[MessageBuilder] = setupMixpanel(globalConfig)

  private def setupMixpanel(config: Config): Option[MessageBuilder] = {
    val token = config.getString("mixpanel.token")

    if (token.isEmpty){
      None
    } else {
      Option(new MessageBuilder(token))
    }
  }

  /**
    * If Mixpanel integration is enabled log an event
    *
    * @param clientIP
    * @param actionType
    * @param fullUrl
    * @param shortUrl
    */
  private def logEvent(clientIP: String, actionType: String, fullUrl: String, shortUrl: String): Unit = {
    mpMessageBuilder.foreach { messageBuilder =>

      val props = new JSONObject()
      props.put("FullURL", fullUrl)
      props.put("ShortURL", shortUrl)

      val event = messageBuilder.event(clientIP, actionType, props)
      val delivery = new ClientDelivery()
      delivery.addMessage(event)

      val mixpanel = new MixpanelAPI()
      mixpanel.deliver(delivery)
    }
  }

  private val addForm: Form[ShortUrl] = Form(
    mapping(
      "original" -> default(text, ""),
      "shortened" -> default(text, "")
    )(ShortUrl.apply)(ShortUrl.unapply)
  )

  def index = Action { implicit request =>
    val queryForm = addForm.bindFromRequest()

    Ok(html.index(configStore.getList(), addForm))
  }

  def findOrAdd(shortcut: String, original: String) = Action { implicit request =>
    configStore.add(original, shortcut)

    Redirect(routes.HomeController.index)
  }

  def add() = Action { implicit request =>
    val add = addForm.bindFromRequest().get

    configStore.add(add.original, add.shortened)

    logEvent(request.remoteAddress, "ConfigUpdate", add.original, add.shortened)
    Redirect(routes.HomeController.index)
  }

  def delete(short: String) = Action { implicit request =>
    configStore.delete(short)

    logEvent(request.remoteAddress, "ConfigUpdate", "removal", short)
    Redirect(routes.HomeController.index)
  }

  def redirect(short: String) = Action { implicit request =>

    val originalUrl = configStore.getOriginalUrl(short)

    if (originalUrl.isDefined){
      logEvent(request.remoteAddress, "Redirect", originalUrl.get, short)
      Redirect(originalUrl.get)
    } else {
      logEvent(request.remoteAddress, "Redirect", "not found", short)
      NotFound("Url not found")
    }
  }
}