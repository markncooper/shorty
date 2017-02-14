package controllers

import javax.inject._

import com.brigade.shorty.ShortUrl
import play.api.mvc._
import views.html

import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms.{default, mapping, text}
import play.api.i18n.Messages.Implicits._

@Singleton
class HomeController @Inject() extends Controller {
  val configStore = new ConfigStore()

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

    Redirect(routes.HomeController.index)
  }

  def delete(short: String) = Action { implicit request =>
    configStore.delete(short)

    Redirect(routes.HomeController.index)
  }

  def redirect(short: String) = Action { implicit request =>

    val originalUrl = configStore.getOriginalUrl(short)

    if (originalUrl.isDefined){
      Redirect(originalUrl.get)
    } else {
      NotFound("Url not found")
    }
  }
}