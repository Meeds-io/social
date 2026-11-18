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
  <v-divider
    v-if="item.type === 'SEPARATOR'"
    class="my-1"
    role="separator" />
  <div v-else-if="isSitePages">
    <sidebar-list-sub-list
      :item="item" />
  </div>
  <div v-else-if="isSpaces || isSpaceTemplate || isSpaceCategory">
    <component
      :is="$root.displaySequentially ? 'v-hover' : 'div'"
      v-model="hover"
      v-if="displaySpacesList">
      <div
        class="position-relative"
        @focusin="focused = true"
        @focusout="onFocusOut">
        <v-list-item
          :title="spacesTooltip"
          :aria-expanded="spacesExpanded"
          :aria-label="$t(item.name)"
          :class="[$root.iconCollapse && 'mx-0', showArrow && 'pe-15']"
          role="button"
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
        </v-list-item>
        <!-- The arrow follows the entry instead of sitting in it: a screen reader's browse mode reads the entry as one
             object and activates it on Enter, whatever control inside it has the focus.
             Kept for screen readers while hidden: their browse mode neither hovers nor focuses the entry.
             Its hover opens the panel only while it shows: a pointer passing by the row leaves no pending open -->
        <div
          v-if="hasArrow"
          :class="$vuetify.rtl && 'l-0' || 'r-0'"
          class="position-absolute t-0 fill-height d-flex align-center pe-4 z-index-one">
          <div :class="!showArrow && 'd-sr-only'">
            <ripple-hover-button
              :active="!drawerOpened && showArrow"
              :title="arrowLabel"
              :aria-label="arrowLabel"
              :aria-expanded="drawerOpened ? 'true' : 'false'"
              class="ms-2"
              icon
              @ripple-hover="openSpacesList">
              <v-icon
                class="me-0 pa-2 icon-default-color"
                small>
                {{ arrowIcon }}
              </v-icon>
            </ripple-hover-button>
          </div>
        </div>
      </div>
    </component>
    <v-expand-transition v-if="displayItemsInMobile">
      <sidebar-list-sub-list
        v-show="!collapsedSpaces || !$root.displaySequentially"
        :item="item" />
    </v-expand-transition>
  </div>
  <component
    v-else-if="item.url"
    :is="$root.displaySequentially ? 'v-hover' : 'div'"
    v-model="hover">
    <div
      class="position-relative"
      @focusin="focused = true"
      @focusout="onFocusOut">
      <v-list-item
        v-bind="itemAttributes"
        v-on="itemActions"
        :title="tooltip"
        :value="item.url"
        :disabled="!item.url"
        :class="[(!$root.expand || !item.avatar) && avatar && 'ms-n2px', actionDisplayed && 'pe-15']"
        class="d-flex ps-3"
        dense>
        <v-list-item-avatar
          v-if="icon || ($root.expand && item.avatar)"
          class="my-auto me-2"
          min-width="40">
          <v-icon
            v-if="icon"
            class="icon-default-color"
            size="18">
            {{ icon || 'fa-folder' }}
          </v-icon>
        </v-list-item-avatar>
        <v-list-item-avatar
          v-if="avatar"
          :class="($root.expand && item.avatar) && 'me-2' || 'ms-2'"
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
        <space-unread-badge
          v-if="isSpace"
          v-show="!toggleArrow"
          :space-id="spaceId"
          :unread-badge="spaceUnreadCount"
          @refresh="retrieveSpace(true)" />
      </v-list-item>
      <!-- The arrow and the home button follow the entry instead of sitting in it: a screen reader's browse mode reads
           the entry's link as one object and follows it on Enter, whatever control inside it has the focus.
           The arrow is kept for screen readers while hidden; its hover opens the panel only while it shows -->
      <div
        v-if="hasArrow || homeButtonAllowed"
        :class="$vuetify.rtl && 'l-0' || 'r-0'"
        class="position-absolute t-0 fill-height d-flex align-center pe-4 z-index-one">
        <div v-if="hasArrow" :class="!showArrow && 'd-sr-only'">
          <ripple-hover-button
            :active="!drawerOpened && showArrow"
            :title="arrowLabel"
            :aria-label="arrowLabel"
            :aria-expanded="drawerOpened ? 'true' : 'false'"
            class="ms-2"
            icon
            @ripple-hover="openOrCloseDrawer()">
            <v-icon
              class="me-0 pa-2 icon-default-color"
              small>
              {{ arrowIcon }}
            </v-icon>
          </ripple-hover-button>
        </div>
        <v-btn
          v-else-if="homeButtonAllowed"
          v-show="homeButtonShown"
          :title="$t('menu.spaces.makeAsHomePage')"
          :aria-label="$t('menu.spaces.makeAsHomePage')"
          class="ms-2"
          icon
          @click.stop.prevent="$root.$emit('update-home-link-page', item, homeUri)">
          <v-icon
            :class="isHome && 'primary--text' || 'icon-default-color'"
            class="me-0 pa-2"
            small>
            fa-house-user
          </v-icon>
        </v-btn>
      </div>
    </div>
  </component>
</template>
<script>
export default {
  props: {
    item: {
      type: Object,
      default: null,
    },
    activeUrl: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    hover: false,
    focused: false,
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
    homeUri() {
      return this.isPage && this.$navigationUtils.getNavigationNodeHomeUri(this.item?.url);
    },
    isHome() {
      return !!this.homeUri && this.defaultUserPath === this.homeUri;
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
    spacesExpanded() {
      return String(this.$root.displaySequentially ? !this.collapsedSpaces : !!this.drawerOpened);
    },
    isCurrentPage() {
      return !!this.activeUrl && this.activeUrl === this.item.url;
    },
    itemAttributes() {
      const attributes = {};
      attributes.role = this.isUrl && 'link' || 'button';
      // its own name: the buttons inside the entry (arrow, home) must not add to it
      attributes['aria-label'] = this.spaceUnreadCount ? this.$t('menu.entryWithUnread', {0: this.item.name, 1: this.spaceUnreadCount}) : this.item.name;
      if (this.isCurrentPage) {
        attributes['aria-current'] = 'page';
      }
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
    hasArrow() {
      return this.isSite || this.isSpace || this.isSpaceTemplate || this.isSpaceCategory || this.isSpaces;
    },
    toggleArrow() {
      return this.hasArrow && (this.hover || this.focused || this.drawerOpened);
    },
    showArrow() {
      return this.toggleArrow && this.$root.expand;
    },
    homeButtonAllowed() {
      return this.isPage && this.$root.expand && this.$root.allowUserHome && !!this.homeUri;
    },
    homeButtonShown() {
      return this.homeButtonAllowed && (this.hover || this.focused || this.isHome);
    },
    // the entry leaves room at its end for the button shown over it
    actionDisplayed() {
      return this.showArrow || this.homeButtonShown;
    },
    // a screen reader lists one arrow per entry: each names the entry it opens
    arrowLabel() {
      return (this.isSpaces || this.isSpaceTemplate || this.isSpaceCategory)
        ? this.$t('menu.accessToEntrySpacesList', {0: this.$t(this.item.name)})
        : this.$t('menu.accessToEntryPagesList', {0: this.item.name});
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
    // the entry keeps its keyboard state while the focus moves to its own buttons
    onFocusOut(event) {
      this.focused = !!(event.relatedTarget && event.currentTarget?.contains?.(event.relatedTarget));
    },
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
      this.$root.sites = await this.$siteService.getSites('PORTAL', null, 'global', true, true, true, true, true, true, true, true, ['displayed', 'temporal']);
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
