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
  <div>
    <exo-drawer
      id="AdminsDrawer"
      ref="drawer"
      v-model="drawer"
      :loading="loading"
      allow-expand
      no-x-scroll
      right
      @expand-updated="expanded = $event">
      <template #title>
        {{ $t('social.admins.drawer.title') }}
      </template>
      <template v-if="drawer" #content>
        <div class="text-body pa-5"> {{ $t('social.admins.drawer.description') }} </div>
        <application-toolbar
          id="adminsToolbar"
          :right-text-filter="{
            minCharacters: 3,
            placeholder: $t('social.admins.drawer.filter.label'),
            tooltip: $t('social.admins.drawer.filter.label')
          }"
          compact
          no-text-truncate
          @filter-text-input-end-typing="keyword = $event"
          @loading="loading = $event">
          <template #left>
            <div class="d-flex">
              <v-btn
                id="AddAdminButton"
                :title="$t('social.admins.button.add')"
                color="primary"
                elevation="0"
                @click="$root.$emit('admins-add-drawer-open')">
                <v-icon
                  color="white"
                  class="me-2"
                  size="14">
                  fa-plus
                </v-icon>
                {{ $t('social.admins.button.add') }}
              </v-btn>
            </div>
          </template>
        </application-toolbar>
        <div v-if="filteredAdmins?.length">
          <v-list
            v-for="(admin, index) in filteredAdmins"
            :key="index">
            <admin-card :membership="admin" />
          </v-list>
        </div>
      </template>
      <template v-if="hasMore" #footer>
        <v-btn
          :loading="loading"
          block
          class="btn pa-0"
          @click="loadNextPage">
          {{ $t('social.admins.drawer.loadMore') }}
        </v-btn>
      </template>
    </exo-drawer>
    <add-admin-drawer />
  </div>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    expanded: false,
    totalSize: 0,
    admins: [],
    page: 1,
    itemsPerPage: 20,
    hasMore: false,
    keyword: null
  }),
  computed: {
    filteredAdmins() {
      if (!this.keyword) {
        return this.admins;
      } else {
        return this.admins.slice().filter(admin => {
          return admin.fullName?.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0;
        });
      }
    },
  },
  created() {
    this.searchAdmins();
    this.$root.$on('admins-drawer-open', this.open);
    this.$root.$on('platform-settings-admins-refresh', this.searchAdmins);
  },
  beforeDestroy() {
    this.$root.$off('admins-drawer-open', this.open);
    this.$root.$off('platform-settings-admins-refresh', this.searchAdmins);
  },
  methods: {
    open() {
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    searchAdmins() {
      const offset = (this.page - 1) * this.itemsPerPage;
      this.loading = true;
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/groups/memberships?groupId=/platform/administrators&offset=${offset}&limit=${this.itemsPerPage}&returnSize=true`, {
        method: 'GET',
        credentials: 'include',
      }).then(resp => {
        if (!resp || !resp.ok) {
          throw new Error(this.$t('IDMManagement.error.UnknownServerError'));
        } else {
          return resp.json();
        }
      }).then(data => {
        this.admins = data && data.entities || [];
        this.totalSize = data && data.size || 0;
        this.$root.$emit('platform-settings-admins-updated', this.totalSize, this.admins);
        this.hasMore = this.totalSize > this.admins.length;
      })
        .finally(() => this.loading = false);
    },
    loadNextPage() {
      this.page++;
      this.searchAdmins();
    },
  }
};
</script>
