import * as Knex from 'knex';

export const up = async (knex: Knex, Promise: any) => {
  return Promise.all([
    knex.schema.createTable('user', table => {
      table.increments();
      table.string('username').unique();
      table.boolean('is_admin').defaultTo(false);
      table.boolean('is_active').defaultTo(false);
      table.timestamps(false, true);
    }),
    knex.schema.createTable('local_auth', table => {
      table.increments();
      table.string('email').unique();
      table.string('password');
      table
        .integer('user_id')
        .unsigned()
        .references('id')
        .inTable('user')
        .onDelete('CASCADE');
      table.timestamps(false, true);
    }),
    knex.schema.createTable('cert_auth', table => {
      table.increments();
      table.string('serial').unique();
      table
        .integer('user_id')
        .unsigned()
        .references('id')
        .inTable('user')
        .onDelete('CASCADE');
      table.timestamps(false, true);
    })
  ]);
};

export const down = async (knex: Knex, Promise: any) => {
  return Promise.all([
    knex.schema.dropTable('user'),
    knex.schema.dropTable('local_auth'),
    knex.schema.dropTable('cert_auth')
  ]);
};
