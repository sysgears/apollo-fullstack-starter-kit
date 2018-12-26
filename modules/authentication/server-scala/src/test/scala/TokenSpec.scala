import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.testkit.RouteTestTimeout
import akka.testkit.TestDuration
import akka.util.ByteString
import common.implicits.RichDBIO._
import common.routes.graphql.jsonProtocols.GraphQLMessage
import common.routes.graphql.jsonProtocols.GraphQLMessageJsonProtocol._
import modules.jwt.model.JwtContent
import modules.jwt.service.JwtAuthService
import repositories.UserRepository
import spray.json._

import scala.concurrent.duration._

class TokenSpec extends AuthenticationTestHelper {
  implicit val timeout: RouteTestTimeout = RouteTestTimeout(10.seconds.dilated)

  val userRepo: UserRepository = inject[UserRepository]
  val authService: JwtAuthService[JwtContent] = inject[JwtAuthService[JwtContent]]

  val testEmail = "scala-test@gmail.com"
  val testPassword = "12345678q"
  val testUsername = "testName"

  "TokenSpec" must {

    def registrationStep: RouteTestResult = {
      val registerMutation =
        """
          |mutation RegisterUser($input: RegisterUserInput!){
          |	register(input: $input){
          |		user {
          |      id
          |      username
          |      email
          |      role
          |      isActive
          |    }
          |	}
          |}
        """.stripMargin

      val registerVariables: JsObject =
        s"""
           |{
           |	"input": {
           |		"username": "$testUsername",
           |		"email": "$testEmail",
           |		"password": "$testPassword"
           |	}
           |}
      """.stripMargin.parseJson.asJsObject

      Post(endpoint,
        HttpEntity(`application/json`,
          ByteString(GraphQLMessage(registerMutation, None, Some(registerVariables)).toJson.compactPrint)
        )) ~> routes
    }

    "resend tokens [SUCCESS]" in {
      registrationStep ~> check()

      val user = await(userRepo.findByUsernameOrEmail(testEmail).run).get

      val refreshToken = authService.createRefreshToken(JwtContent(1), user.password)

      val mutation =
        """
          |mutation RefreshTokens($input: String!){
          |	refreshTokens(refreshToken: $input){
          |    accessToken
          |    refreshToken
          |  }
          |}
        """.stripMargin

      val variables: JsObject =
        s"""
           |{"input": "$refreshToken"}
      """.stripMargin.parseJson.asJsObject

      val graphQLMessage = ByteString(GraphQLMessage(mutation, None, Some(variables)).toJson.compactPrint)
      val entity = HttpEntity(`application/json`, graphQLMessage)

      Post(endpoint, entity) ~> routes ~> check {
        val response = responseAs[String]

        status shouldBe OK
        contentType.mediaType shouldBe `application/json`
        response should include("\"accessToken\"")
        response should include("\"refreshToken\"")
      }
    }

    "resend tokens [FAIL]" in {
      registrationStep ~> check()

      val user = await(userRepo.findByUsernameOrEmail(testEmail).run).get

      val refreshToken = authService.createRefreshToken(JwtContent(1), user.password)

      await(userRepo.update(user.copy(password = "qwerty")).run)

      val mutation =
        """
          |mutation RefreshTokens($input: String!){
          |	refreshTokens(refreshToken: $input){
          |    accessToken
          |    refreshToken
          |  }
          |}
        """.stripMargin

      val variables: JsObject =
        s"""
           |{"input": "$refreshToken"}
      """.stripMargin.parseJson.asJsObject

      val graphQLMessage = ByteString(GraphQLMessage(mutation, None, Some(variables)).toJson.compactPrint)
      val entity = HttpEntity(`application/json`, graphQLMessage)

      Post(endpoint, entity) ~> routes ~> check {
        val response = responseAs[String]

        status shouldBe OK
        contentType.mediaType shouldBe `application/json`
        response should include("\"data\":null")
      }
    }
  }
}