import bcrypt from 'bcryptjs';
import { returnId, truncateTables } from '../../sql/helpers';

export async function seed(knex, Promise) {
  await truncateTables(knex, Promise, ['user', 'user_profile', 'auth_certificate', 'auth_facebook']);

  await returnId(knex('user'))
    .insert({
      username: 'admin',
      email: 'admin@example.com',
      password_hash: await bcrypt.hash('admin', 12),
      role: 'admin',
      is_active: true
    });

  await returnId(knex('user'))
    .insert({
      username: 'user1',
      email: 'user@example.com',
      password_hash: await bcrypt.hash('user', 12),
      role: 'user',
      is_active: true
    });
}
