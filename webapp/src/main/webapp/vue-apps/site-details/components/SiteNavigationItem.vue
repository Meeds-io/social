<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2023 Meeds Association
  contact@meeds.io
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
  <a
    v-on="!isMobile && {
      mouseover: () => showAction = true,
      mouseleave: () => showAction = false,
    }"
    :href="uri"
    :aria-current="isCurrentPage && 'page' || null"
    :aria-label="linkLabel"
    :target="target"
    :role="isTreeGroup ? 'button' : null"
    :tabindex="isTreeGroup ? '0' : null"
    :aria-expanded="isTreeGroup ? String(open) : null"
    :rel="rel"
    :ripple="false"
    class="d-flex px-0"
    @keydown.enter.space="toggleGroup"
    @focusin="onFocusIn"
    @focusout="onFocusOut">
    <v-list-item-avatar
      v-if="iconUrl"
      class="d-flex align-center justify-center my-auto ms-0 me-3"
      min-width="20"
      width="20"
      height="20"
      tile>
      <img
        :src="iconUrl"
        alt=""
        class="no-border-radius object-fit-contain"
        width="auto"
        height="auto">
    </v-list-item-avatar>
    <v-list-item-icon
      v-else
      size="20"
      class="d-flex align-center justify-center my-auto ms-0 me-3">
      <v-icon size="20" class="icon-default-color">
        {{ icon }}
      </v-icon>
    </v-list-item-icon>
    <v-list-item-title class="menu-text-color">
      <div class="d-flex align-center justify-space-between my-auto">
        <span class="text-truncate" :style="navigationLabelStyle">{{ navigationLabel }}</span>
        <v-chip
          v-if="unreadBadge && enableUnread"
          color="error-color-background"
          min-width="22"
          height="22"
          dark>
          {{ unreadBadge }}
        </v-chip>
      </div>
    </v-list-item-title>
    <v-list-item-action
      v-if="!unreadBadge && enableChangeHome && !isNodeGroup && (isHomeLink || showAction)"
      class="ms-auto my-auto flex-shrink-0">
      <v-btn
        :title="$t('menu.spaces.makeAsHomePage')"
        :aria-label="$t('menu.spaces.makeAsHomePage')"
        height="36"
        min-width="36"
        icon
        @click="selectHome($event)">
        <v-icon
          :class="isHomeLink && 'primary--text' || 'icon-default-color'"
          small>
          fa-house-user
        </v-icon>
      </v-btn>
    </v-list-item-action>
  </a>
</template>
<script>
export default {
  props: {
    navigation: {
      type: Object,
      default: null
    },
    enableChangeHome: {
      type: Boolean,
      default: false,
    },
    enableUnread: {
      type: Boolean,
      default: false,
    },
    // the tree node holding this entry shows its children
    open: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    homeLink: eXo.env.portal.homeLink,
    showAction: false,
  }),
  computed: {
    // the same page the navigation tree highlights: the current site and its selected node
    isCurrentPage() {
      return !!this.navigation?.uri
        && this.navigation?.siteKey?.name === eXo.env.portal.siteKeyName
        && this.navigation.uri === eXo.env.portal.selectedNodeUri;
    },
    isMobile() {
      return this.$vuetify.breakpoint.width < this.$vuetify.breakpoint.thresholds.md;
    },
    baseSiteUri() {
      if (this.navigation.siteKey.type === 'GROUP') {
        return `${eXo.env.portal.context}/g/${this.navigation.siteKey.name.replace?.(/\//g, ':')}/`;
      } else {
        return `${eXo.env.portal.context}/${this.navigation.siteKey.name}/`;
      }
    },
    uri() {
      return this.$navigationUtils.getNavigationNodeUri(this.baseSiteUri, this.navigation);
    },
    target() {
      return this.$navigationUtils.getNavigationNodeTarget(this.navigation);
    },
    rel() {
      return this.$navigationUtils.getNavigationNodeRel(this.navigation);
    },
    iconUrl() {
      if (this.navigation?.icon?.includes?.('base64') || this.navigation?.icon?.includes?.('/')) {
        return this.navigation.icon;
      } else {
        return null;
      }
    },
    icon() {
      if (this.iconUrl) {
        return null;
      } else {
        return this.navigation?.icon || 'fa-folder';
      }
    },
    isNodeGroup() {
      return !this.navigation.pageKey;
    },
    // a node grouping pages without a page of its own: the keyboard opens it as a button
    isTreeGroup() {
      return !this.uri && !!this.navigation?.children?.length;
    },
    isHomeLink() {
      return this.uri === this.homeLink;
    },
    unreadBadge() {
      return this.$root.openedSpaceId && this.$root?.unreadPerSpace?.[this.$root.openedSpaceId];
    },
    navigationLabel() {
      return this.navigation?.label;
    },
    // the entry's own name: the home button inside it must not add to it
    linkLabel() {
      return this.unreadBadge && this.enableUnread ? this.$t('menu.entryWithUnread', {0: this.navigationLabel, 1: this.unreadBadge}) : this.navigationLabel;
    },
    navigationLabelStyle() {
      return this.unreadBadge > 0 ? { 'max-width': '140px' } : { 'max-width': '200px'};
    },
  },
  watch: {
    open() {
      this.$nextTick(this.describeTreeToggle);
    },
  },
  created() {
    document.addEventListener('homeLinkUpdated', this.updateHome);
  },
  mounted() {
    this.describeTreeToggle();
  },
  beforeDestroy() {
    document.removeEventListener('homeLinkUpdated', this.updateHome);
  },
  methods: {
    selectHome(event) {
      event.preventDefault();
      event.stopPropagation();
      if (this.homeLink !== this.uri) {
        this.$root.$emit('update-home-link', this.navigation);
      }
    },
    updateHome() {
      this.homeLink = eXo.env.portal.homeLink;
    },
    // Enter or Space on a group node: the tree node opens it on click
    toggleGroup(event) {
      if (this.isTreeGroup) {
        event.preventDefault();
        this.$el.click();
      }
    },
    // the entry's actions show on keyboard focus as on hover
    onFocusIn(event) {
      if (!this.isMobile && event.target.matches?.(':focus-visible')) {
        this.showAction = true;
      }
    },
    onFocusOut(event) {
      if (!event.relatedTarget || !this.$el.contains(event.relatedTarget)) {
        this.showAction = false;
      }
    },
    // the tree's own toggle has no name: a group node's is left out, the node itself being the button; a
    // page's is named after its sub-pages
    describeTreeToggle() {
      const toggle = this.$el?.closest?.('.v-treeview-node__root')?.querySelector?.('.v-treeview-node__toggle');
      if (!toggle) {
        return;
      }
      if (this.isTreeGroup) {
        toggle.setAttribute('tabindex', '-1');
        toggle.setAttribute('aria-hidden', 'true');
      } else {
        toggle.setAttribute('aria-label', this.$t('menu.subPagesOf', {0: this.navigationLabel}));
        toggle.setAttribute('aria-expanded', String(this.open));
      }
    },
  },
};
</script>