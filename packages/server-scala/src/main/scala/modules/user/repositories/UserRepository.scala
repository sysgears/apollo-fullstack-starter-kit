package modules.user.repositories

import com.byteslounge.slickrepo.repository.Repository
import javax.inject.Inject
import modules.user.model.User
import modules.user.model.UserTable.UserTable
import slick.ast.BaseTypedType
import slick.jdbc.JdbcProfile

class UserRepository @Inject()(override val driver: JdbcProfile) extends Repository[User, Int](driver) {

  import driver.api._

  val pkType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[UserTable]
  type TableType = UserTable
}