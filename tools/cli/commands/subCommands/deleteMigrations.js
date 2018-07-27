const shell = require('shelljs');
const chalk = require('chalk');
const { pascalize } = require('humps');
const { migrationsDir, seedsDir } = require('../../config');

/**
 * Delete migrations and seeds
 * @param logger
 * @param module
 */
function deleteMigrations(logger, module) {
  const Module = pascalize(module);
  const startPath = `${__dirname}/../../../..`;
  // change to database migrations directory
  shell.cd(`${startPath}${migrationsDir}`);
  // check if any migrations files for this module exist
  if (shell.find('.').find(file => file.search(`_${Module}.js`) > -1)) {
    const okMigrations = shell.rm(`*_${Module}.js`);
    if (okMigrations) {
      logger.info(chalk.green(`✔ Database migrations files successfully deleted!`));
    }
  }

  // change to database seeds directory
  shell.cd(`${startPath}${seedsDir}`);
  // check if any seed files for this module exist
  if (shell.find('.').find(file => file.search(`_${Module}.js`) > -1)) {
    const okSeeds = shell.rm(`*_${Module}.js`);
    if (okSeeds) {
      logger.info(chalk.green(`✔ Database seed files successfully deleted!`));
    }
  }
}

module.exports = deleteMigrations;
