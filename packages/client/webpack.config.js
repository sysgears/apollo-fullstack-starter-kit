/* eslint-disable import/no-extraneous-dependencies */
const webpack = require('webpack');
const path = require('path');
const waitOn = require('wait-on');

const MiniCSSExtractPlugin = require('mini-css-extract-plugin');
const ManifestPlugin = require('webpack-manifest-plugin');
const CleanWebpackPlugin = require('clean-webpack-plugin');
const ForkTsCheckerWebpackPlugin = require('fork-ts-checker-webpack-plugin');

const webpackPort = 3000;
let ssr = true;

if (process.env.DISABLE_SSR && process.env.DISABLE_SSR !== 'false') {
  ssr = false;
}

class WaitOnWebpackPlugin {
  constructor(waitOnUrl) {
    this.waitOnUrl = waitOnUrl;
    this.done = false;
  }

  apply(compiler) {
    compiler.hooks.afterEmit.tapAsync('WaitOnPlugin', (compilation, callback) => {
      if (!this.done) {
        console.log(`Waiting for backend at ${this.waitOnUrl}`);
        waitOn({ resources: [this.waitOnUrl] }, () => {
          console.log(`Backend at ${this.waitOnUrl} has started`);
          this.done = true;
          callback();
        });
      } else {
        callback();
      }
    });
  }
}

module.exports = {
  entry: {
    index: ['raf/polyfill', '@babel/polyfill', './src/index.ts']
  },
  name: 'web',
  module: {
    rules: [
      { test: /\.mjs$/, include: /node_modules/, type: 'javascript/auto' },
      {
        test: /\.(png|ico|jpg|gif|xml)$/,
        use: { loader: 'url-loader', options: { name: '[hash].[ext]', limit: 100000 } }
      },
      {
        test: /\.woff(2)?(\?v=[0-9]\.[0-9]\.[0-9])?$/,
        use: { loader: 'url-loader', options: { name: '[hash].[ext]', limit: 100000 } }
      },
      {
        test: /\.(otf|ttf|eot|svg)(\?v=[0-9]\.[0-9]\.[0-9])?$/,
        use: { loader: 'file-loader', options: { name: '[hash].[ext]' } }
      },
      {
        test: /\.css$/,
        use: [
          process.env.NODE_ENV === 'production'
            ? { loader: MiniCSSExtractPlugin.loader }
            : { loader: 'style-loader', options: {} },
          { loader: 'css-loader', options: { sourceMap: true, importLoaders: 1 } },
          { loader: 'postcss-loader', options: { sourceMap: true } }
        ]
      },
      {
        test: /\.scss$/,
        use: [
          process.env.NODE_ENV === 'production'
            ? { loader: MiniCSSExtractPlugin.loader }
            : { loader: 'style-loader', options: {} },
          { loader: 'css-loader', options: { sourceMap: true, importLoaders: 1 } },
          { loader: 'postcss-loader', options: { sourceMap: true } },
          { loader: 'sass-loader', options: { sourceMap: true } }
        ]
      },
      {
        test: /\.less$/,
        use: [
          process.env.NODE_ENV === 'production'
            ? { loader: MiniCSSExtractPlugin.loader }
            : { loader: 'style-loader', options: {} },
          { loader: 'css-loader', options: { sourceMap: true, importLoaders: 1 } },
          { loader: 'postcss-loader', options: { sourceMap: true } },
          { loader: 'less-loader', options: { javascriptEnabled: true, sourceMap: true } }
        ]
      },
      { test: /\.graphqls/, use: { loader: 'raw-loader', options: {} } },
      { test: /\.(graphql|gql)$/, use: [{ loader: 'graphql-tag/loader', options: {} }] },
      {
        test: /\.tsx?$/,
        use: [
          { loader: 'cache-loader', options: { cacheDirectory: '../../.cache/cache-loader' } },
          { loader: 'thread-loader', options: { workers: 7 } },
          { loader: 'ts-loader', options: { transpileOnly: true, happyPackMode: true, experimentalWatchApi: true } }
        ]
      },
      {
        test: /\.jsx?$/,
        exclude: /node_modules\/(?!@gqlapp)/,
        use: {
          loader: 'heroku-babel-loader',
          options: { babelrc: true, cacheDirectory: '../../.cache/babel-loader', rootMode: 'upward-optional' }
        }
      },
      { test: /locales/, use: { loader: '@alienfast/i18next-loader', options: {} } }
    ],
    unsafeCache: false
  },
  resolve: {
    symlinks: false,
    cacheWithContext: false,
    unsafeCache: false,
    extensions: [
      '.web.mjs',
      '.web.js',
      '.web.jsx',
      '.web.ts',
      '.web.tsx',
      '.mjs',
      '.js',
      '.jsx',
      '.ts',
      '.tsx',
      '.json'
    ]
  },
  watchOptions: { ignored: /build/ },
  bail: false,
  output: {
    pathinfo: false,
    filename: '[name].[hash].js',
    chunkFilename: '[name].[chunkhash].js',
    path: path.join(__dirname, 'build'),
    publicPath: '/'
  },
  devtool: process.env.NODE_ENV === 'production' ? '#nosources-source-map' : '#cheap-module-source-map',
  mode: process.env.NODE_ENV || 'development',
  performance: { hints: false },
  plugins: (process.env.NODE_ENV !== 'production'
    ? [new webpack.HotModuleReplacementPlugin(), new WaitOnWebpackPlugin('tcp:localhost:8080')]
    : [
        new MiniCSSExtractPlugin({
          chunkFilename: '[name].[id].[chunkhash].css',
          filename: `[name].[chunkhash].css`
        })
      ]
  ).concat([
    new CleanWebpackPlugin('build'),
    new webpack.DefinePlugin({
      __CLIENT__: true,
      __SERVER__: false,
      __SSR__: ssr,
      __DEV__: true,
      __TEST__: false,
      'process.env.NODE_ENV': `"${process.env.NODE_ENV || 'development'}"`,
      __API_URL__: '"/graphql"',
      'process.env.STRIPE_PUBLIC_KEY': process.env.STRIPE_PUBLIC_KEY ? `"${process.env.STRIPE_PUBLIC_KEY}"` : undefined
    }),
    new ManifestPlugin({ fileName: 'assets.json' }),
    new ForkTsCheckerWebpackPlugin({
      tsconfig: path.resolve(__dirname, 'tsconfig.json'),
      checkSyntacticErrors: true
    })
  ]),
  node: { __dirname: true, __filename: true, fs: 'empty', net: 'empty', tls: 'empty' },
  devServer: {
    hot: true,
    publicPath: '/',
    headers: { 'Access-Control-Allow-Origin': '*' },
    open: true,
    quiet: false,
    noInfo: true,
    historyApiFallback: true,
    port: webpackPort,
    writeToDisk: pathname => pathname.endsWith('assets.json'),
    proxy: {
      '!(/sockjs-node/**/*|/*.hot-update.{json,js})': { target: 'http://localhost:8080', logLevel: 'info', ws: true }
    },
    disableHostCheck: true
  }
};
