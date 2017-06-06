import webpack from 'webpack';
import webpackDevMiddleware from 'webpack-dev-middleware';
import webpackHotMiddleware from 'webpack-hot-middleware';
import httpProxyMiddleware from 'http-proxy-middleware';
import express from 'express';
import compression from 'compression';
import http from 'http';
import { spawn } from 'child_process';
import fs from 'fs';
import path from 'path';
import mkdirp from 'mkdirp';
import minilog from 'minilog';
import mime from 'mime';
import _ from 'lodash';
import crypto from 'crypto';
import VirtualModules from 'webpack-virtual-modules';
import waitOn from 'wait-on';
import { Android, Simulator, Config, Project, ProjectSettings, Exp, UrlUtils } from 'xdl';
import qr from 'qrcode-terminal';
import { RawSource } from 'webpack-sources';
import symbolicateMiddleware from 'haul-cli/src/server/middleware/symbolicateMiddleware';
import { fromStringWithSourceMap, SourceListMap } from 'source-list-map';

import liveReloadMiddleware from './middleware/liveReloadMiddleware';
import pkg from '../package.json';
// eslint-disable-next-line import/default
import configs from './webpack.config';

const InspectorProxy = require('react-native/local-cli/server/util/inspectorProxy.js');
const copyToClipBoardMiddleware = require('react-native/local-cli/server/middleware/copyToClipBoardMiddleware');
const cpuProfilerMiddleware = require('react-native/local-cli/server/middleware/cpuProfilerMiddleware');
const getDevToolsMiddleware = require('react-native/local-cli/server/middleware/getDevToolsMiddleware');
const heapCaptureMiddleware = require('react-native/local-cli/server/middleware/heapCaptureMiddleware.js');
const indexPageMiddleware = require('react-native/local-cli/server/middleware/indexPage');
const loadRawBodyMiddleware = require('react-native/local-cli/server/middleware/loadRawBodyMiddleware');
const messageSocket = require('react-native/local-cli/server/util/messageSocket.js');
const openStackFrameInEditorMiddleware = require('react-native/local-cli/server/middleware/openStackFrameInEditorMiddleware');
const statusPageMiddleware = require('react-native/local-cli/server/middleware/statusPageMiddleware.js');
const systraceProfileMiddleware = require('react-native/local-cli/server/middleware/systraceProfileMiddleware.js');
const unless = require('react-native/local-cli/server/middleware/unless');
const webSocketProxy = require('react-native/local-cli/server/util/webSocketProxy.js');

minilog.enable();

process.on('uncaughtException', (ex) => {
  console.error(ex);
});

process.on('unhandledRejection', reason => {
  console.error(reason);
});

const logBack = minilog('webpack-for-backend');

const [serverConfig, clientConfig, dllConfig, androidConfig, iOSConfig] = configs;
const __WINDOWS__ = /^win/.test(process.platform);

let server;
let startBackend = false;
let backendFirstStart = !serverConfig.url;

process.on('exit', () => {
  if (server) {
    server.kill('SIGTERM');
  }
});

function runServer(path) {
  if (startBackend) {
    startBackend = false;
    backendFirstStart = false;
    logBack('Starting backend');
    server = spawn('node', [path], { stdio: [0, 1, 2] });
    server.on('exit', code => {
      if (code === 250) {
        // App requested full reload
        startBackend = true;
      }
      logBack('Backend has been stopped');
      server = undefined;
      runServer(path);
    });
  }
}

function webpackReporter(outputPath, log, err, stats) {
  if (err) {
    log(err.stack);
    throw new Error("Build error");
  }
  if (stats) {
    log(stats.toString({
      hash: false,
      version: false,
      timings: true,
      assets: false,
      chunks: false,
      modules: false,
      reasons: false,
      children: false,
      source: true,
      errors: true,
      errorDetails: true,
      warnings: true,
      publicPath: false,
      colors: true
    }));

    if (!__DEV__) {
      mkdirp.sync(outputPath);
      fs.writeFileSync(path.join(outputPath, 'stats.json'), JSON.stringify(stats.toJson()));
    }
  }
}

let frontendVirtualModules = new VirtualModules({ 'node_modules/backend_reload.js': '' });

function startClient(config, platform) {
  const logger = minilog(`webpack-for-${config.name}`);
  try {
    const reporter = (...args) => webpackReporter(config.output.path, logger, ...args);

    config.plugins.push(frontendVirtualModules);

    if (__DEV__) {
      if (['android', 'ios'].indexOf(platform) >= 0) {
        startExpoServer(config, platform);
      }
      _.each(config.entry, entry => {
        if (pkg.app.reactHotLoader) {
          entry.unshift('react-hot-loader/patch');
        }
        entry.unshift(
          `webpack-hot-middleware/client?http://localhost:${config.devServer.port}/`,
          'webpack/hot/dev-server');
      });
      config.plugins.push(new webpack.HotModuleReplacementPlugin(),
        new webpack.NoEmitOnErrorsPlugin());
      startWebpackDevServer(config, platform, reporter, logger);
    } else {
      const compiler = webpack(config);

      compiler.run(reporter);
    }
  } catch (err) {
    logger(err.message, err.stack);
  }
}


let backendReloadCount = 0;
function increaseBackendReloadCount() {
  backendReloadCount++;
  frontendVirtualModules.writeModule('node_modules/backend_reload.js',
    `var count = ${backendReloadCount};\n`);
}

function startServer() {
  try {
    const reporter = (...args) => webpackReporter(serverConfig.output.path, logBack, ...args);

    if (__DEV__) {
      _.each(serverConfig.entry, entry => {
        if (__WINDOWS__) {
          entry.push('webpack/hot/poll?1000');
        } else {
          entry.push('webpack/hot/signal.js');
        }
      });
      serverConfig.plugins.push(new webpack.HotModuleReplacementPlugin(),
        new webpack.NoEmitOnErrorsPlugin());
    }

    const compiler = webpack(serverConfig);

    if (__DEV__) {
      compiler.plugin('compilation', compilation => {
        compilation.plugin('after-optimize-assets', assets => {
          // Patch webpack-generated original source files path, by stripping hash after filename
          const mapKey = _.findKey(assets, (v, k) => k.endsWith('.map'));
          if (mapKey) {
            let srcMap = JSON.parse(assets[mapKey]._value);
            for (let idx in srcMap.sources) {
              srcMap.sources[idx] = srcMap.sources[idx].split(';')[0];
            }
            assets[mapKey]._value = JSON.stringify(srcMap);
          }
        });
      });

      compiler.watch({}, reporter);

      compiler.plugin('done', stats => {
        const { output } = serverConfig;
        startBackend = true;
        if (server) {
          if (!__WINDOWS__) {
            server.kill('SIGUSR2');
          }

          if (pkg.app.frontendRefreshOnBackendChange) {
            for (let module of stats.compilation.modules) {
              if (module.built && module.resource &&
                module.resource.indexOf(path.resolve('./src/server')) === 0) {
                // Force front-end refresh on back-end change
                logBack.debug('Force front-end current page refresh, due to change in backend at:', module.resource);
                increaseBackendReloadCount();
                break;
              }
            }
          }
        } else {
          runServer(path.join(output.path, 'index.js'));
        }
      });
    } else {
      compiler.run(reporter);
    }
  } catch (err) {
    logBack(err.message, err.stack);
  }
}

let vendorHashesJson, vendorSourceListMap;

function startWebpackDevServer(config, platform, reporter, logger) {
  const configOutputPath = config.output.path;
  config.output.path = '/';

  config.plugins.push(frontendVirtualModules);

  let compiler = webpack(config);

  compiler.plugin('after-emit', (compilation, callback) => {
    if (backendFirstStart) {
      logger.debug("Webpack dev server is waiting for backend to start...");
      waitOn({ resources: [`tcp:localhost:${pkg.app.apiPort}`] }, err => {
        if (err) {
          logger.error(err);
        } else {
          logger.debug("Backend has been started, resuming webpack dev server...");
          callback();
        }
      });
    } else {
      callback();
    }
  });
  if (pkg.app.webpackDll && platform !== 'web' && __DEV__) {
    compiler.plugin('after-compile', (compilation, callback) => {
      _.each(compilation.chunks, chunk => {
        _.each(chunk.files, file => {
          if (!file.endsWith('.map')) {
            let sourceListMap = new SourceListMap();
            sourceListMap.add(vendorSourceListMap);
            sourceListMap.add(fromStringWithSourceMap(compilation.assets[file].source(),
              JSON.parse(compilation.assets[file + ".map"].source())));
            let sourceAndMap = sourceListMap.toStringWithSourceMap({ file });
            compilation.assets[file] = new RawSource(sourceAndMap.source);
            compilation.assets[file + ".map"] = new RawSource(JSON.stringify(sourceAndMap.map));
          }
        });
      });
      callback();
    });
  }
  compiler.plugin('done', stats => {
    const dir = configOutputPath;
    mkdirp.sync(dir);
    if (stats.compilation.assets['assets.json']) {
      const assetsMap = JSON.parse(stats.compilation.assets['assets.json'].source());
      _.each(stats.toJson().assetsByChunkName, (assets, bundle) => {
        const bundleJs = assets.constructor === Array ? assets[0] : assets;
        assetsMap[`${bundle}.js`] = bundleJs;
        if (assets.length > 1) {
          assetsMap[`${bundle}.js.map`] = `${bundleJs}.map`;
        }
      });
      if (pkg.app.webpackDll) {
        assetsMap['vendor.js'] = vendorHashesJson.name;
      }
      fs.writeFileSync(path.join(dir, 'assets.json'), JSON.stringify(assetsMap));
    }
  });

  const app = express();

  const serverInstance = http.createServer(app);

  let wsProxy, ms, inspectorProxy;

  if (platform !== 'web') {
    mime.define({ 'application/javascript': ['bundle'] });

    inspectorProxy = new InspectorProxy();
    const args = {port: config.devServer.port, projectRoots: [path.resolve('.')]};
    app
      .use(loadRawBodyMiddleware)
      // .use(function(req, res, next) {
      //   console.log("req:", req.url, req.rawBody);
      //   next();
      // })
      .use(compression())
      .use(getDevToolsMiddleware(args, () => wsProxy && wsProxy.isChromeConnected()))
      .use(getDevToolsMiddleware(args, () => ms && ms.isChromeConnected()))
      .use(liveReloadMiddleware(compiler))
      .use(symbolicateMiddleware(compiler))
      .use(openStackFrameInEditorMiddleware(args))
      .use(copyToClipBoardMiddleware)
      .use(statusPageMiddleware)
      .use(systraceProfileMiddleware)
      .use(heapCaptureMiddleware)
      .use(cpuProfilerMiddleware)
      .use(indexPageMiddleware)
      .use(unless('/inspector', inspectorProxy.processRequest.bind(inspectorProxy)));
  }

  // app.use('/', express.static(path.resolve('.'), { maxAge: '180 days' }));
  app.use(webpackDevMiddleware(compiler, _.merge({}, config.devServer, {
    reporter({ state, stats }) {
      if (state) {
        logger("bundle is now VALID.");
      } else {
        logger("bundle is now INVALID.");
      }
      reporter(null, stats);
    }
  })))
    .use(webpackHotMiddleware(compiler, { log: false }));

  if (config.devServer.proxy) {
    Object.keys(config.devServer.proxy).forEach(key => {
      app.use(httpProxyMiddleware(key, config.devServer.proxy[key]));
    });
  }

  logger(`Webpack ${config.name} dev server listening on ${config.devServer.port}`);
  serverInstance.listen(config.devServer.port, function() {
    if (platform !== 'web') {
      wsProxy = webSocketProxy.attachToServer(serverInstance, '/debugger-proxy');
      ms = messageSocket.attachToServer(serverInstance, '/message');
      webSocketProxy.attachToServer(serverInstance, '/devtools');
      inspectorProxy.attachToServer(serverInstance, '/inspector');
    }
  });
  serverInstance.timeout = 0;
}

function useWebpackDll() {
  console.log("Using Webpack DLL vendor bundle");
  const jsonPath = path.join('..', pkg.app.frontendBuildDir, 'vendor_dll.json');
  [clientConfig, androidConfig, iOSConfig].forEach(config =>
    config.plugins.push(new webpack.DllReferencePlugin({
      context: process.cwd(),
      manifest: require(jsonPath) // eslint-disable-line import/no-dynamic-require
    }))
  );
  serverConfig.plugins.push(new webpack.DllReferencePlugin({
    context: process.cwd(),
    manifest: require(jsonPath) // eslint-disable-line import/no-dynamic-require
  }));
  vendorHashesJson = JSON.parse(fs.readFileSync(path.join(pkg.app.frontendBuildDir, 'vendor_dll_hashes.json')));
  vendorSourceListMap = fromStringWithSourceMap(
    fs.readFileSync(path.join(pkg.app.frontendBuildDir, vendorHashesJson.name)).toString() + "\n",
    JSON.parse(fs.readFileSync(path.join(pkg.app.frontendBuildDir, vendorHashesJson.name + ".map")).toString())
  );
}

function isDllValid() {
  try {
    const hashesPath = path.join(pkg.app.frontendBuildDir, 'vendor_dll_hashes.json');
    if (!fs.existsSync(hashesPath)) {
      console.warn("Vendor DLL does not exists");
      return false;
    }
    let meta = JSON.parse(fs.readFileSync(hashesPath));
    if (!fs.existsSync(path.join(pkg.app.frontendBuildDir, meta.name))) {
      console.warn("Vendor DLL does not exists");
      return false;
    }
    if (!_.isEqual(meta.modules, dllConfig.entry.vendor)) {
      console.warn('Modules bundled into vendor DLL changed, need to rebuild it');
      return false;
    }

    let json = JSON.parse(fs.readFileSync(path.join(pkg.app.frontendBuildDir, 'vendor_dll.json')));

    for (let filename of Object.keys(json.content)) {
      if (filename.indexOf(' ') < 0) {
        if (!fs.existsSync(filename)) {
          console.warn("Vendor bundle need to be regenerated, file: " + filename + " is missing.");
          return false;
        }
        const hash = crypto.createHash('md5').update(fs.readFileSync(filename)).digest('hex');
        if (meta.hashes[filename] !== hash) {
          console.warn(`Hash for vendor DLL file ${filename} changed, need to rebuild vendor bundle`);
          return false;
        }
      }
    }

    return true;
  } catch (e) {
    console.warn('Error checking vendor bundle, regenerating it...', e);

    return false;
  }
}

function buildDll() {
  return new Promise((done) => {
    if (!isDllValid()) {
      console.log("Generating vendor DLL bundle...");

      const compiler = webpack(dllConfig);

      compiler.run((err, stats) => {
        let json = JSON.parse(fs.readFileSync(path.join(pkg.app.frontendBuildDir, 'vendor_dll.json')));
        const vendorKey = _.findKey(stats.compilation.assets,
          (v, key) => key.startsWith('vendor') && key.endsWith('_dll.js'));
        const meta = { name: vendorKey, hashes: {}, modules: dllConfig.entry.vendor };
        for (let filename of Object.keys(json.content)) {
          if (filename.indexOf(' ') < 0) {
            meta.hashes[filename] = crypto.createHash('md5').update(fs.readFileSync(filename)).digest('hex');
            fs.writeFileSync(path.join(pkg.app.frontendBuildDir, 'vendor_dll_hashes.json'), JSON.stringify(meta));
          }
        }
        done();
      });
    } else {
      done();
    }
  });
}

async function startExpoServer(config, platform) {
  try {
    Config.validation.reactNativeVersionWarnings = false;
    Config.developerTool = 'crna';
    Config.offline = true;
    Exp.determineEntryPointAsync = () => `index.${platform}`;

    const projectRoot = path.resolve('.');
    await Project.startExpoServerAsync(projectRoot);
    await ProjectSettings.setPackagerInfoAsync(projectRoot, {
      packagerPort: config.devServer.port
    });

    const address = await UrlUtils.constructManifestUrlAsync(projectRoot);
    console.log("Expo address:", address);
    console.log("To open this app on your phone scan this QR code in Expo Client (if it doesn't get started automatically)");
    qr.generate(address, code => {
      console.log(code);
    });
    if (platform === 'android') {
      const { success, error } = await Android.openProjectAsync(projectRoot);

      if (!success) {
        console.error(error.message);
      }
    } else if (platform === 'ios') {
      const localAddress = await UrlUtils.constructManifestUrlAsync(projectRoot, {
        hostType: 'localhost',
      });
      const { success, error } = await Simulator.openUrlInSimulatorSafeAsync(localAddress);

      if (!success) {
        console.error(error);
      }
    }
  } catch (e) {
    console.error(e.stack);
  }
}

function startWebpack() {
  if (!serverConfig.url) {
    startServer();
  }
  startClient(clientConfig, "web");
  if (pkg.app.android) {
    startClient(androidConfig, "android");
  }
  if (pkg.app.ios) {
    startClient(iOSConfig, "ios");
  }
}

if (!__DEV__ || !pkg.app.webpackDll) {
  startWebpack();
} else {
  buildDll().then(function () {
    useWebpackDll();
    startWebpack();
  });
}
