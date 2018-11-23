const shell = require('shelljs');
const fs = require('fs');
const chalk = require('chalk');
const { decamelize } = require('humps');
const { copyFiles, renameFiles, computeModulesPath, computePackagePath, runPrettier } = require('../helpers/util');

/**
 * Adds application module to client or server code and adds it to the module list.
 *
 * @param logger - The Logger.
 * @param templatesPath - The path to the templates for a new module.
 * @param moduleName - The name of a new module.
 * @param options - User defined options
 * @param location - The location for a new module [client|server|both].
 * @param finished - The flag about the end of the generating process.
 */
function addModule(logger, templatesPath, moduleName, options, location, finished = true) {
  logger.info(`Copying ${location} files…`);

  // create new module directory
  const destinationPath = computeModulesPath(location, options, moduleName);
  const newModule = shell.mkdir('-p', destinationPath);

  // continue only if directory does not jet exist
  if (newModule.code !== 0) {
    logger.error(chalk.red(`The ${moduleName} directory is already exists.`));
    process.exit();
  }
  //copy and rename templates in destination directory
  copyFiles(destinationPath, templatesPath, location);
  renameFiles(destinationPath, moduleName);

  logger.info(chalk.green(`✔ The ${location} files have been copied!`));

  // get index file path
  const modulesPath = computeModulesPath(location, options);
  const indexFullFileName = fs.readdirSync(modulesPath).find(name => name.search(/index/) >= 0);
  const indexPath = modulesPath + indexFullFileName;
  let indexContent;

  try {
    // prepend import module
    const importFrom = options.old
      ? `./${moduleName}`
      : `@module/${decamelize(moduleName, {
          separator: '-'
        })}-${location}`;
    indexContent = `import ${moduleName} from '${importFrom}';\n` + fs.readFileSync(indexPath);
  } catch (e) {
    logger.error(chalk.red(`Failed to read ${indexPath} file`));
    process.exit();
  }

  // extract application modules
  const appModuleRegExp = /Module\(([^()]+)\)/g;
  const [, appModules] = appModuleRegExp.exec(indexContent) || ['', ''];

  // add module to app module list
  shell
    .ShellString(indexContent.replace(RegExp(appModuleRegExp, 'g'), `Module(${moduleName}, ${appModules})`))
    .to(indexPath);
  runPrettier(indexPath);

  // get package content
  const packagePath = computePackagePath(location);
  const packageContent = `` + fs.readFileSync(packagePath);

  // extract dependencies
  const dependenciesRegExp = /"dependencies":\s\{([^()]+)\},\n\s+"devDependencies"/g;
  const [, dependencies] = dependenciesRegExp.exec(packageContent) || ['', ''];

  // insert package and sort
  const dependenciesSorted = dependencies.split(',');
  dependenciesSorted.push(
    `\n    "@module/${decamelize(moduleName, {
      separator: '-'
    })}-${location}": "^1.0.0"`
  );
  dependenciesSorted.sort();

  // add module to package list
  shell
    .ShellString(
      packageContent.replace(
        RegExp(dependenciesRegExp, 'g'),
        `"dependencies": {${dependenciesSorted}},\n  "devDependencies"`
      )
    )
    .to(packagePath);

  if (finished) {
    logger.info(chalk.green(`✔ Module for ${location} successfully created!`));
  }
}

module.exports = addModule;
