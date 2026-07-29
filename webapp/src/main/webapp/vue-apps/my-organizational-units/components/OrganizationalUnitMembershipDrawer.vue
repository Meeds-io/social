<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
    id="organizationalUnitMembershipDrawer"
    ref="drawer"
    v-model="drawer"
    :loading="loading"
    right>
    <template #title>
      <span class="text-truncate">
        {{ $t('organizationalUnitMembers.manageMembershipsOf', {0: userFullName}) }}
      </span>
    </template>
    <template #content>
      <div
        v-if="initialized && !memberships.length"
        class="d-flex align-center justify-center ma-5">
        <v-icon size="20" class="me-5">fa-users</v-icon>
        {{ $t('UsersManagement.noMemberships') }}
      </div>
      <v-list :loading="loading" class="px-4">
        <v-list-item
          v-for="membership in memberships"
          :key="membership.id"
          class="pa-1 pb-1"
          dense>
          <v-list-item-action class="pa-0 ma-0">
            <select
              v-model="membership.membershipType"
              aria-label="hidden"
              class="ignore-vuetify-classes width-auto pa-0 ma-0"
              @change="modified = true">
              <option
                v-for="role in rolesToDisplay"
                :key="role.name"
                :value="role.name">
                {{ role.label }}
              </option>
            </select>
          </v-list-item-action>
          <v-list-item-content class="d-flex align-center pa-0">
            <v-list-item-title class="d-flex align-center text-truncate">
              <div class="px-2">
                {{ $t('UsersManagement.of') }}
              </div>
              <v-icon size="28" class="me-2">fa-users</v-icon>
              <span class="text-truncate">
                {{ membership.groupLabel }}
              </span>
            </v-list-item-title>
          </v-list-item-content>
          <v-list-item-action class="pa-0 my-auto">
            <v-btn
              icon
              @click.stop.prevent="removeMembership(membership)">
              <v-icon color="error" small>fa-trash</v-icon>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
      </v-list>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :disabled="loading"
          class="btn me-2"
          @click="close">
          {{ $t('organizationalUnitMembers.cancel') }}
        </v-btn>
        <v-btn
          :disabled="!modified"
          :loading="saving"
          class="btn btn-primary"
          @click.prevent.stop="apply">
          {{ $t('organizationalUnitMembers.apply') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    groupId: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    drawer: false,
    loading: false,
    saving: false,
    modified: false,
    initialized: false,
    user: null,
    roles: [],
    memberships: [],
    originalMemberships: [],
  }),
  computed: {
    userName() {
      return this.user?.username || this.user?.userName;
    },
    userFullName() {
      return this.user?.fullname || this.userName;
    },
    rolesToDisplay() {
      return this.roles.map(role => ({
        name: role.name,
        label: this.$te(`UsersManagement.role.${role.name}`) ? this.$t(`UsersManagement.role.${role.name}`) : role.name,
      }));
    },
  },
  created() {
    this.$root.$on('open-organizational-unit-membership-drawer', this.open);
    this.retrieveRoles();
  },
  beforeDestroy() {
    this.$root.$off('open-organizational-unit-membership-drawer', this.open);
  },
  methods: {
    open(user) {
      this.user = user;
      this.memberships = [];
      this.originalMemberships = [];
      this.initialized = false;
      this.modified = false;
      this.retrieveMemberships();
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    removeMembership(membership) {
      this.memberships = this.memberships.filter(m => m.id !== membership.id);
      this.modified = true;
    },
    retrieveRoles() {
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/membershipTypes`, {
        method: 'GET',
        credentials: 'include',
      }).then(response => {
        if (!response?.ok) {
          throw new Error('Error retrieving membership types');
        }
        return response.json();
      }).then(roles => this.roles = roles || []);
    },
    retrieveMemberships() {
      this.loading = true;
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/users/${this.userName}/memberships?groupId=${encodeURIComponent(this.groupId)}&includeNestedGroups=true&returnSize=true`, {
        method: 'GET',
        credentials: 'include',
      }).then(response => {
        if (!response?.ok) {
          throw new Error('Error retrieving user memberships');
        }
        return response.json();
      }).then(memberships => {
        this.memberships = memberships?.entities || [];
        this.originalMemberships = JSON.parse(JSON.stringify(this.memberships));
      }).finally(() => {
        this.loading = false;
        this.initialized = true;
      });
    },
    async apply() {
      this.saving = true;
      try {
        const newMemberships = this.memberships.filter(m => !this.originalMemberships.find(om => om.groupId === m.groupId && om.membershipType === m.membershipType));
        if (newMemberships.length) {
          await Promise.all(newMemberships.map(this.createMembership));
        }
        const toDeleteMemberships = this.originalMemberships.filter(om => !this.memberships.find(m => m.groupId === om.groupId && m.membershipType === om.membershipType));
        if (toDeleteMemberships.length) {
          await Promise.all(toDeleteMemberships.map(this.deleteMembership));
        }
        this.close();
      } finally {
        this.saving = false;
        this.$root.$emit('refresh-organizational-unit-members');
      }
    },
    async createMembership(membership) {
      try {
        await this.$organizationalUnitMembersService.createMembership(this.userName, membership.groupId, membership.membershipType);
      } catch (error) {
        this.handleError(error);
      }
    },
    async deleteMembership(membership) {
      const membershipId = `${membership.membershipType}:${this.userName}:${membership.groupId}`;
      const response = await fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/groups/memberships?membershipId=${encodeURIComponent(membershipId)}`, {
        method: 'DELETE',
        credentials: 'include',
      });
      if (!response?.ok) {
        this.handleError(new Error(await response.text()));
      }
    },
    handleError(error) {
      const message = error?.message || '';
      if (message && !message.includes('Error')) {
        this.$root.$emit('alert-message', message, 'error');
      } else {
        this.$root.$emit('alert-message', this.$t('organizationalUnitMembers.errorSavingMemberships'), 'error');
      }
    },
  },
};
</script>
