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
    class="d-flex px-0"
    :href="uri"
    :rel="rel"
    :ripple="false"
    :target="target"
    @mouseleave="showAction = false"
    @mouseover="showAction = true">
    <v-list-item-icon
      class="d-flex align-center justify-center my-auto ms-0 me-3"
      size="20">
      <v-icon
        class="icon-default-color"
        size="20">
        {{ icon }}
      </v-icon>
    </v-list-item-icon>
    <v-list-item-title class="menu-text-color">
      <div class="d-flex align-center justify-space-between my-auto">
        <span
          class="text-truncate"
          :style="navigationLabelStyle">{{ navigationLabel }}</span>
        <v-chip
          v-if="unreadBadge && enableUnread"
          color="error-color-background"
          dark
          height="22"
          min-width="22">
          {{ unreadBadge }}
        </v-chip>
      </div>
    </v-list-item-title>
    <v-list-item-action
      v-if="!unreadBadge && enableChangeHome && !isNodeGroup && (isHomeLink || showAction)"
      class="ms-auto my-auto flex-shrink-0">
      <v-btn
        height="36"
        icon
        min-width="36"
        :title="$t('menu.spaces.makeAsHomePage')"
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
        default: null,
      },
      enableChangeHome: {
        type: Boolean,
        default: false,
      },
      enableUnread: {
        type: Boolean,
        default: false,
      },
    },
    data: () => ({
      homeLink: eXo.env.portal.homeLink,
      showAction: false,
    }),
    computed: {
      baseSiteUri () {
        if (this.navigation.siteKey.type === 'GROUP') {
          return `${eXo.env.portal.context}/g/${this.navigation.siteKey.name.replace?.(/\//g, ':')}/`;
        } else {
          return `${eXo.env.portal.context}/${this.navigation.siteKey.name}/`;
        }
      },
      uri () {
        return eXo.$navigationUtils.getNavigationNodeUri(this.baseSiteUri, this.navigation);
      },
      target () {
        return eXo.$navigationUtils.getNavigationNodeTarget(this.navigation);
      },
      rel () {
        return eXo.$navigationUtils.getNavigationNodeRel(this.navigation);
      },
      icon () {
        return this.navigation?.icon || 'fa-folder';
      },
      isNodeGroup () {
        return !this.navigation.pageKey;
      },
      isHomeLink () {
        return this.uri === this.homeLink;
      },
      unreadBadge () {
        return this.$root.openedSpaceId && this.$root?.unreadPerSpace?.[this.$root.openedSpaceId];
      },
      navigationLabel () {
        return this.navigation?.label;
      },
      navigationLabelStyle () {
        return this.unreadBadge > 0 ? { 'max-width': '140px' } : { 'max-width': '200px' };
      },
    },
    created () {
      document.addEventListener('homeLinkUpdated', this.updateHome);
    },
    beforeUnmount () {
      document.removeEventListener('homeLinkUpdated', this.updateHome);
    },
    methods: {
      selectHome (event) {
        event.preventDefault();
        event.stopPropagation();
        if (this.homeLink !== this.uri) {
          this.$root.$emit('update-home-link', this.navigation);
        }
      },
      updateHome () {
        this.homeLink = eXo.env.portal.homeLink;
      },
    },
  };
</script>