const path = require('path');
const ESLintPlugin = require('eslint-webpack-plugin');
const { VueLoaderPlugin } = require('vue-loader')

let config = {
  context: path.resolve(__dirname, '.'),
  // set the entry point of the application
  // can use multiple entry
  plugins: [
    new ESLintPlugin({
      files: [
        './src/main/webapp/vue-apps/*.js',
        './src/main/webapp/vue-apps/*.vue',
        './src/main/webapp/vue-apps/**/*.js',
        './src/main/webapp/vue-apps/**/*.vue',
      ],
    }),
    new VueLoaderPlugin()
  ],
  entry: {
    commonVueComponents: './src/main/webapp/vue-apps/common/main.js',
    login: './src/main/webapp/vue-apps/login/main.js',
    register: './src/main/webapp/vue-apps/user-register/main.js',
    registerOnboarding: './src/main/webapp/vue-apps/user-register-onboarding/main.js',
    oAuthLoginExtension: './src/main/webapp/vue-apps/login-oauth/main.js',
    spacesAdministration: './src/main/webapp/vue-apps/spaces-administration/main.js',
    hamburgerMenu: './src/main/webapp/vue-apps/hamburger-menu/main.js',
    administrationHamburgerMenu: './src/main/webapp/vue-apps/administration-navigation/main.js',
    userHamburgerMenu: './src/main/webapp/vue-apps/user-navigation/main.js',
    siteHamburgerMenu: './src/main/webapp/vue-apps/site-navigation/main.js',
    spacesHamburgerMenu: './src/main/webapp/vue-apps/spaces-navigation/main.js',
    profileHamburgerMenu: './src/main/webapp/vue-apps/profile-navigation/main.js',
    topBarNotification: './src/main/webapp/vue-apps/notification-top-bar/main.js',
    gettingStarted: './src/main/webapp/vue-apps/getting-started/main.js',
    externalSpacesList: './src/main/webapp/vue-apps/external-spaces-list/main.js',
    spacesListComponents: './src/main/webapp/vue-apps/spaces-list/initComponents.js',
    spacesList: './src/main/webapp/vue-apps/spaces-list/main.js',
    spacesOverview: './src/main/webapp/vue-apps/spaces-overview/main.js',
    suggestions: './src/main/webapp/vue-apps/suggestions-people-space/main.js',
    peopleListComponents: './src/main/webapp/vue-apps/people-list/initComponents.js',
    peopleList: './src/main/webapp/vue-apps/people-list/main.js',
    peopleOverview: './src/main/webapp/vue-apps/people-overview/main.js',
    profileHeader: './src/main/webapp/vue-apps/profile-header/main.js',
    profileAboutMe: './src/main/webapp/vue-apps/profile-about-me/main.js',
    profileContactInformation: './src/main/webapp/vue-apps/profile-contact-information/main.js',
    profileWorkExperience: './src/main/webapp/vue-apps/profile-work-experience/main.js',
    userSettingLanguage: './src/main/webapp/vue-apps/user-setting-language/main.js',
    userSettingNotifications: './src/main/webapp/vue-apps/user-setting-notifications/main.js',
    userSettingSecurity: './src/main/webapp/vue-apps/user-setting-security/main.js',
    userSettingTimezone: './src/main/webapp/vue-apps/user-setting-timezone/main.js',
    spaceInfos: './src/main/webapp/vue-apps/space-infos-app/main.js',
    whoIsOnline: './src/main/webapp/vue-apps/who-is-online-app/main.js',
    activityComposer: './src/main/webapp/vue-apps/activity-composer-app/main.js',
    spaceMenu: './src/main/webapp/vue-apps/space-menu/main.js',
    spaceHeader: './src/main/webapp/vue-apps/space-header/main.js',
    spaceMembers: './src/main/webapp/vue-apps/space-members/main.js',
    spaceSettings: './src/main/webapp/vue-apps/space-settings/main.js',
    idmUsersManagement: './src/main/webapp/vue-apps/idm-users-management/main.js',
    idmGroupsManagement: './src/main/webapp/vue-apps/idm-groups-management/main.js',
    idmMembershipTypesManagement: './src/main/webapp/vue-apps/idm-membership-types-management/main.js',
    searchApplication: './src/main/webapp/vue-apps/search/main.js',
    peopleSearchResultCard: './src/main/webapp/vue-apps/search-people/main.js',
    spaceSearchResultCard: './src/main/webapp/vue-apps/search-space/main.js',
    activitySearchResultCard: './src/main/webapp/vue-apps/search-activity/main.js',
    activityReactions: './src/main/webapp/vue-apps/activity-reactions/main.js',
    activityStream: './src/main/webapp/vue-apps/activity-stream/main.js',
    branding: './src/main/webapp/vue-apps/company-branding-app/main.js',
    spaceBannerLogoPopover: './src/main/webapp/vue-apps/space-top-bannerlogo/main.js',
    topBarFavorites: './src/main/webapp/vue-apps/favorites-list-top-bar/main.js',
    popover: './src/main/webapp/vue-apps/popover/main.js',
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: [
          'babel-loader',
        ]
      },
      {
        test: /\.vue$/,
        use: [
          'vue-loader',
        ]
      }
    ]
  }
};

module.exports = config;
