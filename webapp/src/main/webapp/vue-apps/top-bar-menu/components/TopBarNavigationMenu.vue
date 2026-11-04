<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

-->
<template>
  <v-app :class="componentId" class="topBarMenu">
    <v-footer
      v-if="isMobile && mobileNavigations.length"
      class="pt-0 pr-0 pl-0 elevation-2"
      inset
      fixed>
      <v-tabs
        class="navigation-mobile-menu"
        :class="isTopBarElement && 'layout-top-bar' || ''"
        v-model="tab"
        color="none"
        optional
        background-color="transparent"
        height="57"
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
      v-else-if="navigations.length"
      v-model="tab"
      ref="tabs"
      class="topbar-navigation-tabs"
      background-color="transparent"
      color="none"
      optional
      height="57"
      slider-size="4">
      <navigation-menu-item
        v-for="(navigation, index) in desktopNavigations"
        ref="navigationItems"
        :key="navigation.id"
        :navigation="navigation"
        :base-site-uri="getNavigationBaseUri(index)"
        :selected-path="selectedPath"
        @update-navigation-state="updateNavigationState" />
    </v-tabs>
    <!-- Gives the menu its preferred width (max-content) and its smallest one, Home and More (min-content):
         the tabs themselves contribute no width, so the Topbar shrinks the menu before its other applications -->
    <div
      v-if="!isMobile && navigations.length"
      class="topbar-navigation-sizer"
      aria-hidden="true">
      <div
        v-for="(width, index) in sizerWidths"
        :key="index"
        :style="`width: ${width}px`"></div>
    </div>
  </v-app>
</template>
<script>
export default {
  data: () => ({
    componentId: `top-bar-menu-${parseInt(Math.random() * 65536)}`,
    initialized: false,
    mounted: false,
    navigations: [],
    mobileNavigations: [],
    scope: 'ALL',
    globalScope: 'children',
    visibility: ['displayed', 'temporal'],
    siteType: eXo.env.portal.siteKeyType?.toUpperCase(),
    siteName: eXo.env.portal.siteKeyType === 'portal'
      || eXo.env.portal.siteKeyName === 'global'
      ? eXo.env.portal.portalName
      : eXo.env.portal.siteKeyName,
    exclude: 'global',
    tab: null,
    measuring: false,
    tabWidths: [],
    moreTabWidth: 0,
    visibleCount: null,
    resizeObserver: null,
  }),
  computed: {
    isMobile() {
      return this.$vuetify.breakpoint.name === 'sm' || this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'md';
    },
    smallDevice() {
      return this.$vuetify.breakpoint.name === 'sm' || this.$vuetify.breakpoint.name === 'xs';
    },
    mediumDevice() {
      return this.$vuetify.breakpoint.name === 'md';
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
    siteUri() {
      return this.siteType === 'GROUP' ? `g/${this.siteName?.replaceAll('/', ':')}` : this.siteName;
    },
    selectedPath() {
      const pathname = window.location.pathname;
      if (pathname.toLowerCase() === `${eXo.env.portal.context}/${this.siteUri}/`.toLowerCase()
          || pathname.toLowerCase() === `${eXo.env.portal.context}/${this.siteUri}`.toLowerCase()) {
        return `${eXo.env.portal.context}/${this.siteUri}/${eXo.env.portal.selectedNodeUri}`;
      } else {
        return pathname;
      }
    },
    baseSiteUri() {
      return `${eXo.env.portal.context}/${this.siteUri}/`;
    },
    isTopBarElement() {
      return this.$root.isTopBarElement;
    },
    desktopNavigations() {
      if (this.measuring) {
        return this.navigations.length > 1 ? [...this.navigations, this.buildMoreNavigation(this.navigations.slice(1))] : this.navigations;
      } else if (this.visibleCount === null || this.visibleCount >= this.navigations.length) {
        return this.navigations;
      } else {
        return [...this.navigations.slice(0, this.visibleCount), this.buildMoreNavigation(this.navigations.slice(this.visibleCount))];
      }
    },
    preferredWidth() {
      return this.getEntriesWidth(this.tabWidths.length);
    },
    minimalWidth() {
      return this.tabWidths.length > 1 ? Math.min(this.preferredWidth, this.getEntriesWidth(1)) : this.preferredWidth;
    },
    sizerWidths() {
      if (!this.minimalWidth) {
        return [];
      }
      const remainingWidth = this.preferredWidth - this.minimalWidth;
      const piecesCount = Math.ceil(remainingWidth / this.minimalWidth);
      return [this.minimalWidth, ...new Array(piecesCount).fill(remainingWidth / piecesCount)];
    },
  },
  watch: {
    isMobile() {
      if (this.isMobile) {
        this.refreshMobileNavigations();
      } else {
        this.computeSiteBodyMargin();
        this.measureNavigations();
      }
    },
    smallDevice() {
      if (this.smallDevice) {
        this.refreshMobileNavigations();
      }
    },
    mediumDevice() {
      if (this.mediumDevice) {
        this.refreshMobileNavigations();
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
    document.addEventListener('space-settings-updated', this.init);
    this.init();
  },
  beforeDestroy() {
    document.removeEventListener('space-settings-updated', this.init);
    document.fonts?.removeEventListener?.('loadingdone', this.measureNavigations);
    this.resizeObserver?.disconnect?.();
  },
  mounted() {
    this.mounted = true;
    if (window.ResizeObserver) {
      this.resizeObserver = new ResizeObserver(this.handleResize);
      this.resizeObserver.observe(this.$el);
    }
    // The tabs are measured again each time a web font is loaded: a fallback font gives other widths
    document.fonts?.addEventListener?.('loadingdone', this.measureNavigations);
  },
  methods: {
    init() {
      return this.getNavigations()
        .finally(() => this.initialized = true);
    },
    cacheMenuContent() {
      sessionStorage.setItem(this.$root.cacheId, document.querySelector('#topBarMenu').innerHTML);
    },
    hideCachedMenu() {
      this.refreshWindowSize();
      const menuElements = document.querySelectorAll('.topBarMenu');
      for (const menuElement of menuElements) {
        if (!menuElement.classList.contains(this.componentId)) {
          menuElement.remove();
        }
      }
      window.setTimeout(() => this.cacheMenuContent(), 2000);
    },
    getNavigationBaseUri(index) {
      const navigationBaseUri = `${this.baseSiteUri}${this.navigations[0].name}`;
      return index && `${navigationBaseUri}/` || navigationBaseUri;
    },
    async getNavigations() {
      if (this.$root.parentNodeId) {
        const navigations = await this.$navigationService.getNavigations(this.siteName, this.siteType, this.scope, this.visibility, null, this.$root.parentNodeId, null, true);
        this.navigations = navigations || [];
        this.buildNavigations();
      }
    },
    updateNavigationState(value) {
      // The measuring pass creates the More entry over every other entry: its selection is not the menu's
      if (!this.measuring) {
        this.tab = value;
      }
    },
    buildNavigations() {
      if (this.navigations.length && this.navigations[0].children?.length) {
        this.navigations.push(...this.navigations[0].children);
        this.navigations[0].children = [];
      } else {
        this.navigations = [];
      }
      if (this.isMobile) {
        this.refreshMobileNavigations();
      } else {
        this.measureNavigations();
      }
    },
    buildMoreNavigation(children) {
      return {
        id: 'topbar-navigation-more',
        tabClass: 'topbar-navigation-more',
        name: 'more',
        label: this.$t('topBar.navigation.label.more'),
        children,
      };
    },
    getEntriesWidth(count) {
      const entriesWidth = this.tabWidths.slice(0, count).reduce((sum, width) => sum + width, 0);
      return count < this.tabWidths.length ? entriesWidth + this.moreTabWidth : entriesWidth;
    },
    // Renders every entry and the More entry once, to read the width each tab takes with its label
    async measureNavigations() {
      if (this.isMobile || !this.navigations.length || this.measuring || !this.$el.isConnected) {
        return;
      }
      this.measuring = true;
      try {
        await this.$nextTick();
        const tabWidthById = {};
        (this.$refs.navigationItems || []).forEach(item => {
          const tab = this.findTabElement(item);
          tabWidthById[item.navigation.id] = tab ? Math.ceil(tab.getBoundingClientRect().width) : 0;
        });
        this.tabWidths = this.navigations.map(navigation => tabWidthById[navigation.id] || 0);
        this.moreTabWidth = tabWidthById['topbar-navigation-more'] || 0;
      } finally {
        this.measuring = false;
      }
      this.refreshVisibleCount();
    },
    // A menu item's root element is its v-menu, rendered apart from the tab that activates it
    findTabElement(vm) {
      return this.findTabComponent(vm)?.$el || null;
    },
    findTabComponent(vm) {
      if (vm.$el?.classList?.contains?.('v-tab')) {
        return vm;
      }
      for (const child of vm.$children) {
        const tab = this.findTabComponent(child);
        if (tab) {
          return tab;
        }
      }
      return null;
    },
    handleResize() {
      // A measure taken before the menu was laid out reads no width at all: measure again once it has one
      if (this.$el.clientWidth && this.navigations.length && !this.tabWidths.some(Boolean)) {
        this.measureNavigations();
      } else {
        this.refreshVisibleCount();
      }
    },
    async refreshVisibleCount() {
      if (!this.isMobile && !this.measuring && this.tabWidths.length) {
        // The tabs' own width: the menu's side margins are already left out of it
        this.visibleCount = this.computeVisibleCount(this.$refs.tabs?.$el?.clientWidth || 0);
        await this.$nextTick();
        // The tabs bar recomputes its overflow on a window resize or a change of its items or value, not when only its
        // own width changes, as when the sizer's widths arrive: measured narrower, it would keep scrolling tabs that fit
        this.$refs.tabs?.$refs?.items?.setWidths?.();
        this.refreshSelectedTab();
      }
    },
    // Selects the entry whose page is, or holds, the current page, or More when that entry is one of those it lists
    refreshSelectedTab() {
      const selectedIndex = this.navigations.findIndex((navigation, index) => this.holdsSelectedPath(navigation, this.getNavigationBaseUri(index)));
      if (selectedIndex < 0) {
        return;
      }
      const selectedNavigation = this.desktopNavigations.includes(this.navigations[selectedIndex]) ? this.navigations[selectedIndex] : this.desktopNavigations[this.desktopNavigations.length - 1];
      const selectedItem = (this.$refs.navigationItems || []).find(item => item.navigation === selectedNavigation);
      const selectedTab = selectedItem && this.findTabComponent(selectedItem);
      if (selectedTab) {
        // A tab with no link takes as its value its index in the tabs group, in the order the tabs registered
        this.tab = selectedTab.value || selectedTab.tabsBar?.items?.indexOf?.(selectedTab);
      }
    },
    holdsSelectedPath(navigation, baseSiteUri) {
      return this.$navigationUtils.getNavigationNodeUri(baseSiteUri, navigation) === this.selectedPath
        || !!navigation.children?.some?.(child => this.holdsSelectedPath(child, baseSiteUri));
    },
    // Every entry is displayed when they all fit; otherwise as many entries as fit followed by More,
    // and never less than the first entry and More
    computeVisibleCount(availableWidth) {
      const entriesCount = this.tabWidths.length;
      if (entriesCount <= 1 || this.getEntriesWidth(entriesCount) <= availableWidth) {
        return entriesCount;
      }
      let count = entriesCount - 1;
      while (count > 1 && this.getEntriesWidth(count) > availableWidth) {
        count--;
      }
      return count;
    },
    refreshMobileNavigations() {
      if ((this.$vuetify.breakpoint.name === 'md' && this.navigations.length > 3)
          || ((this.$vuetify.breakpoint.name === 'sm' || this.$vuetify.breakpoint.name === 'xs') && this.navigations.length > 2)) {
        this.mobileNavigations = [];
        let children = [];
        if (this.$vuetify.breakpoint.name === 'md') {
          children = this.navigations.slice(2, this.navigations.length);
          this.mobileNavigations.push(...this.navigations.slice(0, 2));
        } else {
          children = this.navigations.slice(1, this.navigations.length);
          this.mobileNavigations.push(...this.navigations.slice(0, 1));
        }
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