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
      <v-list-item class="width-min-content pt-3">
        <v-list-item-avatar
          class="spaceAvatar mt-0 mb-0 align-self-start"
          :width="avatarWidth"
          :height="avatarHeight">
          <v-img
            class="object-fit-cover"
            :src="avatar" />
        </v-list-item-avatar>
        <v-list-item-content class="pb-0 pt-0">
          <a :href="spaceURL" class="font-weight-bold text-truncate-2 primary--text mb-2">{{ spaceDisplayName }}</a>
          <v-list-item-subtitle>
            {{ membersCount }} {{ $t('space.logo.banner.popover.members') }}
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
    </v-flex>
    <p
      v-if="description?.length"
      v-sanitized-html="description"
      class="text-subtitle text-truncate-4 mb-0 pt-3 px-4"></p>
    <v-flex>
      <v-list-item>
        <v-list-item-content>
          <v-list-item-title>
            {{ $t('space.logo.banner.popover.managers') }}
          </v-list-item-title>
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
      <v-list-item-action
        id="HamburgerMenuSpaceLeftNavigationActions"
        class="my-0 py-3 d-flex flex-row align-center justify-space-around me-0">
        <v-tooltip bottom>
          <template #activator="{ on, attrs }">
            <v-btn
              :aria-label="$t('menu.spaces.makeAsHomePage')"
              role="button"
              link
              icon
              v-bind="attrs"
              v-on="on"
              @click="$root.$emit('change-home-link-space', space)">
              <v-icon 
                :class="isHomeLink && 'primary--text' || 'icon-default-color'" 
                class="me-0 pa-2" 
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
        <site-navigation-tree
          :navigations="spaceNavigations"
          :site-name="spaceGroupId"
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
    loading: false,
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
    spaceGroupId() {
      return this.space?.groupId;
    },
    spaceUri() {
      return this.spaceGroupId?.replace?.(/\//g, ':');
    },
    spaceURL() {
      if (this.spaceUri) {
        return `${eXo.env.portal.context}/g/${this.spaceUri}/`;
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
            this.retrieveSpaceNavigations()
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
    retrieveSpaceNavigations() {
      if (this.loading) {
        return;
      }
      this.loading = true;
      return this.$siteService.getSite('GROUP', this.spaceUri, {
        expandNavigations: true,
        excludeEmptyNavigationSites: true,
        lang: eXo.env.portal.language,
        visibility: ['displayed', 'temporal'],
        excludeGroupNodesWithoutPageChildNodes: true,
        temporalCheck: true,
      })
        .then(data => this.spaceNavigations = data?.siteNavigations || [])
        .finally(() => this.loading = false);
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
