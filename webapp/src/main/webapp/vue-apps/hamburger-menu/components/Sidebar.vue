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
    <v-hover v-model="$root.hoverButton">
      <sidebar-button
        :unread-per-space="unreadPerSpace"
        @open-drawer="openFirstLevel" />
    </v-hover>
    <div>
      <template v-if="$root.displaySequentially">
        <sidebar-third-level
          v-if="allowDisplayLevels"
          v-model="thirdLevelDrawer"
          :opened-space="space"
          :home-link="homeLink"
          :drawer-width="drawerWidth" />
        <sidebar-second-level
          v-if="allowDisplayLevels"
          v-model="secondLevelDrawer"
          :second-level="secondLevel"
          :third-level-drawer="thirdLevelDrawer"
          :opened-space="space"
          :home-link="homeLink"
          :drawer-width="drawerWidth"
          :site="site" />
        <sidebar-first-level
          :first-level-drawer="firstLevelDrawer"
          :second-level-drawer="secondLevelDrawer"
          :third-level-drawer="thirdLevelDrawer"
          :second-level="secondLevel"
          :opened-site="site"
          :spaces="spaces"
          :opened-space="space"
          :drawer-width="drawerWidth"
          @firstLevelDrawer="updateFirstLevelDrawer($event)" />
      </template>
      <template v-else>
        <sidebar-first-level
          :first-level-drawer="firstLevelDrawer"
          :second-level-drawer="secondLevelDrawer"
          :third-level-drawer="thirdLevelDrawer"
          :second-level="secondLevel"
          :opened-site="site"
          :spaces="spaces"
          :opened-space="space"
          :drawer-width="drawerWidth"
          @firstLevelDrawer="updateFirstLevelDrawer($event)" />
        <sidebar-second-level
          v-if="allowDisplayLevels"
          v-model="secondLevelDrawer"
          :second-level="secondLevel"
          :third-level-drawer="thirdLevelDrawer"
          :opened-space="space"
          :home-link="homeLink"
          :drawer-width="drawerWidth"
          :site="site" />
        <sidebar-third-level
          v-if="allowDisplayLevels"
          v-model="thirdLevelDrawer"
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
    drawerWidth: 310,
    space: null,
    site: null,
    initStep: 0,
    spaces: null,
    limit: 7,
    offset: 0,
    unreadPerSpace: null,
    interval: null,
    mouseEvent: false,
    allowClosing: true,
    closeTimeout: null,
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
    stickyDisplay() {
      return this.$root.sticky;
    },
    iconDisplay() {
      return this.$root.icon;
    },
    hiddenDisplay() {
      return this.$root.hidden;
    },
    hover() {
      return this.$root.hover;
    },
  },
  watch: {
    showOverlay() {
      if (this.showOverlay) {
        document.dispatchEvent(new CustomEvent('drawerOpened'));
      } else {
        document.dispatchEvent(new CustomEvent('drawerClosed'));
      }
    },
    iconDisplay: {
      immediate: true,
      handler() {
        if (this.$root.icon) {
          this.firstLevelDrawer = true;
        }
      },
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
      } else if (this.firstLevelDrawer && this.$root.displaySequentially && this.mouseEvent) {
        // Close if mouse is not entered to menu
        this.closeTimeout = window.setTimeout(() => {
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
      } else if (!this.interval && this.$root.displaySequentially) {
        this.interval = window.setTimeout(() => this.closeMenu(), 500);
      }
    },
    icon() {
      if (this.icon) {
        this.firstLevelDrawer = false;
      }
    },
    site() {
      this.$root.openedSiteName = this.site?.name;
    },
    space() {
      this.$root.openedSpaceId = this.space?.id;
    },
  },
  created() {
    this.$root.$on('change-space-menu', this.changeSpaceMenu);
    this.$root.$on('change-spaces-menu', this.changeSpacesMenu);
    this.$root.$on('change-site-menu', this.changeSiteMenu);
    this.$root.$on('dialog-opened', () => this.allowClosing = false);
    this.$root.$on('dialog-closed', () => window.setTimeout(() => {
      this.allowClosing = true;
    }, 200));
    this.$root.$on('menu-opened', () => this.allowClosing = false);
    this.$root.$on('menu-closed', () => this.allowClosing = true);
    document.addEventListener('closeDisplayedDrawer', this.closeDisplayedDrawer);
  },
  methods: {
    async openFirstLevel(mouseEvent) {
      this.mouseEvent = mouseEvent;
      this.firstLevelDrawer = false;
      await this.$nextTick();

      window.clearTimeout(this.closeTimeout);
      window.clearInterval(this.interval);
      this.firstLevelDrawer = true;
      if (this.allowClosing) {
        this.allowClosing = false;
        this.closeTimeout = window.setTimeout(() => this.allowClosing = true, 2000);
      }
    },
    async changeSpacesMenu(spaceTemplateId, spacesUrl, sortBy, name) {
      this.site = null;
      if (this.secondLevel === 'spaces'
          && ((spaceTemplateId && this.$root.openedSpaceTemplateId === spaceTemplateId)
              || (spacesUrl && this.$root.openedSpacesUrl === spacesUrl))) {
        this.space = null;
        this.secondLevel = null;
        this.secondLevelDrawer = false;
        this.thirdLevelDrawer = false;
        window.setTimeout(() => {
          this.$root.openedSpaceTemplateId = null;
          this.$root.openedSpaceTemplateName = null;
          this.$root.openedSpacesUrl = null;
          this.$root.spacesSortBy = null;
        }, 50);
      } else {
        if (this.secondLevel) {
          this.secondLevel = null;
          await this.$nextTick();
        }
        this.space = null;
        this.$root.openedSpaceTemplateId = spaceTemplateId;
        this.$root.openedSpaceTemplateName = name;
        this.$root.openedSpacesUrl = spacesUrl;
        this.$root.spacesSortBy = sortBy;
        this.secondLevel = 'spaces';
        this.secondLevelDrawer = true;
        this.thirdLevelDrawer = false;
      }
    },
    changeSpaceMenu(space, thirdLevel) {
      this.site = null;
      if (!thirdLevel && this.secondLevel === 'spaces') {
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
        if (this.secondLevel === 'spaces') {
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
        this.thirdLevelDrawer = false;
        this.site= null;
      } else {
        this.site = site;
        this.secondLevel = 'site';
        this.secondLevelDrawer = true;
        this.thirdLevelDrawer = false;
      }
    },
    closeDisplayedDrawer() {
      if (this.firstLevelDrawer || this.secondLevelDrawer) {
        this.closeMenu();
      }
    },
    updateFirstLevelDrawer(drawer) {
      if (this.$root.icon && this.$root.stickyAllowed) {
        this.firstLevelDrawer = true;
      } else {
        this.firstLevelDrawer = drawer;
      }
    },
    closeMenu() {
      if (!this.allowClosing) {
        this.interval = window.setTimeout(() => this.closeMenu(), 500);
        return;
      }
      if (!this.hover) {
        this.updateFirstLevelDrawer(false);
        this.secondLevelDrawer = false;
        this.thirdLevelDrawer = false;
        this.space = null;
        this.site = null;
        this.secondLevel = null;
      }
    },
  },
};
</script>
