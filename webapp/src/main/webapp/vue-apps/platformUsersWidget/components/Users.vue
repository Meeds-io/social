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
  <v-app>
    <widget-wrapper
      :title="$t('social.users.label')"
      extra-class="application-body">
      <template #action>
        <v-btn
          id="usersWidgetAddButton"
          :title="$t('social.users.button.addUser')"
          :aria-label="$t('social.users.button.addUser')"
          small
          icon
          @click="openCreation">
          <v-icon size="18">fa-plus</v-icon>
        </v-btn>
      </template>
      <div v-if="users.length" class="d-flex justify-start mt-2">
        <platform-avatars-see-all
          id="usersWidgetAvatarsButton"
          :users="users"
          :count="usersCount"
          :label="$t('social.users.button.seeUsers')"
          @open="openList" />
      </div>
    </widget-wrapper>
    <platform-users-drawer
      ref="usersDrawer"
      @add="openCreation" />
    <users-management-creation-drawer
      go-back
      @go-back="closeCreation"
      @closed="backToList" />
    <users-management-user-form-drawer />
    <users-management-user-membership-drawer />
  </v-app>
</template>
<script>
import {getUsers} from '../js/UsersWidgetService.js';

export default {
  data: () => ({
    users: [],
    usersCount: 0,
    followUpDrawer: false,
  }),
  created() {
    this.retrieveUsers();
    this.$root.$on('refreshUsers', this.retrieveUsers);
    this.$root.$on('openUserMemberships', this.openFollowUp);
    this.$root.$on('editUser', this.openFollowUp);
  },
  beforeDestroy() {
    this.$root.$off('refreshUsers', this.retrieveUsers);
    this.$root.$off('openUserMemberships', this.openFollowUp);
    this.$root.$off('editUser', this.openFollowUp);
  },
  methods: {
    retrieveUsers() {
      return getUsers('', 4)
        .then(data => {
          this.users = (data?.users || []).map(user => ({...user, userName: user.username}));
          this.usersCount = data?.size || this.users.length;
        })
        .catch(() => {
          this.users = [];
          this.usersCount = 0;
        });
    },
    openList() {
      this.$refs.usersDrawer.open();
    },
    // A creation outcome that opens the memberships or the profile drawer
    // completes the account there: the list then stays closed
    openFollowUp() {
      this.followUpDrawer = true;
    },
    backToList() {
      if (this.followUpDrawer) {
        this.followUpDrawer = false;
      } else {
        this.openList();
      }
    },
    // The creation form opens over the page, the list opening when it closes
    openCreation() {
      this.$root.$emit('addNewUser');
    },
    closeCreation() {
      this.$root.$emit('closeNewUser');
    },
  },
};
</script>
