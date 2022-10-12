<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2022 Meeds Association
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
  <v-container class="recentDrawer" flat>
    <v-flex class="d-flex pa-0">
      <v-list-item-icon class="d-flex d-sm-none backToMenu my-5 mx-2 icon-default-color justify-center" @click="closeMenu()">
        <v-icon class="fas fa-arrow-left" small />
      </v-list-item-icon>
      <v-list-item class="width-min-content pt-3">
        <v-list-item-avatar
          class="spaceAvatar mt-0 mb-0 align-self-start"
          :width="isMobile && '45' || '60'"
          :height="isMobile && '45' || '60'">
          <v-img
            class="object-fit-cover"
            :src="avatar" />
        </v-list-item-avatar>
        <v-list-item-content class="pb-0 pt-0">
          <p class="blue--text text--darken-3 font-weight-bold text-truncate-2">{{ spaceDisplayName }}</p>
          <v-list-item-subtitle>
            {{ membersCount }} {{ $t('space.logo.banner.popover.members') }}
          </v-list-item-subtitle>
          <p v-if="!isMobile" class="text-truncate-4 text-caption text--primary font-weight-medium mb-0 mt-2">
            {{ description }}
          </p>
        </v-list-item-content>
      </v-list-item>
    </v-flex>
    <p v-if="isMobile" class="text-truncate-4 text-caption text--primary font-weight-medium pt-3 px-4">
      {{ description }}
    </p>
    <v-flex>
      <v-list-item>
        <v-list-item-content class="body-2 grey--text text-truncate text--darken-1">
          {{ $t('space.logo.banner.popover.managers') }}
        </v-list-item-content>
        <v-list-item-action>
          <exo-user-avatars-list
            :users="managersToDisplay"
            :icon-size="30"
            :popover="false"
            max="1"
            avatar-overlay-position
            @open-detail="openDetails()" />
        </v-list-item-action>
      </v-list-item>
      <v-divider />
    </v-flex>
    <v-flex>
      <v-list-item-action class="my-0 py-3 d-flex flex-row align-center justify-space-around me-0">
        <v-btn
          v-if="homeLink !== url"
          link
          icon 
          @click="selectHome()">
          <v-icon class="me-0 pa-2 icon-default-color" small>
            fa-house-user
          </v-icon>
        </v-btn>
        <v-btn
          icon 
          disabled
          @click="leftNavigationActionEvent('openInNewTab')">
          <v-icon class="me-0 pa-2 icon-default-color" small>
            fa-envelope-open-text
          </v-icon>
        </v-btn>
        <exo-space-favorite-action
          v-if="favoriteActionEnabled"
          :is-favorite="isFavorite"
          :space-id="spaceId"
          entity-type="spaces_left_navigation" />
        <extension-registry-components
          :params="params"
          name="SpacePopover"
          type="space-popover-action"
          parent-element="div"
          element="div"
          element-class="mx-auto ma-lg-0"
          class="space-panel-action" />
        <span
          v-for="extension in enabledExtensionComponents"
          class="space-panel-action"
          :key="extension.key"
          :class="`${extension.appClass} ${extension.typeClass}`"
          :ref="extension.key"></span>
      </v-list-item-action>
    </v-flex>
    <v-flex>
      <v-list>
        <v-list-item
          v-for="navigation in spaceNavigations"
          :key="navigation.id"
          :href="navigation.uri">
          <v-list-item-icon class="me-3 my-3 d-flex">
            <i 
              aria-hidden="true" 
              :class="`${applicationIcon(navigation.icon)} icon-default-color icon-default-size`"> </i>
          </v-list-item-icon>
          <v-list-item-content>
            {{ navigation.label }}
          </v-list-item-content>
        </v-list-item>
      </v-list>
    </v-flex>
  </v-container>
</template>
<script>
export default {
  data() {
    return {
      spaceNavigations: [],
      favoritesSpaceEnabled: eXo.env.portal.spaceFavoritesEnabled,
      externalExtensions: [],
    };
  },
  props: {
    space: {
      type: Object,
      default: null
    },
    homeLink: {
      type: String,
      default: null
    }
  },
  computed: {
    spaceId() {
      return this.space?.id;
    },
    spaceDisplayName() {
      return this.space?.displayName;
    },
    avatar() {
      return this.space?.avatarUrl;
    },
    membersCount() {
      return this.space?.membersCount;
    },
    description() {
      return this.space?.description;
    },
    managersToDisplay() {
      return this.space?.managers;
    },
    isFavorite() {
      return this.space?.isFavorite;
    },
    favoriteActionEnabled() {
      return this.favoritesSpaceEnabled;
    },
    params() {
      return {
        identityType: 'space',
        identityId: eXo.env.portal.spaceId
      };
    },
    enabledExtensionComponents() {
      return this.externalExtensions.filter(extension => extension.enabled);
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'sm' || this.$vuetify.breakpoint.name === 'xs';
    },
  },
  watch: {
    spaceId: {
      immediate: true,
      handler(newVal, oldVal) {
        if (newVal !== oldVal) {
          this.refreshExtensions();
        }
      },
    },
  },
  created() {
    this.retrieveSpaceNavigations(this.spaceId);
  },
  methods: {
    retrieveSpaceNavigations(spaceId) {
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${spaceId}/navigations`, {
        method: 'GET',
        credentials: 'include',
      })
        .then(resp => resp && resp.ok && resp.json())
        .then(data => {
          data.forEach(navigation => {
            navigation.uri = `${this.url()}${navigation.uri}`;
          });
          this.spaceNavigations = data || [];
        });
    },
    applicationIcon(iconClass) {
      const navigationIcon = iconClass || '';
      if (!navigationIcon?.length) {
        return 'fas fa-sticky-note';
      }
      if (navigationIcon.includes('uiIconAppSpaceHomePage')) {
        return 'fas fa-stream';
      } else if (navigationIcon.includes('uiIconAppMembersPortlet')) {
        return 'fas fa-users';
      } else if (navigationIcon.includes('uiIconAppTasksManagement') || navigationIcon.includes('uiIconApptasks')) {
        return 'fas fa-tasks';
      } else if (navigationIcon.includes('uiIconAppNotes') || navigationIcon.includes('uiIconAppnotes') ) {
        return 'fas fa-clipboard';
      } else if (navigationIcon.includes('uiIconAppSpaceWallet')) {
        return 'fas fa-wallet';
      } else if (navigationIcon.includes('uiIconAppSpaceSettingPortlet')) {
        return 'fas fa-cog';
      } else {
        return navigationIcon;
      } 
    },
    closeMenu() {
      this.$emit('close-menu');
    },
    url() {
      if (this.space && this.space.groupId) {
        const uriPart = this.space.groupId.replace(/\//g, ':');
        return `${eXo.env.portal.context}/g/${uriPart}/`;
      } else {
        return '#';
      }
    },
    leftNavigationActionEvent(clickedItem) {
      document.dispatchEvent(new CustomEvent('space-left-navigation-action', {detail: clickedItem} ));
    },
    selectHome() {
      this.$emit('selectHome');
      this.leftNavigationActionEvent('makeAsHomePage');
    },
    openDetails() {
      document.dispatchEvent(new CustomEvent('display-space-hosts', {detail: this.managersToDisplay} ));
    },
    refreshExtensions() {
      this.externalExtensions = [];
      this.$nextTick(() => {
        this.externalExtensions = extensionRegistry.loadExtensions('space-popup', 'space-popup-action') || [];
        this.$nextTick().then(() => this.externalExtensions.forEach(this.initExtensionAction));
      });
    },
    initExtensionAction(extension) {
      if (extension.enabled) {
        let container = this.$refs[extension.key];
        if (container && container.length > 0) {
          container = container[0];
          extension.init(container, this.space.prettyName);
        } else {
          // eslint-disable-next-line no-console
          console.error(
            `Error initialization of the ${extension.key} action component: empty container`
          );
        }
      }
    },
  },
};
</script>