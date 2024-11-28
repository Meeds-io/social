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
  <v-divider v-if="item.type === 'SEPARATOR'" class="my-1" />
  <div v-else-if="isSitePages">
    <sidebar-list-sub-list
      :item="item" />
  </div>
  <div v-else-if="isSpaces || isSpaceTemplate">
    <v-hover v-if="hasItems || isSpaces" v-model="hover">
      <v-list-item
        v-bind="url && {
          href: url,
          target: item.target,
          value: item.url,
        }"
        v-on="hover && hasItems && !url && {
          click: () => collapsedSpaces = !collapsedSpaces,
        }"
        :title="displaySpacesExpandFull && (collapsedSpaces && $t('menu.spacesExpand') || $t('menu.spacesCollapse'))"
        :class="$root.iconCollapse && 'mx-0'"
        class="d-flex ps-3"
        dense>
        <v-list-item-avatar class="me-2 my-auto" min-width="40">
          <v-btn
            v-if="displaySpacesExpandButton"
            :title="collapsedSpaces && $t('menu.spacesExpand') || $t('menu.spacesCollapse')"
            height="36"
            width="36"
            icon
            @mousedown.prevent.stop="0"
            @mouseup.prevent.stop="0"
            @click.prevent.stop="collapsedSpaces = !collapsedSpaces">
            <v-icon size="18">{{ spacesIcon }}</v-icon>
          </v-btn>
          <v-icon v-else size="18">{{ spacesIcon }}</v-icon>
        </v-list-item-avatar>
        <v-list-item-content v-if="$root.expand">
          <v-list-item-title class="menu-text-color text-truncate">
            {{ $t(item.name) }}
          </v-list-item-title>
        </v-list-item-content>
        <v-list-item-action
          v-if="toggleArrow && $root.expand"
          class="my-auto align-center"
          @mousedown.stop.prevent
          @mouseup.stop.prevent>
          <ripple-hover-button
            :active="!drawerOpened"
            :title="$t('menu.accessToSpacesList')"
            class="ms-2"
            icon
            @ripple-hover="openSpacesList">
            <v-icon
              class="me-0 pa-2 icon-default-color"
              small>
              {{ arrowIcon }}
            </v-icon>
          </ripple-hover-button>
        </v-list-item-action>
      </v-list-item>
    </v-hover>
    <v-expand-transition>
      <sidebar-list-sub-list
        v-show="!collapsedSpaces"
        :item="item" />
    </v-expand-transition>
  </div>
  <v-hover
    v-else-if="item.url"
    v-model="hover">
    <v-list-item
      v-bind="itemAttributes"
      v-on="itemActions"
      :value="item.url"
      :disabled="!item.url"
      :class="!$root.expand && item.avatar && 'ms-n2px'"
      class="d-flex ps-3"
      dense>
      <v-list-item-avatar
        v-if="$root.expand || !item.avatar"
        class="my-auto me-2"
        min-width="40">
        <v-icon
          v-if="!item.avatar"
          class="icon-default-color"
          size="18">
          {{ item.icon || 'fa-folder' }}
        </v-icon>
      </v-list-item-avatar>
      <v-list-item-avatar
        v-if="item.avatar"
        :class="$root.expand && 'me-2' || 'ms-2'"
        class="my-auto"
        min-width="28"
        width="28"
        height="28"
        tile>
        <img
          :src="item.avatar"
          :alt="item.name"
          class="border-radius"
          width="28"
          height="auto">
      </v-list-item-avatar>
      <v-list-item-content v-if="$root.expand">
        <v-list-item-title class="menu-text-color text-truncate">
          {{ item.name }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action
        v-if="toggleArrow && $root.expand"
        class="my-auto align-center"
        @mousedown.stop.prevent
        @mouseup.stop.prevent>
        <ripple-hover-button
          :active="!drawerOpened"
          :title="$t('menu.accessToPagesList')"
          class="ms-2"
          icon
          @ripple-hover="openOrCloseDrawer()">
          <v-icon
            class="me-0 pa-2 icon-default-color"
            small>
            {{ arrowIcon }}
          </v-icon>
        </ripple-hover-button>
      </v-list-item-action>
      <v-list-item-action
        v-else-if="isPage"
        class="my-auto align-center"
        @mousedown.stop.prevent
        @mouseup.stop.prevent>
        <v-btn
          v-if="$root.expand"
          v-show="hover || isHome"
          class="ms-2"
          icon
          @click.stop.prevent="$root.$emit('update-home-link-page', item)">
          <v-icon
            :class="isHome && 'primary--text' || 'icon-default-color'"
            class="me-0 pa-2"
            small>
            fa-house-user
          </v-icon>
        </v-btn>
      </v-list-item-action>
      <space-unread-badge
        v-if="isSpace"
        v-show="!toggleArrow"
        :space-id="id"
        :unread-badge="space?.unread"
        @refresh="retrieveSpace(true)" />
    </v-list-item>
  </v-hover>
</template>
<script>
export default {
  props: {
    item: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    hover: false,
    collapsedSpaces: false,
    space: null,
  }),
  computed: {
    id() {
      return this.item?.properties?.id;
    },
    menuItems() {
      return this.item?.items;
    },
    hasItems() {
      return this.menuItems?.length;
    },
    defaultUserPath() {
      return this.$root.defaultUserPath;
    },
    isHome() {
      return this.isPage && this.defaultUserPath === this.item?.url;
    },
    isSitePages() {
      return this.item.type === 'SITE' && this.item.properties.expandPages === 'true';
    },
    isSpaces() {
      return this.item.type === 'SPACES';
    },
    isSpace() {
      return this.item.type === 'SPACE';
    },
    isSite() {
      return this.item.type === 'SITE' && this.item.properties.expandPages !== 'true';
    },
    isPage() {
      return this.item.type === 'PAGE';
    },
    isSpaceTemplate() {
      return this.item.type === 'SPACE_TEMPLATE';
    },
    drawerOpened() {
      return (this.isSite && this.$root.openedSiteName === this.item?.properties?.siteName)
        || (this.isSpace && Number(this.$root.openedSpaceId) === Number(this.item?.properties?.id))
        || (this.isSpaces && this.$root.openedSpacesUrl === this.url)
        || (this.isSpaceTemplate && Number(this.$root.openedSpaceTemplateId) === Number(this.item?.properties?.spaceTemplateId));
    },
    arrowIconLeft() {
      return this.$vuetify.rtl && 'fa-arrow-right' || 'fa-arrow-left';
    },
    displaySpacesExpandButton() {
      return this.hover && this.hasItems && this.url;
    },
    displaySpacesExpandFull() {
      return this.hover && this.hasItems && !this.url;
    },
    displaySpacesExpandKey() {
      return `sidebar-collapsed-${this.item.type}-${this.item.url || this.item?.properties?.id || this.item?.properties?.spaceTemplateId}`;
    },
    spacesIcon() {
      return this.hover && this.hasItems ? (this.collapsedSpaces && `fa-caret-${this.$vuetify.rtl && 'left' || 'right'}` || 'fa-caret-down') : (this.item.icon || 'fa-folder');
    },
    arrowIconRight() {
      return this.$vuetify.rtl && 'fa-arrow-left' || 'fa-arrow-right';
    },
    arrowIcon() {
      return this.drawerOpened && this.arrowIconLeft || this.arrowIconRight;
    },
    isUrl() {
      return (this.isPage || this.$root.displaySequentially) && this.item.url;
    },
    url() {
      return this.isUrl && this.item.url && Autolinker.parse(this.item.url, {
        email: true,
      })?.[0]?.getUrl?.() || this.item.url;
    },
    target() {
      return this.isUrl && this.item.target === 'NEW_TAB' && '_blank' || null;
    },
    itemAttributes() {
      const attributes = {};
      if (this.isUrl) {
        attributes.href = this.url;
        attributes.target = this.item.target;
      }
      return attributes;
    },
    itemActions() {
      const actions = {};
      if (!this.isUrl) {
        if (this.isSite) {
          actions.click = this.openOrCloseDrawer;
        } else if (this.isSpace) {
          actions.click = this.openOrCloseDrawer;
        }
      } else if (this.url?.includes?.('#')) {
        actions.click = this.forceOpenLink;
      }
      return actions;
    },
    spaceUnreadCount() {
      return this.isSpace && 2;
    },
    toggleArrow() {
      return (this.isSite || this.isSpace || this.isSpaceTemplate || this.isSpaces)
        && (this.hover || this.drawerOpened);
    },
  },
  watch: {
    hover() {
      if (this.hover) {
        this.initHover();
      }
    },
    collapsedSpaces() {
      if (this.collapsedSpaces) {
        window.localStorage.setItem(this.displaySpacesExpandKey, 'true');
      } else {
        window.localStorage.removeItem(this.displaySpacesExpandKey);
      }
    },
  },
  created() {
    this.collapsedSpaces = (this.isSpaces || this.isSpaceTemplate) && window.localStorage.getItem(this.displaySpacesExpandKey) === 'true';
  },
  methods: {
    openSpacesList() {
      this.$root.$emit('change-spaces-menu',
        this.isSpaceTemplate && this.item.properties?.spaceTemplateId,
        this.isSpaces && this.url,
        this.item.properties?.sortBy,
        this.item.name);
    },
    async openOrCloseDrawer() {
      if (this.isSite) {
        if (!this.$root.sites) {
          await this.retrieveSites();
        }
        const site = this.$root.sites?.find(s => s.name === this.item?.properties?.siteName);
        this.$root.$emit('change-site-menu', site);
      } else if (this.isSpace) {
        if (!this.space) {
          await this.retrieveSpace();
        }
        this.$root.$emit('change-space-menu', this.space);
      }
    },
    async initHover() {
      if (this.isSite && !this.$root.sites) {
        await this.retrieveSites();
      } else if (this.isSpace && !this.space) {
        await this.retrieveSpace();
      }
    },
    async retrieveSites() {
      this.$root.sites = await this.$siteService.getSites('PORTAL', null, 'global', true, true, true, true, true, true, true, true, true, ['displayed', 'temporal']);
    },
    async retrieveSpace(refresh) {
      this.space = await this.$spaceService.getSpaceById(this.id, 'member,managers,favorite,unread,muted', refresh);
      this.$set(this.$root.unreadPerSpace, this.space.id, this.space.unread && Number(this.space.unread) || 0);
    },
    forceOpenLink() {
      if (this.target === '_blank') {
        window.open(this.url);
      } else {
        window.location.href = this.url;
      }
    },
  },
};
</script>