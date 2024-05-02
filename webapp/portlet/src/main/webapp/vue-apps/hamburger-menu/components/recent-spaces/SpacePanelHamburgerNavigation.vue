<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

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
      <v-list-item-icon
        v-if="!displaySequentially"
        class="backToMenu my-5 mx-2 icon-default-color justify-center"
        @click="$emit('close')">
        <v-icon
          v-if="$root.ltr"
          class="fas fa-arrow-left"
          small />
        <v-icon
          v-else
          class="fas fa-arrow-right"
          small />
      </v-list-item-icon>
      <v-list-item class="width-min-content text-truncate pt-3">
        <v-list-item-avatar
          class="spaceAvatar mt-0 mb-0 align-self-start"
          :width="avatarWidth"
          :height="avatarHeight">
          <v-img
            class="object-fit-cover"
            :src="avatar" />
        </v-list-item-avatar>
        <v-list-item-content class="pb-0 pt-0">
          <a :href="spaceURL" class="font-weight-bold text-truncate primary--text mb-2">{{ spaceDisplayName }}</a>
          <v-list-item-subtitle>
            {{ membersCount }} {{ $t('space.logo.banner.popover.members') }}
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
    </v-flex>
    <p class="text-truncate-4 text-caption text--primary font-weight-medium pt-3 px-4">
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
            :margin-left="managersToDisplay.length > 1 && 'ml-n5' || ''"
            :compact="managersToDisplay.length > 1"
            clickable="'false'"
            max="3"
            avatar-overlay-position
            @open-detail="openDetails()" />
        </v-list-item-action>
      </v-list-item>
      <v-divider />
    </v-flex>
    <v-flex>
      <v-list-item-action class="my-0 py-3 d-flex flex-row align-center justify-space-around me-0">
        <v-tooltip bottom>
          <template #activator="{ on, attrs }">
            <v-btn
              v-bind="attrs" 
              v-on="on" 
              link
              icon 
              @click="$root.$emit('change-home-link-space', space)">
              <v-icon 
                class="me-0 pa-2" 
                :class="isHomeLink && 'primary--text' || 'icon-default-color'" 
                small>
                fa-house-user
              </v-icon>
            </v-btn>
          </template>
          <span>
            {{ $t('menu.spaces.makeAsHomePage') }}
          </span>
        </v-tooltip>
        <v-tooltip bottom>
          <template #activator="{ on, attrs }">
            <v-btn
              :disabled="markAsReadDisabled"
              v-bind="attrs" 
              v-on="on" 
              icon
              @click="markAsAllRead">
              <v-icon class="me-0 pa-2" small>
                fa-envelope-open-text
              </v-icon>
            </v-btn>
          </template>
          <span>
            {{ $t('menu.spaces.markAsRead') }}
          </span>
        </v-tooltip>
        <space-mute-notification-button
          :space-id="spaceId"
          :muted="muted"
          origin="spaceLeftNavigationAction" />
        <exo-space-favorite-action
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
        <space-panel-hamburger-navigation-item
          v-for="navigation in spaceNavigations"
          :key="navigation.id"
          :navigation="navigation"
          :space-unread-items="spaceUnreadItems" />
      </v-list>
    </v-flex>
  </v-container>
</template>
<script>
export default {
  props: {
    space: {
      type: Object,
      default: null
    },
    homeLink: {
      type: String,
      default: null
    },
    displaySequentially: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    spaceNavigations: [],
    externalExtensions: [],
    spaceUnreadItems: null,
  }),
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
    muted() {
      return this.space?.isMuted === 'true';
    },
    isHomeLink() {
      return this.spaceURL === this.homeLink;
    },
    params() {
      return {
        identityType: 'space',
        identityId: this.spaceId
      };
    },
    enabledExtensionComponents() {
      return this.externalExtensions.filter(extension => extension.enabled);
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'sm' || this.$vuetify.breakpoint.name === 'xs';
    },
    spaceURL() {
      if (this.space && this.space.groupId) {
        const uriPart = this.space.groupId.replace(/\//g, ':');
        return `${eXo.env.portal.context}/g/${uriPart}/`;
      } else {
        return '#';
      }
    },
    avatarWidth() {
      return this.isMobile && '45' || '60';
    },
    avatarHeight() {
      return this.isMobile && '45' || '60';
    },
    hasUnreadItems() {
      return this.spaceUnreadItems && Object.values(this.spaceUnreadItems).reduce((sum, v) => sum += v, 0) > 0;
    },
    markAsReadDisabled() {
      return !this.hasUnreadItems;
    },
  },
  watch: {
    spaceId: {
      immediate: true,
      handler(newVal, oldVal) {
        if (newVal !== oldVal) {
          if (this.spaceId) {
            this.spaceNavigations = [];
            this.retrieveSpaceNavigations(this.spaceId)
              .then(() => this.refreshExtensions());
          }
        }
      },
    },
    space: {
      deep: true,
      immediate: true,
      handler: function() {
        this.spaceUnreadItems = this.space?.unread;
      },
    },
  },
  created() {
    document.addEventListener('space-unread-activities-updated', this.applySpaceUnreadChanges);
    this.retrieveSpaceNavigations(this.spaceId);
  },
  methods: {
    applySpaceUnreadChanges(event) {
      if (!event?.detail) {
        return;
      }
      const {spaceId, unread} = event.detail;
      if (this.spaceId === spaceId) {
        this.spaceUnreadItems = unread;
      }
    },
    retrieveSpaceNavigations(spaceId) {
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${spaceId}/navigations`, {
        method: 'GET',
        credentials: 'include',
      })
        .then(resp => resp && resp.ok && resp.json())
        .then(data => {
          data.forEach(navigation => {
            navigation.uri = `${this.spaceURL}${navigation.uri}`;
          });
          this.spaceNavigations = data || [];
        });
    },
    markAsAllRead() {
      this.$spaceService.markAllAsRead(this.spaceId);
    },
    closeMenu() {
      this.$emit('close-menu');
    },
    openDetails() {
      document.dispatchEvent(new CustomEvent('display-users-list-drawer', {detail: this.managersToDisplay} ));
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
