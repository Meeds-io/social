<!--

  This file is part of the Meeds project (https://meeds.io/).

  Copyright (C) 2026 Meeds Association contact@meeds.io

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
    id="UsersListDrawer"
    ref="drawer"
    v-model="drawer"
    :loading="loading"
    allow-expand
    no-x-scroll
    right>
    <template #title>
      {{ $t('social.users.drawer.title') }}
    </template>
    <template #titleIcons>
      <v-btn
        id="usersListManagementLink"
        :href="managementUrl"
        :title="$t('social.users.button.openManagement')"
        :aria-label="$t('social.users.button.openManagement')"
        icon>
        <v-icon
          aria-hidden="true"
          size="20">
          fa-external-link-alt
        </v-icon>
      </v-btn>
    </template>
    <template v-if="drawer" #content>
      <div class="d-flex align-center px-4 pt-4 pb-2">
        <template v-if="filtering">
          <v-btn
            id="usersListClearFilterButton"
            :title="$t('social.users.drawer.clearFilter')"
            :aria-label="$t('social.users.drawer.clearFilter')"
            class="me-2"
            icon
            @click="clearFilter">
            <v-icon size="16">{{ $vuetify.rtl && 'fa-arrow-right' || 'fa-arrow-left' }}</v-icon>
          </v-btn>
          <v-text-field
            id="usersListFilterInput"
            ref="filterInput"
            v-model="keyword"
            :placeholder="$t('social.users.drawer.filter.label')"
            :aria-label="$t('social.users.drawer.filter.label')"
            class="pa-0 ma-0"
            prepend-inner-icon="fa-filter"
            hide-details
            outlined
            dense />
        </template>
        <template v-else>
          <v-btn
            id="usersListAddButton"
            :title="$t('social.users.button.add')"
            color="primary"
            elevation="0"
            @click="$emit('add')">
            <v-icon
              color="white"
              class="me-2"
              size="14">
              fa-plus
            </v-icon>
            {{ $t('social.users.button.add') }}
          </v-btn>
          <v-spacer />
          <v-btn
            id="usersListFilterButton"
            :title="$t('social.users.drawer.filter.label')"
            :aria-label="$t('social.users.drawer.filter.label')"
            icon
            @click="openFilter">
            <v-icon size="18">fa-filter</v-icon>
          </v-btn>
        </template>
      </div>
      <v-list v-if="users.length" class="pt-0">
        <v-list-item
          v-for="user in users"
          :key="user.username"
          :href="profileUrl(user)"
          class="users-list-item">
          <v-list-item-avatar>
            <v-img
              :src="user.avatar"
              :alt="user.fullname"
              eager />
          </v-list-item-avatar>
          <v-list-item-content>
            <v-list-item-title>{{ user.fullname }}</v-list-item-title>
            <v-list-item-subtitle>{{ user.position }}</v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>
      </v-list>
    </template>
    <template v-if="hasMore" #footer>
      <v-btn
        :loading="loading"
        block
        class="btn pa-0"
        @click="loadNextPage">
        {{ $t('social.users.drawer.loadMore') }}
      </v-btn>
    </template>
  </exo-drawer>
</template>
<script>
import {getUsers} from '../js/UsersWidgetService.js';

export default {
  data: () => ({
    drawer: false,
    loading: false,
    filtering: false,
    keyword: '',
    users: [],
    limit: 20,
    hasMore: false,
    searchTimeout: null,
    abortController: null,
  }),
  computed: {
    managementUrl() {
      return `${eXo.env.portal.context}/administration/home/organisation/users`;
    },
  },
  watch: {
    keyword() {
      window.clearTimeout(this.searchTimeout);
      this.searchTimeout = window.setTimeout(() => this.search(), 300);
    },
  },
  methods: {
    profileUrl(user) {
      return `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile/${user.username}`;
    },
    open() {
      this.limit = 20;
      this.search();
      this.$refs.drawer.open();
    },
    openFilter() {
      this.filtering = true;
      this.$nextTick(() => this.$refs.filterInput?.focus?.());
    },
    clearFilter() {
      this.filtering = false;
      this.keyword = '';
      this.$nextTick(() => document.getElementById('usersListFilterButton')?.focus?.());
    },
    loadNextPage() {
      this.limit += 20;
      this.search();
    },
    // Filters on the user's name, which the users search matches
    search() {
      this.abortController?.abort?.();
      this.abortController = new AbortController();
      this.loading = true;
      return getUsers(this.keyword?.trim() || '', this.limit, this.abortController.signal)
        .then(data => {
          this.users = data?.users || [];
          this.hasMore = (data?.size || 0) > this.users.length;
        })
        .catch(e => {
          if (e?.name !== 'AbortError') {
            this.users = [];
            this.hasMore = false;
          }
        })
        .finally(() => this.loading = false);
    },
  },
};
</script>
