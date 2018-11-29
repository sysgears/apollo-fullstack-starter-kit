package repositories

import core.slick.{SchemaLoader, TableInitializer}
import javax.inject.Inject
import model.ItemTable.ItemTable
import model.{Item, ItemTable}
import slick.jdbc.SQLiteProfile.api._
import slick.lifted.TableQuery

/** @inheritdoc */
class ItemSchemaInitializer @Inject()(database: Database)
  extends TableInitializer[ItemTable](ItemTable.name, TableQuery[ItemTable], database)
    with SchemaLoader {

  /** @inheritdoc */
  override def seedDatabase(tableQuery: TableQuery[ItemTable]): DBIOAction[_, NoStream, Effect.Write] = {
    val items = List.range(1, 100).map(num => Item(Some(num), s"Item $num"))
    tableQuery ++= items
  }
}