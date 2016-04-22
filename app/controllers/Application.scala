package controllers

import play.api._
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current
import play.api.data._
import play.api.data.Forms._

import play.api.db._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index(null))
  }

  def db = Action {
    var out = ""
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement

      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)")
      stmt.executeUpdate("INSERT INTO ticks VALUES (now())")

      val rs = stmt.executeQuery("SELECT tick FROM ticks")

      while (rs.next) {
        out += "Read from DB: " + rs.getTimestamp("tick") + "\n"
      }
    } finally {
      conn.close()
    }
    Ok(out)
  }

  def login = Action { 
      Ok(views.html.login(null))
  }

  def doLogin = Action { implicit request => 
      val loginRequest = loginForm.bindFromRequest.get
      Ok(s"username: '${loginRequest.username}', password: '${loginRequest.password}'")
  }
  
  def doCreateUser = Action {implicit request => 
      val loginRequest = loginForm.bindFromRequest.get
      val conn = DB.getConnection()
      try {
      	  val stmt = conn.createStatement

	  stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (username PRIMARY KEY, password varchar(20))")
	  stmt.executeUpdate("INSERT INTO users VALUES ('${loginRequest.username}', '${loginRequest.password}')")
	  
      }
      conn.close()

      Ok(s"username: '${loginRequest.username}' has registered")
  }

  def loginForm = Form(mapping("username" -> text, "password" -> text)
      (LoginRequest.apply) (LoginRequest.unapply))

  case class LoginRequest(username:String, password:String)
}
