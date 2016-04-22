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

  def db1 = Action {
      var out = ""
      var conn = DB.getConnection()
      try {
      	  val stmt = conn.createStatement
	  
	  val rs = stmt.executeQuery("SELECT username FROM users")

	  while (rs.next) {
	  	out += "User: " + rs.getString(1) + "\n"
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
      var out = "bad request: user not found"
      val loginRequest = loginForm.bindFromRequest.get
      
      var conn = DB.getConnection()
      try {
      	  val stmt = conn.createStatement
      	  val rs = stmt.executeQuery(s"SELECT username, password FROM users WHERE username = '${loginRequest.username}'")

	  val password = s"${loginRequest.password}"

	  if (rs.next) {
	  	if (rs.getString(2) == password) {
		   out = s"user: '${loginRequest.username}' logged in"
		}
	  }
	  else
		out = "bad request: pwd incorrect"
      } finally {
      	conn.close()
      }
      
      Ok(out)
  }
  
  def doCreateUser = Action {implicit request => 
      val loginRequest = loginForm.bindFromRequest.get

      var conn = DB.getConnection()
      try {
      	  val stmt = conn.createStatement

	  stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (username varchar(20) PRIMARY KEY, password varchar(20))")

	  val p_st = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)")
	  val username = s"${loginRequest.username}"
	  val password = s"${loginRequest.password}"
	  p_st.setString(1, username);
	  p_st.setString(2, password);
	  p_st.executeUpdate(); 

	  
      } finally {
      	conn.close()
      }

      Ok(s"username: ${loginRequest.username} has registered")
  }

  def loginForm = Form(mapping("username" -> text, "password" -> text)
      (LoginRequest.apply) (LoginRequest.unapply))

  case class LoginRequest(username:String, password:String)
}
