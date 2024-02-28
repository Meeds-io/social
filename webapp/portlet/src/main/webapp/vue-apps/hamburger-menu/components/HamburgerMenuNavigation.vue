<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

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
      @open-drawer="openFirstLevel" />
    <div
      @mouseenter="hover = true"
      @mouseleave="hover = false">
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
          :site="site" />
        <hamburger-menu-navigation-first-level
          :sticky-preference="stickyPreference"
          :first-level-drawer="firstLevelDrawer"
          :second-level-drawer="secondLevelDrawer"
          :third-level-drawer="thirdLevelDrawer"
          :second-level="secondLevel"
          :sites="sites"
          :opened-site="site"
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
          :sites="sites"
          :opened-site="site"
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
          :site="site" />
        <hamburger-menu-navigation-third-level
          v-if="allowDisplayLevels"
          v-model="thirdLevelDrawer"
          :display-sequentially="displaySequentially"
          :opened-space="space"
          :home-link="homeLink"
          :drawer-width="drawerWidth" />
      </template>
    </div>
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
    site: null,
    sites: [],
    initStep: 0,
    recentSpaces: null,
    limit: 7,
    offset: 0,
    unreadPerSpace: null,
    hover: false,
    interval: null,
    mouseEvent: false,
    allowClosing: true,
    visibility: ['displayed', 'temporal']
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
        this.site = null;
        this.secondLevel = null;
      }
    },
    firstLevelDrawer() {
      if (!this.firstLevelDrawer && !this.stickyDisplay) {
        this.thirdLevelDrawer = false;
        this.secondLevelDrawer = false;
        this.space = null;
        this.site = null;
        this.secondLevel = null;
      } else if (this.firstLevelDrawer && this.mouseEvent) {
        // Close if mouse is not entered to menu
        window.setTimeout(() => {
          if (!this.hover) {
            this.closeMenu();
          }
        }, 500);
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
    hover() {
      if (this.hover) {
        if (this.interval) {
          window.clearInterval(this.interval);
          this.interval = null;
        }
      } else if (!this.interval) {
        this.interval = window.setTimeout(() => this.closeMenu(), 500);
      }
    },
    stickyPreference() {
      if (this.initialized) {
        document.dispatchEvent(new CustomEvent('left-menu-stickiness', {detail: this.stickyPreference}));
      }
    },
  },
  created() {
    this.stickyPreference = eXo.env.portal.stickyMenu;
    this.$root.$on('change-space-menu', this.changeSpaceMenu);
    this.$root.$on('change-recent-spaces-menu', this.changeRecentSpacesMenu);
    this.$root.$on('change-site-menu', this.changeSiteMenu);
    this.$root.$on('dialog-opened', () => this.allowClosing = false);
    this.$root.$on('dialog-closed', () => this.allowClosing = true);
    document.addEventListener('closeDisplayedDrawer', this.closeDisplayedDrawer);
    this.init();
  },
  mounted() {
    this.initStep++;
  },
  methods: {
    init() {
      return Promise.all([
        this.retrieveSites(),
        this.retrieveRecentSpaces(),
      ]).finally(() => this.initStep++);
    },
    openFirstLevel(mouseEvent) {
      this.mouseEvent = mouseEvent;
      this.firstLevelDrawer = true;
    },
    changeRecentSpacesMenu() {
      this.site = null;
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
      this.site = null;
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
    changeSiteMenu(site) {
      this.space = null;
      if (this.site?.name === site.name) {
        this.secondLevel = null;
        this.secondLevelDrawer = false;
        this.site= null;
      } else {
        this.site = site;
        this.secondLevel = 'site';
        this.secondLevelDrawer = true;
      }
    },
    closeDisplayedDrawer() {
      if (this.firstLevelDrawer || this.secondLevelDrawer) {
        this.closeMenu();
      }
    },
    closeMenu() {
      if (!this.allowClosing) {
        this.interval = window.setTimeout(() => this.closeMenu(), 500);
        return;
      }
      this.firstLevelDrawer = false;
      this.secondLevelDrawer = false;
      this.thirdLevelDrawer = false;
      this.space = null;
      this.site = null;
      this.secondLevel = null;
      window.setTimeout(() => document.dispatchEvent(new CustomEvent('drawerClosed')), 200);
    },
    retrieveSites(){
      return this.$siteService.getSites('PORTAL', null, 'global', true, true, true, true, true, true, true, true, true, ['displayed', 'temporal'])
        .then(data => this.sites = data || []);
    },
    retrieveRecentSpaces() {
      return this.$spaceService.getSpaces('', this.offset, this.limit, 'lastVisited', 'member,managers,favorite,unread,muted')
        .then(data => {
          this.recentSpaces = data && data.spaces || [];
          this.unreadPerSpace = data && data.unreadPerSpace;
          return this.$nextTick();
        });
    },
  },
};
</script>
