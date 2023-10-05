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
  <exo-drawer
    ref="drawer"
    v-model="drawer"
    :loading="loading"
    class="userNotificationDrawer"
    right>
    <template #title>
      {{ $t('UserSettings.drawer.title.muteSpacesNotifications') }}
    </template>
    <template #content>
      <v-flex class="pa-4">
        <span class="subtitle-1">
          {{ $t('UserSettings.drawer.description.muteSpacesNotifications') }}
        </span>
        <exo-identity-suggester
          v-if="showSuggester"
          ref="spacesSuggester"
          v-model="space"
          :labels="spaceSuggesterLabels"
          :include-users="false"
          :ignore-items="ignoreItems"
          :width="220"
          name="spacesSuggester"
          class="user-suggester mt-n2"
          include-spaces />
        <span class="subtitle-1">
          {{ $t('UserSettings.drawer.label.mutedSpaces') }}
        </span>
        <v-hover
          v-for="space in mutedSpaces"
          :key="space.id"
          v-slot="{ hover }">
          <v-list-item
            :href="space.url"
            :key="space.id"
            class="pa-1 pb-1"
            dense>
            <v-list-item-avatar
              :size="avatarSize"
              class="me-2"
              tile>
              <v-img
                :src="space.avatarUrl"
                :height="avatarSize"
                :width="avatarSize"
                :max-height="avatarSize"
                :max-width="avatarSize"
                class="mx-auto spaceAvatar"
                role="presentation" />
            </v-list-item-avatar>
            <v-list-item-content class="pa-0">
              <v-list-item-title class="subtitle-2">
                {{ space.displayName }}
              </v-list-item-title>
              <v-list-item-subtitle v-if="space.description" class="caption text-truncate">
                {{ space.description }}
              </v-list-item-subtitle>
            </v-list-item-content>
            <v-list-item-action class="pa-0 my-auto">
              <v-tooltip
                v-if="isMobile || hover"
                :disabled="isMobile"
                bottom>
                <template #activator="{on, bind}">
                  <v-btn
                    icon
                    @click.stop.prevent="muteSpace(space, true)"
                    v-on="on"
                    v-bind="bind">
                    <v-icon class="icon-default-color" small>fa-bell-slash</v-icon>
                  </v-btn>
                </template>
                <span>{{ $t('UserSettings.button.tooltip.unmute') }}</span>
              </v-tooltip>
            </v-list-item-action>
          </v-list-item>
        </v-hover>
      </v-flex>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  props: {
    settings: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    drawer: false,
    avatarSize: 36,
    showSuggester: true,
    space: null,
    mutedSpaces: [],
    loading: false,
  }),
  computed: {
    spaceSuggesterLabels() {
      return {
        placeholder: this.$t('UserSettings.drawer.spaceSuggesterPlaceholder'),
        noDataLabel: this.$t('UserSettings.drawer.spaceSuggesterNoData'),
      };
    },
    ignoreItems() {
      return this.mutedSpaces.map(space => `space:${space.prettyName}`);
    },
    mutedSpaceIds() {
      return this.settings?.mutedSpaces || [];
    },
    isMobile() {
      return this.$vuetify.breakpoint.sm || this.$vuetify.breakpoint.xs;
    },
  },
  watch: {
    mutedSpaceIds() {
      if (this.drawer) {
        this.retrieveSpaces();
      }
    },
    space() {
      if (this.space) {
        this.muteSpace(this.space);
        this.space = null;
        this.resetSuggester();
      }
    },
  },
  methods: {
    resetSuggester() {
      this.showSuggester = false;
      this.$nextTick().then(() => this.showSuggester = true);
    },
    reset() {
      this.mutedSpaces = [];
      this.space = null;
      this.loading = false;
    },
    open() {
      this.reset();
      this.$emit('refresh');
      this.$refs.drawer.open();
    },
    retrieveSpaces() {
      this.loading = true;
      return Promise.all(this.mutedSpaceIds.map(id => this.$spaceService.getSpaceById(id)))
        .then(spaces => {
          const mutedSpaces = spaces || [];
          mutedSpaces.forEach(space => {
            space.url = `${eXo.env.portal.context}/g/${space.groupId.replace(/\//g, ':')}/`;
          });
          this.mutedSpaces = mutedSpaces;
        })
        .finally(() => this.loading = false);
    },
    muteSpace(space, unmute) {
      this.loading = true;
      const spaceId = space.spaceId || space.id;
      return this.$spaceService.muteSpace(spaceId, unmute)
        .then(() => {
          this.$emit('refresh');
          if (unmute) {
            document.dispatchEvent(new CustomEvent('space-unmuted', {detail: {
              name: 'userSettingsAction',
              spaceId,
            }}));
            this.$root.$emit('alert-message', this.$t('Notification.alert.successfullyUnmuted'), 'success');
          } else {
            document.dispatchEvent(new CustomEvent('space-muted', {detail: {
              name: 'userSettingsAction',
              spaceId,
            }}));
            this.$root.$emit('alert-message', this.$t('Notification.alert.successfullyMuted'), 'success');
          }
        })
        .catch(() => this.$root.$emit('alert-message', this.$t('Notification.alert.errorChangingSpaceMutingStatus'), 'error'))
        .finally(() => this.loading = false);
    },
  },
};
</script>

