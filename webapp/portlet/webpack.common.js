/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
const path = require('path');

let config = {
  context: path.resolve(__dirname, '.'),
  // set the entry point of the application
  // can use multiple entry
  entry: {
    commonVueComponents: './src/main/webapp/common/main.js',
    spacesAdministration: './src/main/webapp/spaces-administration/main.js',
    siteHamburgerMenu: './src/main/webapp/site-navigation/main.js',
    spacesHamburgerMenu: './src/main/webapp/spaces-navigation/main.js',
    profileHamburgerMenu: './src/main/webapp/profile-navigation/main.js',
    topBarNotification: './src/main/webapp/notification-top-bar/main.js',
    gettingStarted: './src/main/webapp/getting-started/main.js',
    spacesListComponents: './src/main/webapp/spaces-list/initComponents.js',
    spacesList: './src/main/webapp/spaces-list/main.js',
    spacesOverview: './src/main/webapp/spaces-overview/main.js',
    suggestions: './src/main/webapp/suggestions-people-space/main.js',
    peopleListComponents: './src/main/webapp/people-list/initComponents.js',
    peopleList: './src/main/webapp/people-list/main.js',
    peopleOverview: './src/main/webapp/people-overview/main.js',
    profileHeader: './src/main/webapp/profile-header/main.js',
    profileAboutMe: './src/main/webapp/profile-about-me/main.js',
    profileContactInformation: './src/main/webapp/profile-contact-information/main.js',
    profileWorkExperience: './src/main/webapp/profile-work-experience/main.js',
    userSettingLanguage: './src/main/webapp/user-setting-language/main.js',
    userSettingNotifications: './src/main/webapp/user-setting-notifications/main.js',
    userSettingSecurity: './src/main/webapp/user-setting-security/main.js',
    userSettingTimezone: './src/main/webapp/user-setting-timezone/main.js',
    spaceInfos: './src/main/webapp/space-infos-app/main.js',
    whoIsOnline: './src/main/webapp/who-is-online-app/main.js',
    activityComposer: './src/main/webapp/activity-composer-app/main.js',
    spaceMenu: './src/main/webapp/space-menu/main.js',
    spaceHeader: './src/main/webapp/space-header/main.js',
    spaceMembers: './src/main/webapp/space-members/main.js',
    spaceSettings: './src/main/webapp/space-settings/main.js',
    idmUsersManagement: './src/main/webapp/idm-users-management/main.js',
    idmGroupsManagement: './src/main/webapp/idm-groups-management/main.js',
    idmMembershipTypesManagement: './src/main/webapp/idm-membership-types-management/main.js',
    searchApplication: './src/main/webapp/search/main.js',
    peopleSearchResultCard: './src/main/webapp/search-people/main.js',
    spaceSearchResultCard: './src/main/webapp/search-space/main.js',
    activitySearchResultCard: './src/main/webapp/search-activity/main.js',
    activityReactions: './src/main/webapp/activity-reactions/main.js'
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