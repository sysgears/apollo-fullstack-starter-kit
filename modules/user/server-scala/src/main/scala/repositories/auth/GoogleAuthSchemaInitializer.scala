package repositories.auth

import core.slick.SchemaInitializer
import javax.inject.Inject
import model.oauth.google.GoogleAuthTable
import model.oauth.google.GoogleAuthTable.GoogleAuthTable
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext

class GoogleAuthSchemaInitializer @Inject()(implicit val executionContext: ExecutionContext) extends SchemaInitializer[GoogleAuthTable] {

  override val context = executionContext
  override val name: String = GoogleAuthTable.name
  override val table = TableQuery[GoogleAuthTable]
}