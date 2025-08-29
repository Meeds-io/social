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
  <div v-else-if="isSpaces || isSpaceTemplate || isSpaceCategory">
    <v-hover
      v-model="hover"
      v-if="displaySpacesList"
      :disabled="!$root.displaySequentially">
      <v-list-item
        :title="spacesTooltip"
        :class="$root.iconCollapse && 'mx-0'"
        class="d-flex ps-3"
        dense
        @click="handleSpacesClick">
        <v-list-item-avatar class="me-2 my-auto" min-width="40">
          <v-btn
            v-if="displaySpacesExpandButton"
            :title="spacesTooltip"
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
    <v-expand-transition v-if="displayItemsInMobile">
      <sidebar-list-sub-list
        v-show="!collapsedSpaces || !$root.displaySequentially"
        :item="item" />
    </v-expand-transition>
  </div>
  <v-hover
    v-else-if="item.url"
    v-model="hover">
    <v-list-item
      v-bind="itemAttributes"
      v-on="itemActions"
      :title="tooltip"
      :value="item.url"
      :disabled="!item.url"
      :class="!$root.expand && avatar && 'ms-n2px'"
      class="d-flex ps-3"
      dense>
      <v-list-item-avatar
        v-if="$root.expand || !avatar"
        class="my-auto me-2"
        min-width="40">
        <v-icon
          v-if="!avatar"
          class="icon-default-color"
          size="18">
          {{ icon || 'fa-folder' }}
        </v-icon>
      </v-list-item-avatar>
      <v-list-item-avatar
        v-if="avatar"
        :class="$root.expand && 'me-2' || 'ms-2'"
        class="my-auto"
        min-width="28"
        width="28"
        height="28"
        tile>
        <img
          :src="avatar"
          alt=""
          class="border-radius object-fit-contain"
          width="auto"
          height="auto">
      </v-list-item-avatar>
      <v-card
        v-if="spaceUnreadCount && $root.icon && !$root.expand"
        :class="$vuetify.rtl && 'l-0' || 'r-0'"
        class="hamburger-unread-badge border-radius-circle error-color-background position-absolute t-0 me-4 mt-0"
        width="12"
        height="12"
        flat />
      <v-list-item-content v-if="$root.expand">
        <v-list-item-title class="menu-text-color text-truncate">
          {{ item.name }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action
        v-if="toggleArrow && $root.expand"
        class="my-auto align-center z-index-one position-relative"
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
          v-if="$root.expand && $root.allowUserHome"
          v-show="hover || isHome"
          :title="$t('menu.spaces.makeAsHomePage')"
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
        :space-id="spaceId"
        :unread-badge="spaceUnreadCount"
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
    isLink() {
      return this.item.type === 'LINK';
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
    isSpaceCategory() {
      return this.item.type === 'SPACE_CATEGORY';
    },
    siteName() {
      return this.isSite && this.item?.properties?.siteName;
    },
    spaceId() {
      return this.isSpace && this.item?.properties?.id;
    },
    spaceTemplateId() {
      return this.isSpaceTemplate && Number(this.item?.properties?.spaceTemplateId);
    },
    spaceCategoryId() {
      return this.isSpaceCategory && Number(this.item?.properties?.spaceCategoryId);
    },
    drawerOpened() {
      return (this.isSite && this.$root.openedSiteName === this.siteName)
        || (this.isSpace && this.$root.openedFirstLevelType === 'SPACE' && Number(this.$root.openedSpaceId) === Number(this.spaceId))
        || (this.isSpaces && this.$root.openedSpaces)
        || (this.isSpaceCategory && Number(this.$root.openedSpaceCategoryId) === this.spaceCategoryId)
        || (this.isSpaceTemplate && Number(this.$root.openedSpaceTemplateId) === this.spaceTemplateId);
    },
    arrowIconLeft() {
      return this.$vuetify.rtl && 'fa-arrow-right' || 'fa-arrow-left';
    },
    displaySpacesExpandButton() {
      return this.hover && this.hasItems && this.url;
    },
    displaySpacesExpandFull() {
      return this.hover && this.hasItems && !this.url || null;
    },
    displaySpacesExpandKey() {
      return `sidebar-collapsed-${this.item.type}-${this.item.url || this.spaceId || this.spaceTemplateId || this.spaceCategoryId}`;
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
      return this.item.url && (this.$root.displaySequentially || (!this.isSpaces && !this.isSpaceTemplate && !this.isSpaceCategory && !this.isSpace && !this.isSite));
    },
    url() {
      return this.isUrl && this.item.url && this.$utils.toLinkUrl(this.item.url, {
        urls: true,
        email: true,
        phone: true,
      }) || this.item.url;
    },
    target() {
      return this.isUrl && this.item.target === 'NEW_TAB' && '_blank' || null;
    },
    itemAttributes() {
      const attributes = {};
      if (this.isUrl) {
        attributes.href = this.url;
        attributes.target = this.target;
        if (attributes.target === '_blank') {
          attributes.rel = 'nofollow noreferrer noopener';
        }
      }
      return attributes;
    },
    itemActions() {
      const actions = {};
      if (!this.isUrl) {
        if (this.isSite || this.isSpace) {
          actions.click = this.openOrCloseDrawer;
        }
      } else if (this.url?.includes?.('#')) {
        actions.click = this.forceOpenLink;
      }
      return actions;
    },
    toggleArrow() {
      return (this.isSite || this.isSpace || this.isSpaceTemplate || this.isSpaceCategory || this.isSpaces)
        && (this.hover || this.drawerOpened);
    },
    tooltip() {
      if (this.isSpace) {
        return this.$t('menu.spaceTooltip', {
          0: this.item.name,
        });
      } else if (this.isPage) {
        const descriptions = this.item?.properties?.descriptions && JSON.parse(this.item.properties.descriptions);
        return descriptions?.[eXo.env.portal.language] || descriptions?.['en'] || this.$t('menu.pageNameTooltip', {
          0: this.item.name,
        });
      } else if (this.isSite) {
        return this.$t('menu.siteNameTooltip', {
          0: this.item.name,
        });
      }
      return null;
    },
    spaceUnreadCount() {
      return this.isSpace && this.$root?.unreadPerSpace?.[this.spaceId];
    },
    spacesTooltip() {
      return (this.isSpaceTemplate || this.isSpaceCategory) && (this.collapsedSpaces && this.$t('menu.spacesExpand', {
        0: this.item.name
      }) || this.$t('menu.spacesCollapse', {
        0: this.item.name
      })) || (this.isSpaces && this.$t('menu.spacesTooltip')) || null;
    },
    displayItemsInMobile() {
      return this.$root.displaySequentially || (!this.isSpaces && !this.isSpaceTemplate && !this.isSpaceCategory) || this.item?.properties?.displayItemsInMobile === 'true';
    },
    displayOnlyWhenMember() {
      return this.item?.properties?.displayOnlyWhenMember === 'true';
    },
    notSpaceMember() {
      return this.item?.properties?.notMember === 'true';
    },
    displaySpacesList() {
      return this.hasItems || !this.displayOnlyWhenMember || !this.notSpaceMember;
    },
    iconUrl() {
      if (!this.item.avatar && (this.item?.icon?.includes?.('base64') || this.item?.icon?.includes?.('/'))) {
        return this.item.icon;
      } else {
        return null;
      }
    },
    icon() {
      if (this.item.avatar || this.iconUrl) {
        return null;
      } else {
        return this.item?.icon;
      }
    },
    avatar() {
      return this.item.avatar || this.iconUrl;
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
    document.addEventListener('space-settings-updated', this.handleSpaceUpdated);
    this.collapsedSpaces = (this.isSpaces || this.isSpaceTemplate || this.isSpaceCategory) && window.localStorage.getItem(this.displaySpacesExpandKey) === 'true';
  },
  beforeDestroy() {
    document.removeEventListener('space-settings-updated', this.handleSpaceUpdated);
  },
  methods: {
    handleSpaceUpdated(event) {
      const space = event?.detail;
      if (space && this.space?.id === space?.id) {
        this.retrieveSpace(true);
      }
    },
    handleSpacesClick() {
      if (this.$root.displaySequentially && (this.isSpaceTemplate || this.isSpaceCategory)) {
        this.collapsedSpaces = !this.collapsedSpaces;
      } else {
        this.openSpacesList();
      }
    },
    openSpacesList() {
      this.$root.openedItem = this.item;
      this.$root.$emit('change-spaces-menu',
        this.isSpaceTemplate && this.spaceTemplateId,
        this.isSpaceCategory && this.spaceCategoryId,
        this.isSpaces && this.url,
        this.item.properties?.sortBy,
        this.item.name,
        this.item.type);
    },
    async openOrCloseDrawer() {
      if (this.isSite) {
        if (!this.$root.sites) {
          await this.retrieveSites();
        }
        const site = this.$root.sites?.find(s => s.name === this.siteName);
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
      this.space = await this.$spaceService.getSpaceById(this.spaceId, 'member,managers,favorite,unread,muted', refresh);
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