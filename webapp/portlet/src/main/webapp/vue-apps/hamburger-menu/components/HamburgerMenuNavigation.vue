<template>
  <v-app
    color="transaprent"
    class="HamburgerNavigationMenu"
    flat>
    <a
      v-if="!stickyDisplay"
      class="HamburgerNavigationMenuLink flex border-box-sizing"
      @click="firstLevelDrawer = true">
      <div class="px-5 py-3">
        <v-icon size="24">fa-bars</v-icon>
      </div>
    </a>
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
        :administration-navigations="administrationNavigations"
        :administration-categories="administrationCategories" />
      <hamburger-menu-navigation-first-level
        :sticky-preference="stickyPreference"
        :first-level-drawer="firstLevelDrawer"
        :second-level-drawer="secondLevelDrawer"
        :third-level-drawer="thirdLevelDrawer"
        :second-level="secondLevel"
        :has-administration-navigations="hasAdministrationNavigations"
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
        :administration-navigations="administrationNavigations"
        :administration-categories="administrationCategories" />
      <hamburger-menu-navigation-third-level
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
    administrationCategories: null,
    navigationScope: 'ALL',
    navigationVisibilities: ['displayed'],
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
    visibilityQueryParams() {
      return this.navigationVisibilities.map(visibilityName => `visibility=${visibilityName}`).join('&');
    },
    hasAdministrationNavigations() {
      return this.administrationNavigations?.length;
    },
    displaySequentially() {
      return this.$vuetify.breakpoint.lgAndUp;
    },
    stickyAllowed() {
      return this.$vuetify.breakpoint.lgAndUp;
    },
    stickyDisplay() {
      return this.stickyPreference && this.stickyAllowed;
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
    document.addEventListener('closeDisplayedDrawer', () => this.closeMenu());
    this.retrieveAdministrationMenu().finally(() => this.$root.$applicationLoaded());
  },
  methods: {
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
    closeMenu() {
      this.firstLevelDrawer = false;
      this.secondLevelDrawer = false;
      this.thirdLevelDrawer = false;
      this.space = null;
      this.secondLevel = null;
      window.setTimeout(() => document.dispatchEvent(new CustomEvent('drawerClosed')), 200);
    },
    retrieveAdministrationMenu() {
      if (this.administrationNavigations) {
        return;
      }
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/navigations/group?${this.visibilityQueryParams}`, {
        method: 'GET',
        credentials: 'include',
      })
        .then(resp => resp && resp.ok && resp.json())
        .then(data => this.administrationNavigations = data || [])
        .then(() => this.administrationNavigations?.length && fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/navigations/categories`, {
          method: 'GET',
          credentials: 'include',
        }).then(resp => resp && resp.ok && resp.json()))
        .then(data => this.administrationCategories = data || {});
    },
  },
};
</script>
