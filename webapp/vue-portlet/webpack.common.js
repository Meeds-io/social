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
    topBarNotification: './src/main/webapp/notification-top-bar/main.js',
    gettingStarted: './src/main/webapp/getting-started/main.js',
    spacesList: './src/main/webapp/spaces-list/main.js',
    spacesOverview: './src/main/webapp/spaces-overview/main.js',
    suggestions: './src/main/webapp/suggestions-people-space/main.js',
    peopleList: './src/main/webapp/people-list/main.js',
    peopleOverview: './src/main/webapp/people-overview/main.js',
    profileHeader: './src/main/webapp/profile-header/main.js',
    profileAboutMe: './src/main/webapp/profile-about-me/main.js',
    profileContactInformation: './src/main/webapp/profile-contact-information/main.js',
    profileWorkExperience: './src/main/webapp/profile-work-experience/main.js'
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
