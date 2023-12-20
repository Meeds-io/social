<!--
   * This file is part of the Meeds project (https://meeds.io/).
   *
   * Copyright (C) 2023 Meeds Association
   * contact@meeds.io
   *
   * This program is free software; you can redistribute it and/or
   * modify it under the terms of the GNU Lesser General Public
   * License as published by the Free Software Foundation; either
   * version 3 of the License, or (at your option) any later version.
   *
   * This program is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty of
   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
   * Lesser General Public License for more details.
   *
   * You should have received a copy of the GNU Lesser General Public License
   * along with this program; if not, write to the Free Software Foundation,
   * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->

<template>
  <v-app :class="componentId" class="topBarMenu">
    <v-footer
      v-if="isMobile && mobileNavigations.length"
      class="white pt-0 pr-0 pl-0 elevation-2"
      inset
      fixed>
      <v-tabs
        class="navigation-mobile-menu"
        v-model="tab"
        optional
        height="56"
        slider-size="4">
        <navigation-mobile-menu-item
          v-for="(navigation, index) in mobileNavigations"
          :key="navigation.id"
          :navigation="navigation"
          :base-site-uri="getNavigationBaseUri(index)"
          @update-navigation-state="updateNavigationState" />
      </v-tabs>
    </v-footer>
    <v-tabs
      v-else
      v-model="tab"
      show-arrows
      center-active
      optional
      height="56"
      slider-size="4">
      <navigation-menu-item
        v-for="(navigation, index) in navigations"
        :key="navigation.id"
        :navigation="navigation"
        :base-site-uri="getNavigationBaseUri(index)"
        @update-navigation-state="updateNavigationState" />
    </v-tabs>
  </v-app>
</template>

<script>
export default {
  data: () => ({
    componentId: `top-bar-menu-${parseInt(Math.random() * 65536)}`,
    BASE_SITE_URI: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/`,
    initialized: false,
    mounted: false,
    navigations: [],
    mobileNavigations: [],
    scope: 'ALL',
    globalScope: 'children',
    visibility: ['displayed', 'temporal'],
    siteType: 'PORTAL',
    exclude: 'global',
    tab: null,
    navigationTabState: 'topNavigationTabState',
  }),
  computed: {
    isMobile() {
      return this.$vuetify.breakpoint.name === 'sm' || this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'md';
    },
    navigationsLength() {
      return this.mobileNavigations?.length || 0;
    },
    loadingFinished() {
      return this.initialized && this.mounted;
    },
    parentScrollableSelector() {
      return document.querySelector('.site-scroll-parent .UIPageBody') ? '.site-scroll-parent' : '.site-scroll-parent .UIPage';
    },
  },
  watch: {
    isMobile() {
      if (this.isMobile) {
        this.refreshMobileNavigations();
      } else {
        this.computeSiteBodyMargin();
      }
    },
    navigationsLength() {
      this.computeSiteBodyMargin();
    },
    loadingFinished() {
      if (this.loadingFinished) {
        this.$root.$applicationLoaded();
        window.setTimeout(this.hideCachedMenu, 200);
      }
    },
  },
  created() {
    this.init();
    window.addEventListener('beforeunload', this.cacheMenuContent);
  },
  mounted() {
    this.mounted = true;
  },
  methods: {
    init() {
      return this.getNavigations()
        .then(() => this.getActiveTab())
        .finally(() => this.initialized = true);
    },
    cacheMenuContent() {
      sessionStorage.setItem('topBarMenu', document.querySelector('#topBarMenu').innerHTML);
    },
    hideCachedMenu() {
      this.refreshWindowSize();
      const menuElements = document.querySelectorAll('.topBarMenu');
      for (const menuElement of menuElements) {
        if (!menuElement.classList.contains(this.componentId)) {
          menuElement.remove();
        }
      }
    },
    getNavigationBaseUri(index) {
      const navigationBaseUri = `${this.BASE_SITE_URI}${this.navigations[0].name}`;
      return index && `${navigationBaseUri}/` || navigationBaseUri;
    },
    getNavigations() {
      const siteName = eXo.env.portal.portalName;
      return this.$navigationService.getNavigations(siteName, this.siteType, this.globalScope, this.visibility, this.exclude, null, null, true)
        .then(navs => {
          if (navs.length) {
            const homeNavigation = navs[0];
            return this.$navigationService.getNavigations(siteName, this.siteType, this.scope, this.visibility, null, homeNavigation.id, null, true)
              .then(navigations => {
                this.navigations = navigations || [];
                this.constructNavigations();
              });
          }
        });
    },
    updateNavigationState(value) {
      sessionStorage.setItem(this.navigationTabState,  value);
    },
    constructNavigations() {
      if (this.navigations.length && this.navigations[0].children?.length) {
        this.navigations.push(...this.navigations[0].children);
        this.navigations[0].children = [];
      } else {
        this.navigations = [];
      }
      if (this.isMobile) {
        this.refreshMobileNavigations();
      }
    },
    refreshMobileNavigations() {
      if (this.navigations.length > 3) {
        this.mobileNavigations = [];
        const children = this.navigations.slice(2, this.navigations.length);
        this.mobileNavigations.push(...this.navigations.slice(0, 2));
        this.mobileNavigations.push({
          id: 0,
          name: 'more',
          label: this.$t('topBar.navigation.label.more'),
          children: children
        });
      } else {
        this.mobileNavigations = this.navigations;
      }
      this.computeSiteBodyMargin();
    },
    getActiveTab() {
      const siteName = eXo.env.portal.portalName;
      let pathname = location.pathname;
      if (pathname === `${eXo.env.portal.context}/${siteName}/`) {
        pathname = `${eXo.env.portal.context}/${siteName}/${eXo.env.portal.selectedNodeUri}`;
        this.updateNavigationState(pathname);
      }
      this.tab = sessionStorage.getItem(this.navigationTabState);
      if (pathname !== this.tab && !pathname.startsWith(this.tab)) {
        this.tab = pathname;
      }
    },
    computeSiteBodyMargin() {
      if (this.isMobile && this.navigationsLength > 0) {
        window.setTimeout(() => {
          $(this.parentScrollableSelector).css('margin-bottom', '70px');
        }, 200);
      } else {
        $(this.parentScrollableSelector).css('margin-bottom', '');
      }
    },
    refreshWindowSize() {
      this.$nextTick().then(() => {
        window.setTimeout(() => window.dispatchEvent(new Event('resize')), 200);
      });
    },
  }
};
</script>