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
    id="addAdminDrawer"
    ref="drawer"
    v-model="drawer"
    :loading="saving"
    go-back-button="true"
    right>
    <template #title>
      {{ $t('social.admins.drawer.adminsToAdd') }}
    </template>
    <template #content>
      <v-autocomplete
        id="userNameInput"
        ref="userNameInput"
        v-model="selectedUsers"
        :disabled="saving"
        :loading="loadingSuggestions > 0"
        :items="users"
        :search-input.sync="searchTerm"
        :placeholder="$t('social.admins.drawer.addAdmin.placeholder')"
        :required="!selectedUsers.length"
        :return-object="false"
        name="membershipUser"
        height="100"
        append-icon=""
        menu-props="closeOnClick, maxHeight = 100"
        class="identitySuggester mx-4"
        content-class="identitySuggesterContent"
        width="100%"
        max-width="100%"
        item-text="fullName"
        item-value="userName"
        persistent-hint
        hide-selected
        chips
        cache-items
        dense
        flat
        multiple
        @change="clearSearch"
        @update:search-input="searchTerm = $event">
        <template slot="no-data">
          <v-list-item class="pa-0">
            <v-list-item-title class="px-2">
              {{ $t('social.admins.drawer.searchUser') }}
            </v-list-item-title>
          </v-list-item>
        </template>
        <template slot="selection" slot-scope="{item, selected}">
          <v-chip
            :input-value="selected"
            class="identitySuggesterItem"
            close
            @click:close="removeMemberShip(item)">
            <span class="text-truncate">
              {{ item.fullName }}
            </span>
          </v-chip>
        </template>
        <template slot="item" slot-scope="{ item }">
          <v-list-item-title class="text-truncate identitySuggestionMenuItemText" v-text="item.fullName" />
        </template>
      </v-autocomplete>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :disabled="saving"
          class="btn me-2"
          @click="cancel">
          {{ $t('social.admins.button.cancel') }}
        </v-btn>
        <v-btn
          :loading="saving"
          :disabled="saveButtonDisabled"
          class="btn btn-primary"
          @click.prevent.stop="saveMembership">
          {{ $t('social.admins.button.add') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    saving: false,
    goBackButton: false,
    selectedUsers: [],
    memberships: [],
    searchTerm: null,
    previousSearchTerm: null,
    loadingSuggestions: 0,
    users: [],
    membership: {}
  }),
  computed: {
    saveButtonDisabled() {
      return this.saving || !this.memberships.length;
    },
  },
  watch: {
    selectedUsers() {
      this.selectedUsers.forEach(user => {
        if (!this.memberships.some(membership => membership.userName === user)) {
          this.memberships.push({
            groupId: '/platform/administrators',
            membershipType: '*',
            userName: user
          });
        }
      });
    },
    searchTerm(value) {
      if (value?.length) {
        this.$refs.userNameInput.isFocused = true;
        window.setTimeout(() => {
          if (this.previousSearchTerm === this.searchTerm) {
            this.users = [];

            this.loadingSuggestions++;
            this.$userService.getUsersByStatus(value, 0, 20, 'ENABLED')
              .then(data => {
                this.users = data && data.entities || [];
              })
              .finally(() => this.loadingSuggestions--);
          }
          this.previousSearchTerm = this.searchTerm;
        }, 400);
      } else {
        this.users = [];
      }
    },
  },
  created() {
    this.$root.$on('admins-add-drawer-open', this.open);
  },
  beforeDestroy() {
    this.$root.$off('admins-add-drawer-open', this.open);
  },
  methods: {
    open() {
      this.$refs.drawer.open();
    },
    cancel() {
      this.$refs.drawer.close();
    },
    clearSearch() {
      this.searchTerm = null;
    },
    removeMemberShip(user) {
      this.memberships.splice(this.memberships.findIndex(membership => membership.userName === user.userName), 1);
      this.selectedUsers.splice(this.selectedUsers.findIndex(userName => userName === user.userName), 1);
    },
    saveMembership(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      this.saving = true;

      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/groups/memberships/bulk?membershipId=${this.membership.id || ''}`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(this.memberships),
      }).then(resp => {
        if (!resp || !resp.ok) {
          if (resp.status === 400) {
            return resp.text().then(error => {
              this.fieldError = error;
              throw new Error(error);
            });
          } else {
            throw new Error(this.$t('IDMManagement.error.UnknownServerError'));
          }
        }
      }).then(() => this.$root.$emit('platform-settings-admins-refresh'))
        .then(() => this.$refs.drawer.close())
        .finally(() => {
          this.memberships = [];
          this.saving = false;
        });
    },
  },
};
</script>
