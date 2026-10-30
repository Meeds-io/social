<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2026 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

-->
<template>
  <exo-drawer
    id="pendingUsersDrawer"
    ref="drawer"
    v-model="drawer"
    :loading="loading"
    allow-expand
    right>
    <template #title>{{ $t('UsersManagement.pending.title') }}</template>
    <template v-if="drawer" #content>
      <v-tabs
        v-model="tab"
        slider-size="4"
        class="flex-grow-0"
        grow>
        <v-tab tab-value="creation" href="#creation">
          {{ $t('UsersManagement.pending.tab.creation') }}
        </v-tab>
        <v-tab tab-value="invitations" href="#invitations">
          {{ $t('UsersManagement.pending.tab.invitations') }}
        </v-tab>
      </v-tabs>
      <v-list
        v-if="users.length"
        class="px-2"
        dense>
        <v-list-item
          v-for="user in users"
          :key="user.username"
          class="px-2">
          <v-list-item-avatar size="32" class="me-3">
            <v-img :src="user.avatar" eager />
          </v-list-item-avatar>
          <v-list-item-content>
            <v-list-item-title class="text-truncate">{{ user.fullname || user.username }}</v-list-item-title>
            <v-list-item-subtitle class="text-truncate">
              {{ user.email }}
              <!-- A viewer who is not an administrator of the configured
                   membership type reads rows without the enrollment date -->
              <template v-if="tab === 'invitations' && user.enrollmentDate">
                •
                <span v-if="user.enrollmentExpired" class="error--text">{{ $t('UsersManagement.pending.tokenExpired') }}</span>
                <span v-else>{{ $t('UsersManagement.pending.sentOn', {0: sentOn(user)}) }}</span>
              </template>
            </v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-action class="my-0">
            <v-btn
              :title="actionTitle"
              :aria-label="actionTitle"
              class="pendingUserActionButton"
              icon
              small
              @click="$root.$emit('openOnboardUser', toRow(user))">
              <v-icon size="16" class="icon-default-color">{{ tab === 'creation' ? 'fa-envelope' : 'fa-redo' }}</v-icon>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
      </v-list>
      <div v-else-if="!loading" class="d-flex justify-center text-sub-title pa-8">
        {{ tab === 'creation' ? $t('UsersManagement.pending.empty.creation') : $t('UsersManagement.pending.empty.invitations') }}
      </div>
    </template>
    <template v-if="hasMore" #footer>
      <div class="d-flex justify-center">
        <v-btn
          :loading="loading"
          :disabled="loading"
          class="btn"
          block
          @click="loadMore">
          {{ $t('UsersManagement.loadMore') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
import * as pendingUsersService from '../js/PendingUsersService.js';

/**
 * The accounts that never signed in, in two tabs: created and not invited
 * yet, invited and not connected yet. Loaded when opened, a page at a time
 */
export default {
  data: () => ({
    drawer: false,
    loading: false,
    tab: 'creation',
    users: [],
    size: 0,
    loadSequence: 0,
    pageSize: 20,
    lang: eXo.env.portal.language,
    dateFormat: {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    },
  }),
  computed: {
    hasMore() {
      return this.users.length < this.size;
    },
    actionTitle() {
      return this.tab === 'creation' ? this.$t('UsersManagement.pending.sendInvitation') : this.$t('UsersManagement.pending.sendAgain');
    },
  },
  watch: {
    tab(newTab, oldTab) {
      if (this.drawer && newTab !== oldTab) {
        this.reload();
      }
    },
  },
  created() {
    this.$root.$on('openPendingUsers', this.open);
    this.$root.$on('pending-users-changed', this.reloadWhenOpen);
  },
  beforeDestroy() {
    this.$root.$off('openPendingUsers', this.open);
    this.$root.$off('pending-users-changed', this.reloadWhenOpen);
  },
  methods: {
    // The component stays mounted after the drawer closes: each open starts
    // again from the tab asked, on its first page
    open(tab) {
      const requested = tab === 'invitations' ? 'invitations' : 'creation';
      const changed = this.tab !== requested;
      this.tab = requested;
      this.drawer = true;
      if (!changed) {
        // The tab watcher loads the list only when the tab changes
        this.reload();
      }
    },
    reloadWhenOpen() {
      if (this.drawer) {
        this.reload();
      }
    },
    reload() {
      this.users = [];
      this.size = 0;
      // A page still in flight belongs to the list being replaced
      this.loadSequence++;
      return this.load();
    },
    loadMore() {
      if (this.loading) {
        return;
      }
      return this.load();
    },
    load() {
      const sequence = this.loadSequence;
      const requestedAt = window.performance.now();
      this.loading = true;
      return pendingUsersService.getPendingUsers(this.tab, this.users.length, this.pageSize)
        .then(page => {
          // Only the answer to the current list is applied: a reload or a
          // tab switch since the request makes it stale
          if (sequence === this.loadSequence) {
            this.users = this.users.concat(page.users);
            this.size = page.size;
            // The reminder of this tab shows the same total, unless it holds
            // one requested later
            this.$root.$emit('pending-users-size', this.tab, page.size, requestedAt);
          }
        })
        .catch(() => sequence === this.loadSequence && this.$root.$emit('alert-message', this.$t('IDMManagement.error.UnknownServerError'), 'error'))
        .finally(() => {
          if (sequence === this.loadSequence) {
            this.loading = false;
          }
        });
    },
    sentOn(user) {
      return this.$dateUtil.formatDateObjectToDisplay(new Date(Number(user.enrollmentDate)), this.dateFormat, this.lang);
    },
    // The Onboard User drawer reads a row of the users table
    toRow(user) {
      return Object.assign({}, user, {
        userName: user.userName || user.username,
      });
    },
  },
};
</script>
