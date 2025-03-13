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
  <v-container
    v-if="space"
    class="recentDrawer"
    flat>
    <v-flex class="d-flex pa-0">
      <v-list-item-icon
        v-if="!$root.displaySequentially"
        class="backToMenu my-5 mx-2 icon-default-color justify-center"
        @click="$emit('close')">
        <v-icon size="20">
          {{ $vuetify.rtl && 'fa-arrow-right' || 'fa-arrow-left' }}
        </v-icon>
      </v-list-item-icon>
      <v-list-item class="width-min-content pt-3">
        <v-list-item-avatar
          class="spaceAvatar mt-0 mb-0 align-self-start"
          :height="avatarHeight"
          :width="avatarWidth">
          <v-img
            class="object-fit-cover"
            :src="avatar" />
        </v-list-item-avatar>
        <v-list-item-content class="pb-0 pt-0">
          <a
            class="font-weight-bold text-truncate-2 primary--text mb-2"
            :href="spaceURL">{{ spaceDisplayName }}</a>
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
            avatar-overlay-position
            clickable="'false'"
            :compact="managersToDisplay.length > 1"
            :icon-size="30"
            :margin-left="managersToDisplay.length > 1 && 'ml-n5' || ''"
            max="3"
            :popover="false"
            :users="managersToDisplay"
            @open-detail="openDetails()" />
        </v-list-item-action>
      </v-list-item>
      <v-divider />
    </v-flex>
    <v-flex>
      <v-list-item-action
        id="HamburgerMenuSpaceLeftNavigationActions"
        class="my-0 pt-3 pb-0 d-flex flex-row align-center justify-end me-0">
        <v-tooltip bottom>
          <template #activator="{ on, attrs }">
            <v-btn
              v-bind="attrs"
              class="me-2"
              :disabled="markAsReadDisabled"
              icon
              v-on="on"
              @click="markAsAllRead">
              <v-icon
                class="me-0 pa-2"
                small>
                fa-envelope-open-text
              </v-icon>
            </v-btn>
          </template>
          <span>
            {{ $t('menu.spaces.markAsRead') }}
          </span>
        </v-tooltip>
        <space-favorite-action
          class="me-2"
          entity-type="spaces_left_navigation"
          :is-favorite="isFavorite"
          :space-id="spaceId"
          @added="space.isFavorite = 'true'"
          @removed="space.isFavorite = 'false'" />
        <extension-registry-components
          class="space-panel-action d-flex"
          element="div"
          element-class="me-2 ms-0"
          name="SpacePopover"
          :params="params"
          parent-element="div"
          type="space-popover-action" />
        <span
          v-for="extension in enabledExtensionComponents"
          :key="extension.key"
          :ref="extension.key"
          class="space-panel-action me-2"
          :class="`${extension.appClass} ${extension.typeClass}`"></span>
        <space-hamburger-action-menu
          class="me-2"
          :space="space" />
      </v-list-item-action>
    </v-flex>
    <v-flex>
      <v-list
        v-if="spaceNavigations"
        class="pa-0">
        <site-navigation-tree
          collapsed
          :navigations="spaceNavigations"
          :selected-name="selectedNavigationName"
          :site-name="spaceGroupId" />
      </v-list>
    </v-flex>
  </v-container>
</template>
<script>
  export default {
    props: {
      space: {
        type: Object,
        default: null,
      },
      homeLink: {
        type: String,
        default: null,
      },
    },
    data: () => ({
      spaceNavigations: null,
      externalExtensions: [],
      selectedNavigationName: null,
      loading: false,
    }),
    computed: {
      spaceId () {
        return this.space?.id;
      },
      spacePrettyName () {
        return this.space?.prettyName;
      },
      spaceDisplayName () {
        return this.space?.displayName;
      },
      avatar () {
        return this.space?.avatarUrl;
      },
      membersCount () {
        return this.space?.membersCount;
      },
      description () {
        return this.space?.description;
      },
      managersToDisplay () {
        return this.space?.managers;
      },
      isFavorite () {
        return this.space?.isFavorite;
      },
      muted () {
        return this.space?.isMuted === 'true';
      },
      isHomeLink () {
        return this.spaceURL === this.homeLink;
      },
      canRedactOnSpace () {
        return this.space?.canRedactOnSpace;
      },
      params () {
        return {
          identityType: 'space',
          identityId: this.spaceId,
          spacePrettyName: this.spacePrettyName,
          canRedactOnSpace: this.canRedactOnSpace,
        };
      },
      enabledExtensionComponents () {
        return this.externalExtensions.filter(extension => extension.enabled);
      },
      isMobile () {
        return eXo.vuetify.display.name.value === 'sm' || eXo.vuetify.display.name.value === 'xs';
      },
      spaceGroupId () {
        return this.space?.groupId;
      },
      spaceUri () {
        return this.spaceGroupId?.replace?.(/\//g, ':');
      },
      spaceURL () {
        if (this.space?.id) {
          return `${eXo.env.portal.context}/s/${this.space?.id}/`;
        } else {
          return '#';
        }
      },
      avatarWidth () {
        return this.isMobile && '45' || '60';
      },
      avatarHeight () {
        return this.isMobile && '45' || '60';
      },
      hasUnreadItems () {
        return this.$root?.unreadPerSpace?.[this.space?.id];
      },
      markAsReadDisabled () {
        return !this.hasUnreadItems;
      },
    },
    watch: {
      spaceId: {
        immediate: true,
        handler (newVal, oldVal) {
          if (newVal !== oldVal) {
            if (this.spaceId) {
              this.spaceNavigations = null;
              this.retrieveSpaceNavigations()
                .then(() => this.refreshExtensions());
            }
          }
        },
      },
    },
    created () {
      this.retrieveSpaceNavigations(this.spaceId);
      this.selectedNavigationName = eXo.env.portal.siteKeyName === this.spaceGroupId
        && eXo.env.portal.selectedNodeUri?.split?.('/')?.reverse?.()?.[0];
    },
    methods: {
      retrieveSpaceNavigations () {
        if (this.loading) {
          return;
        }
        this.loading = true;
        return eXo.$siteService.getSite('GROUP', this.spaceUri, {
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
      markAsAllRead () {
        eXo.$spaceService.markAllAsRead(this.spaceId);
      },
      closeMenu () {
        this.$emit('close-menu');
      },
      openDetails () {
        document.dispatchEvent(new CustomEvent('display-users-list-drawer', { detail: this.managersToDisplay } ));
      },
      refreshExtensions () {
        this.externalExtensions = [];
        this.$nextTick(() => {
          this.externalExtensions = extensionRegistry.loadExtensions('space-popup', 'space-popup-action') || [];
          this.$nextTick().then(() => this.externalExtensions.forEach(this.initExtensionAction));
        });
      },
      initExtensionAction (extension) {
        if (extension.enabled) {
          let container = this.$refs[extension.key];
          if (container && container.length > 0) {
            container = container[0];
            extension.init(container, this.space.prettyName);
          } else {
           
            console.error(
              `Error initialization of the ${extension.key} action component: empty container`
            );
          }
        }
      },
    },
  };
</script>
