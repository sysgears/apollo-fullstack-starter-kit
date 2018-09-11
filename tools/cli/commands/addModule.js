const shell = require('shelljs');
const fs = require('fs');
const chalk = require('chalk');
const { copyFiles, renameFiles, computeModulesPath } = require('../helpers/util');

/**
 * Add module in client or server and add new module to the Feature connector
 * @param logger
 * @param templatesPath
 * @param module
 * @param location
 * @param finished
 */
function addModule(logger, templatesPath, module, location, finished = true) {
  logger.info(`Copying ${location} files…`);

  // create new module directory
  const modulePath = computeModulesPath(location, module);
  const newModule = shell.mkdir(modulePath);

  // continue only if directory does not jet exist
  if (newModule.code !== 0) {
    logger.error(chalk.red(`The ${module} directory is already exists.`));
    process.exit();
  }
  //copy and rename templates in destination directory
  copyFiles(modulePath, templatesPath, location);
  renameFiles(modulePath, module);

  logger.info(chalk.green(`✔ The ${location} files have been copied!`));

  // get index file path
  const modulesPath = computeModulesPath(location);
  const indexFullFileName = fs.readdirSync(modulesPath).find(name => name.search(/index/) >= 0);
  const indexPath = modulesPath + indexFullFileName;
  let indexContent;

  try {
    // prepend import module
    indexContent = `import ${module} from './${module}';\n` + fs.readFileSync(indexPath);
  } catch (e) {
    logger.error(chalk.red(`Failed to read ${indexPath} file`));
    process.exit();
  }

  // extract Feature modules
  const featureRegExp = /Feature\(([^()]+)\)/g;
  const [, featureModules] = featureRegExp.exec(indexContent) || ['', ''];

  // add module to Feature connector
  shell
    .ShellString(indexContent.replace(RegExp(featureRegExp, 'g'), `Feature(${module}, ${featureModules})`))
    .to(indexPath);

  if (finished) {
    logger.info(chalk.green(`✔ Module for ${location} successfully created!`));
  }
}

module.exports = addModule;
