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
    class="pa-2 space-popover card-border-radius"
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
        <p
          v-if="spaceDescription"
          v-sanitized-html="spaceDescription"
          class="text-truncate-2 text-caption text--primary font-weight-medium mt-2"></p>
      </v-list-item-content>
    </v-list-item>
    <div class="d-flex justify-end">
      <space-mute-notification-button
        :space-id="spaceId"
        :muted="isSpaceMuted"
        origin="spacePopoverAction" />
      <space-favorite-action
        v-if="isSpaceMember"
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
  data: () => ({
    retrievedSpace: null,
    externalExtensions: [],
  }),
  computed: {
    enabledExtensionComponents() {
      return this.externalExtensions.filter(extension => extension.enabled);
    },
    spaceId() {
      return this.space?.id;
    },
    spacePrettyName() {
      return this.space?.prettyName;
    },
    spaceMembersCount() {
      return this.space?.membersCount || this.retrievedSpace?.membersCount;
    },
    spaceDescription() {
      return this.space?.description || this.retrievedSpace?.description;
    },
    isSpaceMember() {
      return this.space?.isMember || this.retrievedSpace?.isMember;
    },
    isSpaceMuted() {
      return this.space?.isMuted === 'true' || this.retrievedSpace?.isMuted === 'true';
    },
    canRedactOnSpace() {
      return this.retrievedSpace ? this.retrievedSpace?.canRedactOnSpace : this.space?.canRedactOnSpace;
    },
    params() {
      return {
        identityType: 'space',
        identityId: this.spaceId,
        spacePrettyName: this.spacePrettyName,
        canRedactOnSpace: this.canRedactOnSpace,
      };
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
    this.init();
  },
  methods: {
    async init() {
      if (this.spaceId && !Object.hasOwn(this.space, 'membersCount')) {
        this.retrievedSpace = await this.$spaceService.getSpaceById(this.spaceId, 'favorite');
      }
    },
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
