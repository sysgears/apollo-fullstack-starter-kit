export function up(knex, Promise) {
  return Promise.all([
    knex.schema.createTable('count', (table) => {
      table.increments();
      table.timestamps();
      table.integer('amount').notNull();
    }),
  ]);
}

export function down(knex, Promise) {
  return Promise.all([
    knex.schema.dropTable('count'),
  ]);
}
