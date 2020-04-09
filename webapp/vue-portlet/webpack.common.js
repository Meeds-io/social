const path = require('path');

let config = {
  context: path.resolve(__dirname, '.'),
  // set the entry point of the application
  // can use multiple entry
  entry: {
    commonVueComponents: './src/main/webapp/common/main.js',
    spacesAdministration: './src/main/webapp/spaces-administration-app/main.js',
    spaceTemplates: './src/main/webapp/space-templates-app/main.js',
    spacesHamburgerMenu: './src/main/webapp/spaces-navigation/main.js',
    profileHamburgerMenu: './src/main/webapp/profile-navigation/main.js',
    topBarLogo: './src/main/webapp/logo-top-bar/main.js',
    topBarNotification: './src/main/webapp/notification-top-bar/main.js',
    gettingStarted: './src/main/webapp/getting-started/main.js',
    spacesList: './src/main/webapp/spaces-list/main.js',
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: [
          'babel-loader',
          'eslint-loader',
        ]
      },
      {
        test: /\.vue$/,
        use: [
          'vue-loader',
          'eslint-loader',
        ]
      }
    ]
  },
  externals: {
    vue: 'Vue',
    vuetify: 'Vuetify',
  },
};

module.exports = config;
