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
  <v-app>
    <v-card
      outlined
      min-height="300"
      class="white border-radius pa-5 card-border-radius">
      <div
        v-if="isLoading"
        class="width-full d-flex full-height">
        <v-progress-circular
          class="ma-auto"
          color="primary"
          indeterminate />
      </div>
      <div v-else>
        <organizational-chart-header />
        <organizational-chart
          :user="user"
          :managed-users="managedUsersList"
          :profile-action-extensions="profileActionExtensions"
          :is-loading="isLoadingManagedUsers"
          :has-more="hasMore"
          @update-chart="updateChart"
          @load-more-managed-users="getManagedUsers" />
      </div>
    </v-card>
  </v-app>
</template>

<script>
export default {
  data() {
    return {
      user: null,
      managedUsers: [],
      fieldsToRetrieve: 'settings,manager,managedUsersCount',
      pageSize: 12,
      limit: 0,
      offset: 0,
      hasMore: false,
      isLoadingManagedUsers: false,
      isLoading: true,
      profileActionExtensions: [],
      userName: eXo?.env?.portal.userName,
      collator: new Intl.Collator(eXo.env.portal.language, {numeric: true, sensitivity: 'base'})
    };
  },
  computed: {
    managedUsersList() {
      return this.managedUsers?.filter(managedUser => managedUser.enabled)
        ?.sort((a, b) => this.usersNaturalComparator(a, b));
    }
  },
  beforeCreate() {
    this.isLoading = true;
    this.$userService.getUser(eXo?.env?.portal.userName, 'settings,manager,managedUsersCount').then(user => {
      this.user = user;
    });
  },
  created() {
    this.getManagedUsers();
    this.refreshExtensions();
    document.addEventListener('profile-extension-updated', this.refreshExtensions);
  },
  methods: {
    usersNaturalComparator(a, b) {
      return this.collator.compare(a.fullname, b.fullname);
    },
    updateChart(user) {
      this.isLoading = true;
      this.userName = user.username;
      this.managedUsers = [];
      this.getUser();
      this.getManagedUsers();
    },
    refreshExtensions() {
      this.profileActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
      this.profileActionExtensions.sort((elementOne, elementTwo) => (elementOne.order || 100) - (elementTwo.order || 100));
    },
    getUser() {
      return this.$userService.getUser(this.userName, this.fieldsToRetrieve).then(user => {
        this.user = user;
      });
    },
    getManagedUsers() {
      const profileSetting = {manager: this.userName};
      if (this.abortController) {
        this.abortController.abort();
      }
      this.abortController = new AbortController();
      this.offset = this.managedUsers.length || 0;
      this.limit = this.limit || this.pageSize;
      this.isLoadingManagedUsers = true;
      this.$userService.getUsersByAdvancedFilter(profileSetting, this.offset, this.limit + 1, 'settings,managedUsersCount', 'all', this.abortController.signal).then(data => {
        const users = data?.users?.slice(0, this.limit);
        this.managedUsers.push(...users);
        this.hasMore = data?.users?.length > this.limit;
      }).finally(() => {
        this.abortController = null;
        this.isLoadingManagedUsers = false;
        this.isLoading = false;
      });
    }
  }
};
</script>
