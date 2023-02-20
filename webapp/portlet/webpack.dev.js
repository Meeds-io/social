const path = require('path');
const { merge } = require('webpack-merge');
const webpackCommonConfig = require('./webpack.common.js');

// the display name of the war
const app = 'social-portlet';

// add the server path to your server location path
const exoServerPath = "D:\\\eXo\\servers\\platform-6.4.0-RC01";

let config = merge(webpackCommonConfig, {
  output: {
    path: path.resolve(`${exoServerPath}/webapps/${app}/`),
    filename: 'js/[name].bundle.js',
    libraryTarget: 'amd'
  },
  mode: 'development',
  devtool: 'eval-source-map'
});

module.exports = config;
