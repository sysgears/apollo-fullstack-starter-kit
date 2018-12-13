package repositories.auth

import core.slick.SchemaInitializer
import javax.inject.Inject
import model.oauth.linkedin.LinkedinAuthTable
import model.oauth.linkedin.LinkedinAuthTable.LinkedinAuthTable
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext

class LinkedinAuthSchemaInitializer @Inject()(implicit val executionContext: ExecutionContext) extends SchemaInitializer[LinkedinAuthTable] {

  override val context = executionContext
  override val name: String = LinkedinAuthTable.name
  override val table = TableQuery[LinkedinAuthTable]
}