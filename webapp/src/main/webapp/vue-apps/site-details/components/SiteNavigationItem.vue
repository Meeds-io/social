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
  <v-card
    class="d-flex flex-row transparent"
    transparent
    flat>
    <v-card-content class="pa-0 flex-grow-1 my-auto">
      <a
        :href="uri"
        :target="target"
        :rel="rel"
        :ripple="false"
        class="d-flex px-0"
        @mouseover="showAction = true"
        @mouseleave="showAction = false"
        @keydown="handleKeyDown"
        @focus="changeHover"
        @blur="handleListItemBlur">
        <v-list-item-icon size="20" class="d-flex align-center justify-center my-auto ms-0 me-3">
          <v-icon size="20" class="icon-default-color">
            {{ icon }}
          </v-icon>
        </v-list-item-icon>
        <v-list-item-title class="menu-text-color">
          <div class="d-flex align-center justify-space-between my-auto width-fit-content">
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
      </a>
    </v-card-content>
    <v-card-actions class="pa-0 align-center">
      <div
        v-show="!unreadBadge && enableChangeHome && !isNodeGroup && (isHomeLink || showAction || localExpand)"
        class="ms-auto my-auto flex-shrink-0">
        <v-btn
          :title="$t('menu.spaces.makeAsHomePage')"
          icon
          class="pa-0 ms-2"
          @blur="showAction=false"
          @click="selectHome($event)">
          <v-icon
            :class="isHomeLink && 'primary--text' || 'icon-default-color'"
            small>
            fa-house-user
          </v-icon>
        </v-btn>
      </div>
    </v-card-actions>
  </v-card>
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
  },
  data: () => ({
    homeLink: eXo.env.portal.homeLink,
    showAction: false,
    localExpand: false,
    isShiftTabPressed: false,
  }),
  computed: {
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
    icon() {
      return this.navigation?.icon || 'fa-folder';
    },
    isNodeGroup() {
      return !this.navigation.pageKey;
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
    navigationLabelStyle() {
      return this.unreadBadge > 0 ? { 'max-width': '140px' } : { 'max-width': '200px'};
    },
  },
  watch: {
    showAction() {
      if (this.showAction) {
        this.localExpand = true;
      } else {
        this.localExpand = false;
      }
    },
    drawerOpened(newVal) {
      if (!newVal){
        this.localExpand = true;
      }
    },
  },
  created() {
    document.addEventListener('homeLinkUpdated', this.updateHome);
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
    handleKeyDown(event) {
      this.isShiftTabPressed = event.key === 'Tab' && event.shiftKey;
    },
    handleListItemBlur() {
      if (this.isShiftTabPressed) {
        this.localExpand = false;
        this.showAction = false;
        this.isShiftTabPressed = false;
      }
    },
    changeHover() {
      this.showAction = true;
      this.localExpand = true;
    },
  },
};
</script>