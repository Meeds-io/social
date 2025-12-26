<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

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
    id="SpaceMembersDrawer"
    ref="drawer"
    v-model="drawer"
    :loading="loading"
    allow-expand
    no-x-scroll
    right
    @expand-updated="expanded = $event">
    <template #title>
      {{ $t('social.space.description.members.drawer') }}
    </template>
    <template v-if="drawer" #content>
      <application-toolbar
        id="peopleListToolbar"
        :right-text-filter="{
          minCharacters: 3,
          placeholder: $t('peopleList.label.filterPeople'),
          tooltip: $t('peopleList.label.filterPeople')
        }"
        compact
        no-text-truncate
        @filter-text-input-end-typing="keyword = $event"
        @loading="loading = $event">
        <template #left>
          <div class="d-flex">
            <space-invite-buttons-group
              class="px-2"
              go-back-button />
          </div>
        </template>
      </application-toolbar>
      <people-card-list
        ref="spaceMembers"
        :space-id="spaceId"
        :is-manager="isManager"
        :keyword="keyword"
        :people-count="peopleCount"
        :sm="expanded && 6 || 12"
        :md="expanded && 4 || 12"
        :lg="expanded && 3 || 12"
        :xl="expanded && 3 || 12"
        :compact-display="!expanded"
        :mobile-display="!expanded"
        filter="member"
        no-load-more-button
        no-margins
        class="px-1 my-2"
        @has-more="hasMore = $event"
        @loading="loading = $event"
        @members-count-updated="membersCountUpdated" />
    </template>
    <template v-if="hasMore" #footer>
      <v-btn
        :loading="loading"
        block
        class="btn pa-0"
        @click="loadNextPage">
        {{ $t('social.space.description.members.loadMore') }}
      </v-btn>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    keyword: null,
    initialized: false,
    loading: false,
    expanded: false,
    hasMore: false,
    space: null,
  }),
  computed: {
    spaceId() {
      return this.$root.spaceId;
    },
    peopleCount() {
      return this.$root.space?.membersCount || 0;
    },
    isManager() {
      return this.$root?.isManager;
    }
  },
  created() {
    this.$root.$on('space-settings-members-updated', this.refreshMembers);
    this.$root.$on('space-settings-pending-updated', this.refreshPending);
    this.$root.$on('space-members-drawer-open', this.open);

    document.addEventListener('space-members-drawer-open', this.openDrawer);
    document.addEventListener('space-members-drawer-close', this.close);
  },
  beforeDestroy() {
    this.$root.$off('space-settings-members-updated', this.refreshMembers);
    this.$root.$off('space-settings-pending-updated', this.refreshPending);
    this.$root.$off('space-members-drawer-open', this.open);

    document.removeEventListener('space-members-drawer-open', this.openDrawer);
    document.removeEventListener('space-members-drawer-close', this.close);
  },
  methods: {
    open() {
      if (this.isManager && !this.initialized) {
        document.dispatchEvent(new CustomEvent('space-member-management-actions-load'));
        this.initialized = true;
      }
      this.$refs.drawer.open();
    },
    openDrawer(event) {
      const settings = event.detail;
      this.initSettings(settings);
      this.open();
    },
    initSettings(settings) {
      if (!settings) {
        return;
      }
      this.$root.isManager = settings.isManager;
      this.$root.space = settings.space;
      this.$root.spaceId = settings.space.id;
      this.$root.isExternalFeatureEnabled ??= eXo?.env?.portal?.isExternalFeatureEnabled;
    },
    close() {
      this.$refs.drawer.close();
    },
    refreshMembers() {
      if (this.filter === 'member') {
        this.refreshUsers();
      }
    },
    refreshPending() {
      if (this.filter === 'invited' || this.filter === 'pending') {
        this.refreshUsers();
      }
    },
    refreshUsers() {
      this.$refs.spaceMembers.searchPeople();
    },
    loadNextPage() {
      this.$refs.spaceMembers.loadNextPage();
    }
  },
};
</script>