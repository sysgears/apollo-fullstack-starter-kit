package routes.auth

import akka.http.scaladsl.model.StatusCodes
import com.github.scribejava.core.oauth.OAuth20Service
import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.google.inject.Inject
import com.google.inject.name.Named
import common.errors.AmbigousResult
import core.controllers.AkkaRoute
import model.User
import modules.jwt.model.JwtContent
import modules.jwt.model.Tokens._
import modules.jwt.service.JwtAuthService
import org.mindrot.jbcrypt.BCrypt
import repositories.UserRepository
import repositories.auth.GithubAuthRepository
import services.ExternalApiService
import common.implicits.RichDBIO._
import common.implicits.RichFuture._
import model.oauth.github.{GithubAuth, GithubOauth2Response}
import spray.json._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class GithubAuthController @Inject()(@Named("github") oauth2Service: OAuth20Service,
                                     externalApiService: ExternalApiService,
                                     userRepository: UserRepository,
                                     githubAuthRepository: GithubAuthRepository,
                                     jwtAuthService: JwtAuthService[JwtContent])
                                    (implicit val executionContext: ExecutionContext) extends AkkaRoute {

  override val routes: Route =
    (path("auth" / "github") & get) {
      redirect(oauth2Service.getAuthorizationUrl, StatusCodes.Found)
    } ~ (path("auth" / "github" / "callback") & get) {
      parameters('state.?, 'code) { (state, code) =>
        onComplete {
          for {
            githubAuthInfo <- externalApiService.getUserInfo[GithubOauth2Response](code, "https://api.github.com/user", oauth2Service)
            user <- githubAuthRepository.findOne(githubAuthInfo.id).run.flatMap {
              case Some(ghUser) => userRepository.findOne(ghUser.userId).run failOnNone AmbigousResult(s"User with id: ${ghUser.userId} stored in github auth table, but not in the user table")
              case None => for {
                user <- userRepository.save(
                  User(
                    username = githubAuthInfo.name,
                    email = githubAuthInfo.email,
                    password = BCrypt.hashpw(githubAuthInfo.id.toString + githubAuthInfo.name, BCrypt.gensalt),
                    role = "user",
                    isActive = true)
                ).run
                _ <- githubAuthRepository.save(GithubAuth(Some(githubAuthInfo.id), githubAuthInfo.name, user.id.get)).run
              } yield user
            }
          } yield jwtAuthService.createTokens(JwtContent(user.id.get), user.password)
        } {
          case Success(tokens) =>
            setCookie(HttpCookie("access-token", value = tokens.accessToken), HttpCookie("refresh-token", value = tokens.refreshToken)) {
              state match {
                case Some(redirectUrl) => redirect(s"$redirectUrl?data=${tokens.toJson.toString}", StatusCodes.Found)
                case None => redirect("/profile", StatusCodes.Found)
              }
            }
          case Failure(_) => redirect("/login", StatusCodes.Found)
        }
      }
    }
}