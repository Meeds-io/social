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
    <div class="layout-side-bar">
      <template v-if="$root.displaySequentially">
        <sidebar-third-level
          v-if="allowDisplayLevels"
          v-model="thirdLevelDrawer"
          :opened-space="space"
          :home-link="homeLink"
          :drawer-width="drawerWidth"
          @keydown.esc.native="closeLevelsDrawer" />
        <sidebar-second-level
          v-if="allowDisplayLevels"
          v-model="secondLevelDrawer"
          :second-level="secondLevel"
          :third-level-drawer="thirdLevelDrawer"
          :opened-space="space"
          :home-link="homeLink"
          :drawer-width="drawerWidth"
          :site="site"
          @keydown.esc.native="closeLevelsDrawer" />
        <sidebar-first-level
          :first-level-drawer="firstLevelDrawer"
          :second-level-drawer="secondLevelDrawer"
          :third-level-drawer="thirdLevelDrawer"
          :second-level="secondLevel"
          :opened-site="site"
          :opened-space="space"
          :drawer-width="drawerWidth"
          @firstLevelDrawer="updateFirstLevelDrawer($event)" />
        <v-overlay
          v-if="showInnerOverlay"
          absolute />
      </template>
      <template v-else>
        <sidebar-first-level
          :first-level-drawer="firstLevelDrawer"
          :second-level-drawer="secondLevelDrawer"
          :third-level-drawer="thirdLevelDrawer"
          :second-level="secondLevel"
          :opened-site="site"
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
          :site="site"
          @keydown.esc.native="closeLevelsDrawer" />
        <sidebar-third-level
          v-if="allowDisplayLevels"
          v-model="thirdLevelDrawer"
          :opened-space="space"
          :home-link="homeLink"
          :drawer-width="drawerWidth"
          @keydown.esc.native="closeLevelsDrawer" />
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
    limit: 7,
    offset: 0,
    unreadPerSpace: null,
    interval: null,
    mouseEvent: false,
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
    mode() {
      return this.$root.mode;
    },
    showInnerOverlay() {
      return this.$root.sticky && (this.secondLevelDrawer || this.thirdLevelDrawer);
    },
  },
  watch: {
    showOverlay() {
      if (this.showOverlay) {
        document.dispatchEvent(new CustomEvent('drawerOpened', {detail: true}));
      } else {
        document.dispatchEvent(new CustomEvent('drawerClosed', {detail: true}));
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
      if (!this.$root.displaySequentially && document.querySelector('.v-overlay--active')) {
        if (this.secondLevelDrawer) {
          document.querySelector('.v-overlay--active').addEventListener('click', this.closeLevelsDrawer);
        } else {
          document.querySelector('.v-overlay--active').removeEventListener('click', this.closeLevelsDrawer);
        }
      }
      if (!this.secondLevelDrawer) {
        this.thirdLevelDrawer = false;
        this.space = null;
        this.site = null;
        this.secondLevel = null;
        // Closing the second level by any means (Esc, outside click, auto-close,
        // navigation) must also clear the "spaces menu" opened-state, otherwise
        // the SPACES / category / template arrows stay in their opened (back-
        // pointing) direction and keep aria-expanded="true" while closed.
        this.resetSpacesMenuState();
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
      } else if (!this.interval && this.$root.displaySequentially && !this.$root.hidden) {
        this.interval = window.setTimeout(() => this.closeMenu(), 500);
      }
    },
    site() {
      this.$root.openedSiteName = this.site?.name;
    },
    space() {
      this.$root.openedSpaceId = this.space?.id;
    },
    mode(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.closeMenuEffectively(true);
      }
    },
  },
  created() {
    this.$root.$on('change-space-menu', this.changeSpaceMenu);
    this.$root.$on('change-spaces-menu', this.changeSpacesMenu);
    this.$root.$on('change-site-menu', this.changeSiteMenu);
    document.addEventListener('closeDisplayedDrawer', this.closeDisplayedDrawer);
    document.addEventListener('drawerOpened', this.closeDisplayedDrawerIfNotSelf);
  },
  beforeDestroy() {
    this.$root.$off('change-space-menu', this.changeSpaceMenu);
    this.$root.$off('change-spaces-menu', this.changeSpacesMenu);
    this.$root.$off('change-site-menu', this.changeSiteMenu);
    document.removeEventListener('closeDisplayedDrawer', this.closeDisplayedDrawer);
    document.removeEventListener('drawerOpened', this.closeDisplayedDrawerIfNotSelf);
  },
  methods: {
    async openFirstLevel(mouseEvent) {
      this.mouseEvent = mouseEvent;
      this.firstLevelDrawer = false;
      await this.$nextTick();

      window.clearTimeout(this.closeTimeout);
      window.clearInterval(this.interval);
      this.firstLevelDrawer = true;
      if (this.$root.allowClosing) {
        this.$root.allowClosing = false;
        this.closeTimeout = window.setTimeout(() => this.$root.allowClosing = true, 2000);
      }
    },
    async changeSpacesMenu(spaceTemplateId, spaceCategoryId, spacesUrl, sortBy, name, type) {
      this.site = null;
      if (this.secondLevel === 'spaces'
          && ((spaceTemplateId && this.$root.openedSpaceTemplateId === spaceTemplateId)
              || (spaceCategoryId && this.$root.openedSpaceCategoryId === spaceCategoryId)
              || (spacesUrl && this.$root.openedSpaces))) {
        this.space = null;
        this.secondLevel = null;
        this.secondLevelDrawer = false;
        this.thirdLevelDrawer = false;
      } else {
        if (this.secondLevel) {
          this.secondLevel = null;
          await this.$nextTick();
        }
        this.space = null;
        this.$root.openedFirstLevelType = type;
        this.$root.openedSpaceTemplateId = spaceTemplateId;
        this.$root.openedSpaceCategoryId = spaceCategoryId;
        this.$root.openedSpaceTemplateName = spaceTemplateId && name || null;
        this.$root.openedSpaceCategoryName = spaceCategoryId && name || null;
        this.$root.openedSpaces = !spaceTemplateId && !spaceCategoryId;
        this.$root.openedSpacesUrl = spacesUrl;
        this.$root.spacesSortBy = sortBy;
        this.secondLevel = 'spaces';
        this.secondLevelDrawer = true;
        this.thirdLevelDrawer = false;
      }
    },
    changeSpaceMenu(space, thirdLevel) {
      this.site = null;
      // Opening a space as the second level replaces the spaces list, so clear
      // its opened-state (otherwise the SPACES / category / template arrow stays
      // ← while a space/site panel is shown). Skipped for third-level changes,
      // where the spaces list stays the second level behind it.
      if (!thirdLevel) {
        this.resetSpacesMenuState();
      }
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
      if (!thirdLevel) {
        this.$root.openedFirstLevelType = 'SPACE';
      }
    },
    changeSiteMenu(site) {
      this.space = null;
      // Opening a site as the second level replaces any spaces list, so clear
      // its opened-state so the spaces arrows don't stay ← behind the site panel.
      this.resetSpacesMenuState();
      if (this.site?.name === site.name) {
        this.secondLevel = null;
        this.secondLevelDrawer = false;
        this.thirdLevelDrawer = false;
        this.site= null;
      } else {
        this.site = site;
        this.secondLevel = 'site';
        this.$root.openedFirstLevelType = 'SITE';
        this.secondLevelDrawer = true;
        this.thirdLevelDrawer = false;
      }
    },
    closeDisplayedDrawerIfNotSelf(event) {
      if (!event?.detail) {
        this.closeMenuEffectively(true);
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
      if (!this.$root.allowClosing) {
        this.interval = window.setTimeout(() => this.closeMenu(), 500);
        return;
      }
      if (!this.hover) {
        this.closeMenuEffectively();
      }
    },
    closeMenuEffectively(force) {
      if (force && this.$root.icon) {
        this.$root.hoverMenu = false;
        this.$root.hoverThirdLevel = false;
        this.$root.hoverSecondLevel = false;
        this.$root.hoverFirstLevel = false;
        this.$root.hoverDeferred = false;
      } else {
        this.updateFirstLevelDrawer(false);
      }
      this.secondLevelDrawer = false;
      this.thirdLevelDrawer = false;
      this.space = null;
      this.site = null;
      this.secondLevel = null;
    },
    resetSpacesMenuState() {
      this.$root.openedFirstLevelType = null;
      this.$root.openedSpaceTemplateId = null;
      this.$root.openedSpaceCategoryId = null;
      this.$root.openedSpaceTemplateName = null;
      this.$root.openedSpaceCategoryName = null;
      this.$root.openedItem = null;
      this.$root.openedSpaces = false;
      this.$root.openedSpacesUrl = null;
      this.$root.spacesSortBy = null;
    },
    closeLevelsDrawer(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      if (this.thirdLevelDrawer) {
        this.thirdLevelDrawer = false;
        this.$nextTick(() => this.$root.lastThirdLevelFocusElement?.focus?.());
      } else {
        this.secondLevelDrawer = false;
        this.$nextTick(() => this.$root.lastSecondLevelFocusElement?.focus?.());
      }
    },
  },
};
</script>
