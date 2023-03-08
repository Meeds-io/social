<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

-->
<template>
  <v-app
    color="transaprent"
    class="HamburgerNavigationMenu"
    flat>
    <hamburger-menu-navigation-button 
      v-if="!stickyDisplay"
      :unread-per-space="unreadPerSpace"
      @open-drawer="firstLevelDrawer = true" />
    <template v-if="displaySequentially">
      <hamburger-menu-navigation-third-level
        v-if="allowDisplayLevels"
        v-model="thirdLevelDrawer"
        :display-sequentially="displaySequentially"
        :opened-space="space"
        :home-link="homeLink"
        :drawer-width="drawerWidth" />
      <hamburger-menu-navigation-second-level
        v-if="allowDisplayLevels"
        v-model="secondLevelDrawer"
        :display-sequentially="displaySequentially"
        :second-level="secondLevel"
        :third-level-drawer="thirdLevelDrawer"
        :opened-space="space"
        :home-link="homeLink"
        :drawer-width="drawerWidth"
        :has-administration-navigations="hasAdministrationNavigations"
        :administration-navigations="administrationNavigations" />
      <hamburger-menu-navigation-first-level
        :sticky-preference="stickyPreference"
        :first-level-drawer="firstLevelDrawer"
        :second-level-drawer="secondLevelDrawer"
        :third-level-drawer="thirdLevelDrawer"
        :second-level="secondLevel"
        :has-administration-navigations="hasAdministrationNavigations"
        :site-navigations="siteNavigations"
        :recent-spaces="recentSpaces"
        :opened-space="space"
        :sticky-allowed="stickyAllowed"
        :drawer-width="drawerWidth"
        @stickyPreference="stickyPreference = $event"
        @firstLevelDrawer="firstLevelDrawer = $event" />
    </template>
    <template v-else>
      <hamburger-menu-navigation-first-level
        :sticky-preference="stickyPreference"
        :first-level-drawer="firstLevelDrawer"
        :second-level-drawer="secondLevelDrawer"
        :third-level-drawer="thirdLevelDrawer"
        :second-level="secondLevel"
        :has-administration-navigations="hasAdministrationNavigations"
        :site-navigations="siteNavigations"
        :recent-spaces="recentSpaces"
        :opened-space="space"
        :sticky-allowed="stickyAllowed"
        :drawer-width="drawerWidth"
        @stickyPreference="stickyPreference = $event"
        @firstLevelDrawer="firstLevelDrawer = $event" />
      <hamburger-menu-navigation-second-level
        v-if="allowDisplayLevels"
        v-model="secondLevelDrawer"
        :display-sequentially="displaySequentially"
        :second-level="secondLevel"
        :third-level-drawer="thirdLevelDrawer"
        :opened-space="space"
        :home-link="homeLink"
        :drawer-width="drawerWidth"
        :has-administration-navigations="hasAdministrationNavigations"
        :administration-navigations="administrationNavigations" />
      <hamburger-menu-navigation-third-level
        v-if="allowDisplayLevels"
        v-model="thirdLevelDrawer"
        :display-sequentially="displaySequentially"
        :opened-space="space"
        :home-link="homeLink"
        :drawer-width="drawerWidth" />
    </template>
  </v-app>
</template>
<script>
export default {
  data: () => ({
    firstLevelDrawer: false,
    secondLevelDrawer: false,
    thirdLevelDrawer: false,
    secondLevel: null,
    stickyPreference: false,
    drawerWidth: 310,
    space: null,
    administrationNavigations: null,
    siteNavigations: null,
    initStep: 0,
    recentSpaces: null,
    limit: 7,
    offset: 0,
    unreadPerSpace: null,
    //unreadPerSpaceArray: []
  }),
  computed: {
    allowDisplayLevels() {
      return this.stickyDisplay || this.firstLevelDrawer;
    },
    levelsOpened() {
      return this.secondLevelDrawer || this.thirdLevelDrawer;
    },
    showOverlay() {
      return this.stickyDisplay && this.levelsOpened;
    },
    hasAdministrationNavigations() {
      return this.administrationNavigations?.length;
    },
    displaySequentially() {
      return this.$vuetify.breakpoint.width >= this.$vuetify.breakpoint.thresholds.lg;
    },
    stickyAllowed() {
      return this.$vuetify.breakpoint.width >= this.$vuetify.breakpoint.thresholds.lg;
    },
    stickyDisplay() {
      return this.stickyPreference && this.stickyAllowed;
    },
    initialized() {
      return this.initStep === 2;
    },
  },
  watch: {
    initialized() {
      if (this.initialized) {
        this.$root.$applicationLoaded();
      }
    },
    showOverlay() {
      if (this.showOverlay) {
        document.dispatchEvent(new CustomEvent('drawerOpened'));
      } else {
        document.dispatchEvent(new CustomEvent('drawerClosed'));
      }
    },
    secondLevelDrawer() {
      if (!this.secondLevelDrawer) {
        this.thirdLevelDrawer = false;
        this.space = null;
        this.secondLevel = null;
      }
    },
    firstLevelDrawer() {
      if (!this.firstLevelDrawer && !this.stickyDisplay) {
        this.thirdLevelDrawer = false;
        this.secondLevelDrawer = false;
        this.space = null;
        this.secondLevel = null;
      }
    },
    stickyDisplay() {
      this.closeMenu();
      if (this.stickyDisplay) {
        document.body.className = `${document.body.className.replace('HamburgerMenuSticky', '')  } HamburgerMenuSticky`;
      } else {
        document.body.className = document.body.className.replace('HamburgerMenuSticky', '');
      }
    },
  },
  created() {
    this.stickyPreference = eXo.env.portal.stickyMenu;
    this.$root.$on('change-space-menu', this.changeSpaceMenu);
    this.$root.$on('change-recent-spaces-menu', this.changeRecentSpacesMenu);
    this.$root.$on('change-administration-menu', this.changeAdministrationMenu);
    document.addEventListener('closeDisplayedDrawer', this.closeDisplayedDrawer);
    this.init();
  },
  mounted() {
    this.initStep++;
  },
  methods: {
    init() {
      return Promise.all([
        this.retrieveSiteNavigations(),
        this.retrieveAdministrationNavigations(),
        this.retrieveRecentSpaces(),
      ]).finally(() => this.initStep++);
    },
    changeRecentSpacesMenu() {
      if (this.secondLevel === 'recentSpaces') {
        this.space = null;
        this.secondLevel = null;
        this.secondLevelDrawer = false;
        this.thirdLevelDrawer = false;
      } else {
        this.space = null;
        this.secondLevel = 'recentSpaces';
        this.secondLevelDrawer = true;
        this.thirdLevelDrawer = false;
      }
    },
    changeSpaceMenu(space, thirdLevel) {
      if (!thirdLevel && this.secondLevel === 'recentSpaces') {
        this.space = space;
        this.secondLevel = 'spaceMenu';
        this.secondLevelDrawer = true;
        this.thirdLevelDrawer = false;
      } else if (this.space?.id === space?.id) {
        this.space = null;
        if (this.thirdLevelDrawer) {
          this.thirdLevelDrawer = false;
        } else {
          this.secondLevelDrawer = false;
        }
      } else {
        this.space = space;
        if (this.secondLevel === 'recentSpaces') {
          this.thirdLevelDrawer = true;
        } else {
          this.secondLevel = 'spaceMenu';
          this.secondLevelDrawer = true;
          this.thirdLevelDrawer = false;
        }
      }
    },
    changeAdministrationMenu() {
      this.thirdLevelDrawer = false;
      this.space = null;

      if (this.secondLevel === 'administration') {
        this.secondLevel = null;
        this.secondLevelDrawer = false;
      } else {
        this.secondLevel = 'administration';
        this.secondLevelDrawer = true;
      }
    },
    closeDisplayedDrawer() {
      if (this.firstLevelDrawer || this.secondLevelDrawer) {
        this.closeMenu();
      }
    },
    closeMenu() {
      this.firstLevelDrawer = false;
      this.secondLevelDrawer = false;
      this.thirdLevelDrawer = false;
      this.space = null;
      this.secondLevel = null;
      window.setTimeout(() => document.dispatchEvent(new CustomEvent('drawerClosed')), 200);
    },
    retrieveSiteNavigations() {
      return this.$navigationService.getNavigations(eXo.env.portal.portalName, 'portal', 'children', 'displayed')
        .then(data => this.siteNavigations = data || []);
    },
    retrieveAdministrationNavigations() {
      return this.$navigationService.getNavigations(null, 'group', null, 'displayed')
        .then(data => this.administrationNavigations = data || []);
    },
    retrieveRecentSpaces() {
      return this.$spaceService.getSpaces('', this.offset, this.limit, 'lastVisited', 'member,managers,favorite,unread')
        .then(data => {
          this.recentSpaces = data && data.spaces || [];
          this.unreadPerSpace = data && data.unreadPerSpace;
          return this.$nextTick();
        });
    },
  },
};
</script>
