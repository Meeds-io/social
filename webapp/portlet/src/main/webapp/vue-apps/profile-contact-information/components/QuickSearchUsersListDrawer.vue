<!--
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 -->

<template>
  <exo-drawer
    ref="quickSearchUsersListDrawer"
    allow-expand
    right
    @expand-updated="expanded = $event">
    <template slot="title">
      <span class="text-color ma-auto">
        {{ $t('profileContactInformation.quick.search.label', {0: propertyValue}) }}
      </span>
    </template>
    <template slot="content">
      <div
        v-if="!isSearching && !listUsers.length"
        class="mt-auto mb-auto pt-5 align-center">
        {{ $t('Search.noResults') }}
      </div>
      <div
        v-if="expanded"
        class="pt-2 quickSearchResultExpanded">
        <v-container class="pa-0">
          <v-row>
            <v-col
              v-for="user in listUsers"
              :key="user.id"
              :id="`peopleCardItem${user.id}`"
              cols="12"
              md="4"
              lg="3"
              xl="3"
              class="pa-0">
              <people-card
                :user="user"
                :profile-action-extensions="profileActionExtensions" />
            </v-col>
          </v-row>
        </v-container>
      </div>
      <div
        v-else
        class="pt-2 quickSearchResultExpanded quickSearchResultCollapsed">
        <people-card
          v-for="user in listUsers"
          :key="user.id"
          :id="`peopleCardItem${user.id}`"
          :user="user"
          :profile-action-extensions="profileActionExtensions"
          :compact-display="true" />
      </div>
    </template>
    <template
      v-if="hasMore"
      slot="footer">
      <div class="ma-auto d-flex width-full">
        <v-btn
          :loading="isSearching"
          class="btn btn-primary width-full"
          flat
          outlined
          @click="search">
          {{ $t('Search.button.loadMore') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {

  data() {
    return {
      users: [],
      pageSize: 9,
      limit: 0,
      offset: 0,
      fieldsToRetrieve: 'all,spacesCount,relationshipStatus,connectionsCount,binding',
      hasMore: false,
      profileActionExtensions: [],
      profileSetting: null,
      expanded: false,
      propertyValue: null,
      isSearching: false,
    };
  },
  computed: {
    listUsers() {
      return this.users;
    }
  },
  watch: {
    isSearching() {
      if (this.isSearching) {
        this.$refs.quickSearchUsersListDrawer.startLoading();
      } else {
        this.$refs.quickSearchUsersListDrawer.endLoading();
      }
    },
  },
  created() {
    this.profileActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
    this.$root.$on('open-quick-search-users-drawer', this.open);
    this.$root.$on('relationship-status-updated', this.updateRelationshipStatus);
  },
  methods: {
    updateRelationshipStatus(user, status) {
      const index = this.users.findIndex(obj => obj.id === user.id);
      if (index !== -1) {
        this.users[index].relationshipStatus = status;
      }
    },
    open(profileSetting, propertyValue) {
      this.profileSetting = profileSetting;
      this.propertyValue = propertyValue;
      this.users = [];
      this.search();
      this.$refs.quickSearchUsersListDrawer.open();
    },
    search() {
      this.isSearching = true;
      if (this.abortController) {
        this.abortController.abort();
      }
      this.abortController = new AbortController();
      this.offset = this.users.length || 0;
      this.limit = this.limit || this.pageSize;
      this.$userService.getUsersByAdvancedFilter(this.profileSetting, this.offset, this.limit + 1, this.fieldsToRetrieve,'all', this.abortController.signal).then(data => {
        this.users.push(...data.users);
        this.hasMore = data.users?.length > this.limit;
      }).finally(() => {
        this.abortController = null;
        this.isSearching = false;
      });
    }
  }
};
</script>
