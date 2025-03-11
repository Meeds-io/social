<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

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
  <application-toolbar
    id="spacesListToolbar"
    class="px-1"
    :compact="compactDisplay || $root.isMobile"
    :filters-count="filtersCount"
    no-text-truncate
    :right-filter-button="displayRightFilter && {
      text: $t('spaceList.advanced.filter.button.title'),
      displayText: !$root.isMobile,
    }"
    :right-text-filter="{
      minCharacters: 3,
      placeholder: $t('spacesList.label.filterSpaces'),
      tooltip: $t('spacesList.label.filterSpaces')
    }"
    @filter-button-click="$root.$emit('spaces-list-filter-open', filter)"
    @filter-expand="filterExpand = $event"
    @filter-text-input-end-typing="$emit('keyword-changed', $event)"
    @loading="$emit('loading', $event)">
    <template #left>
      <div
        v-if="$root.title"
        class="text-header">
        {{ $root.title }}
      </div>
      <div
        v-else
        class="d-flex align-center"
        :class="!canCreateSpace && 'ms-n3'">
        <v-btn
          v-if="canCreateSpace"
          id="addNewSpaceButton"
          color="primary"
          elevation="0"
          :small="$root.isMobile"
          @click="$root.$emit('addNewSpace')">
          <v-icon
            dark
            size="18">
            fa-plus
          </v-icon>
          <span class="ms-2 hidden-xs-only">
            {{ $t('spacesList.button.add') }}
          </span>
        </v-btn>
        <space-pending-button
          v-if="$root.requestsCount"
          badge-color="error-color-background"
          :count="$root.requestsCount"
          filter="requests"
          icon="fa-user-clock"
          label-key="spacesList.label.pendingRequests" />
        <space-pending-button
          v-if="$root.invitationsCount"
          badge-color="warning-color-background"
          :count="$root.invitationsCount"
          filter="invited"
          icon="fa-history"
          label-key="spacesList.label.invitationsSent" />
        <space-pending-button
          v-if="$root.pendingCount"
          badge-color="info-color-background"
          :count="$root.pendingCount"
          filter="pending"
          icon="fa-spinner"
          icon-class="fa-rotate-270"
          label-key="spacesList.label.usersRequests" />
        <div
          v-if="filterMessage"
          class="text-subtitle showingSpaceText d-none d-sm-flex ms-3">
          {{ filterMessage }}
        </div>
      </div>
    </template>
    <template
      v-if="$root.canEdit && !filterExpand"
      #right>
      <div class="ms-auto">
        <spaces-public-access-warning />
        <v-btn
          v-if="$root.hover"
          id="spacesListSettingsButton"
          icon
          small
          @click="$root.$emit('spaces-list-settings-open')">
          <v-icon size="20">
            fa-cog
          </v-icon>
        </v-btn>
      </div>
    </template>
  </application-toolbar>
</template>
<script>
  export default {
    props: {
      filter: {
        type: String,
        default: null,
      },
      filtersCount: {
        type: Number,
        default: () => 0,
      },
      compactDisplay: {
        type: Boolean,
        default: false,
      },
      filterMessage: {
        type: String,
        default: null,
      },
      canCreateSpace: {
        type: Boolean,
        default: false,
      },
    },
    data: () => ({
      loading: 0,
      filterExpand: false,
    }),
    computed: {
      displayRightFilter () {
        return this.$root.sortBy !== 'lastVisited' && !this.$root.anonymous;
      },
    },
    created () {
      if (!this.$root.anonymous) {
        this.$root.$on('spaces-list-refresh', this.refresh);
        this.$root.$on('space-list-pending-updated', this.refresh);
      }
    },
    mounted () {
      if (!this.$root.anonymous) {
        this.refresh();
      }
    },
    beforeUnmount () {
      if (!this.$root.anonymous) {
        this.$root.$off('spaces-list-refresh', this.refresh);
        this.$root.$off('space-list-pending-updated', this.refresh);
      }
    },
    methods: {
      refresh () {
        this.getSpacesInvitation();
        this.getSpacesPending();
        this.getSpacesRequest();
      },
      getSpacesInvitation () {
        this.loading++;
        this.$spaceService.getSpacesByFilter({
          filter: 'invited',
        })
          .then(data => this.$root.invitationsCount = data && data.size || 0)
          .finally(() => this.loading--);
      },
      getSpacesPending () {
        this.loading++;
        this.$spaceService.getSpacesByFilter({
          filter: 'pending',
        })
          .then(data => this.$root.pendingCount = data?.size || 0)
          .finally(() => this.loading--);
      },
      getSpacesRequest () {
        this.loading++;
        this.$spaceService.getSpacesByFilter({
          filter: 'requests',
        })
          .then(data => this.$root.requestsCount = data?.size || 0)
          .finally(() => this.loading--);
      },
    },
  };
</script>
