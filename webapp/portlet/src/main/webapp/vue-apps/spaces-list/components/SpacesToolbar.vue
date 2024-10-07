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
    :right-text-filter="{
      minCharacters: 3,
      placeholder: $t('spacesList.label.filterSpaces'),
      tooltip: $t('spacesList.label.filterSpaces')
    }"
    :right-filter-button="{
      text: $t('spaceList.advanced.filter.button.title'),
      displayText: !$root.isMobile,
    }"
    :compact="compactDisplay || $root.isMobile"
    :filters-count="filtersCount"
    no-text-truncate
    class="px-1"
    @filter-text-input-end-typing="$emit('keyword-changed', $event)"
    @filter-button-click="$root.$emit('spaces-list-filter-open', filter)"
    @loading="$emit('loading', $event)">
    <template #left>
      <div class="d-flex align-center">
        <v-btn
          v-if="canCreateSpace" 
          id="addNewSpaceButton"
          :small="$root.isMobile"
          color="primary"
          elevation="0"
          @click="$root.$emit('addNewSpace')">
          <v-icon size="18" dark>fa-plus</v-icon>
          <span class="ms-2 hidden-xs-only">
            {{ $t('spacesList.button.add') }}
          </span>
        </v-btn>
        <space-pending-button
          v-if="$root.requestsCount"
          :count="$root.requestsCount"
          filter="requests"
          label-key="spacesList.label.pendingRequests"
          icon="fa-user-clock"
          badge-color="error-color-background" />
        <space-pending-button
          v-if="$root.invitationsCount"
          :count="$root.invitationsCount"
          filter="invited"
          label-key="spacesList.label.invitationsSent"
          icon="fa-history"
          badge-color="warning-color-background" />
        <space-pending-button
          v-if="$root.pendingCount"
          :count="$root.pendingCount"
          filter="pending"
          label-key="spacesList.label.usersRequests"
          icon="fa-spinner"
          icon-class="fa-rotate-270"
          badge-color="info-color-background" />
        <div
          v-if="filterMessage"
          class="text-subtitle showingSpaceText d-none d-sm-flex ms-3">
          {{ filterMessage }}
        </div>
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
      default: false
    },
    filterMessage: {
      type: String,
      default: null
    },
    canCreateSpace: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    loading: 0,
  }),
  created() {
    this.$root.$on('spaces-list-refresh', this.refresh);
    this.$root.$on('space-list-pending-updated', this.refresh);
    this.refresh();
  },
  beforeDestroy() {
    this.$root.$off('spaces-list-refresh', this.refresh);
    this.$root.$off('space-list-pending-updated', this.refresh);
  },
  methods: {
    refresh() {
      this.getSpacesInvitation();
      this.getSpacesPending();
      this.getSpacesRequest();
    },
    getSpacesInvitation() {
      this.loading++;
      this.$spaceService.getSpaces(null, null, null, 'invited')
        .then(data => this.$root.invitationsCount = data && data.size || 0)
        .finally(() => this.loading--);
    },
    getSpacesPending() {
      this.loading++;
      this.$spaceService.getSpaces(null, null, null, 'pending')
        .then(data => this.$root.pendingCount = data?.size || 0)
        .finally(() => this.loading--);
    },
    getSpacesRequest() {
      this.loading++;
      this.$spaceService.getSpaces(null, null, null, 'requests')
        .then(data => this.$root.requestsCount = data?.size || 0)
        .finally(() => this.loading--);
    },
  },
};
</script>
