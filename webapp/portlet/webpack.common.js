const path = require('path');

let config = {
  context: path.resolve(__dirname, '.'),
  // set the entry point of the application
  // can use multiple entry
  entry: {
    spaceInfos: './src/main/webapp/space-infos-app/main.js',
    whoIsOnline: './src/main/webapp/who-is-online-app/main.js',
    activityComposer: './src/main/webapp/activity-composer-app/main.js',
    spaceMenu: './src/main/webapp/space-menu/main.js',
    spaceHeader: './src/main/webapp/space-header/main.js',
    spaceMembers: './src/main/webapp/space-members/main.js',
    spaceSettings: './src/main/webapp/space-settings/main.js',
    idmUsersManagement: './src/main/webapp/idm-users-management/main.js',
    idmGroupsManagement: './src/main/webapp/idm-groups-management/main.js',
    idmMembershipsManagement: './src/main/webapp/idm-memberships-management/main.js'
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
  }
};

module.exports = config;