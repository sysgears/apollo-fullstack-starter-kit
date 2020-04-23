const exportedAssets = {};

if (!__TEST__) {
  // Favicon.ico should not be hashed, since some browsers expect it to be exactly on /favicon.ico URL
  require('!file-loader?name=[name].[ext]!./assets/favicon.ico'); // eslint-disable-line

  // android-chrome paths are required to be exact as they are required in manifest file
  require('!file-loader?name=[name].[ext]!./assets/android-chrome-192x192.png'); // eslint-disable-line
  require('!file-loader?name=[name].[ext]!./assets/android-chrome-256x256.png'); // eslint-disable-line

  // Require all files from assets dir recursively addding them into assets.json
  let req = require.context('!file-loader?name=[hash].[ext]!./assets', true, /.*/);
  req.keys().map(key => {
    exportedAssets[`${key.replace('./', '')}`] = req(key);
  });
}

export default exportedAssets;
