<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io

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
    elevation="2"
    class="pa-2 card-border-radius"
    max-width="250"
    min-width="250"
    id="identity-popover"
    @mouseenter="$root.$emit('popover-hovered')"
    @mouseleave="$root.$emit('popover-not-hovered')">
    <v-list-item class="px-2">
      <v-list-item-content class="py-0">
        <v-list-item-title>
          <exo-space-avatar
            :space="space"
            :size="45"
            bold-title
            link-style
            subtitle-new-line>
            <template slot="subTitle">
              <span v-if="spaceMembersCount" class="caption text-bold">
                {{ spaceMembersCount }} {{ $t('UIActivity.label.Members') }}
              </span>
            </template>
          </exo-space-avatar>
        </v-list-item-title>
        <p v-if="spaceDescription" class="text-truncate-2 text-caption text--primary font-weight-medium">
          {{ spaceDescription }}
        </p>
      </v-list-item-content>
    </v-list-item>
    <div class="d-flex justify-end">
      <space-mute-notification-button
        :space-id="spaceId"
        :muted="space?.isMuted === 'true'"
        origin="spacePopoverAction" />
      <exo-space-favorite-action
        v-if="favoriteActionEnabled"
        :key="space.id"
        :is-favorite="space.isFavorite"
        :space-id="space.id" />
      <extension-registry-components
        :params="params"
        class="d-flex"
        name="SpacePopover"
        type="space-popover-action"
        parent-element="div"
        element="div"
        element-class="mx-auto ma-lg-0" />
      <div
        v-for="extension in enabledExtensionComponents"
        :key="extension.key"
        :class="`${extension.appClass} ${extension.typeClass}`"
        :ref="extension.key">
      </div>
    </div>
  </v-card>
</template>

<script>

export default {
  props: {
    space: {
      type: Object,
      default: null,
    },
  },
  data() {
    return {
      externalExtensions: [],
    };
  },
  computed: {
    enabledExtensionComponents() {
      return this.externalExtensions.filter(extension => extension.enabled);
    },
    spaceId() {
      return this.space?.id;
    },
    spaceMembersCount() {
      return this.space?.membersCount;
    },
    spaceDescription() {
      return this.space?.description;
    },
    params() {
      return {
        identityType: 'space',
        identityId: this.spaceId,
      };
    },
    favoriteActionEnabled() {
      return this.space?.isMember;
    }
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
  methods: {
    refreshExtensions() {
      this.externalExtensions = [];
      this.$nextTick(() => {
        this.externalExtensions = extensionRegistry.loadExtensions('space-popup', 'space-popup-action') || [];
        this.$nextTick().then(() => this.externalExtensions.forEach(this.initExtensionAction));
      });
      // workaround for menu v-select absolute content that must be displayed inside the menu
      $('.profile-popover-menu').css('height', 'auto');
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
  }
};
</script>
