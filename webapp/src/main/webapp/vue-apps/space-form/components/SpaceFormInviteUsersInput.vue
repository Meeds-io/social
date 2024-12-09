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
  <div>
    <exo-identity-suggester
      v-model="selectedUser"
      :labels="suggesterLabels"
      :search-options="{
        filterType: 'accessible',
      }"
      :items="users"
      name="inviteMembers"
      type-of-relations="user_to_invite"
      class="mb-4 mt-n3"
      include-users
      include-spaces
      autofocus />
    <v-list
      v-if="invitedMembers?.length"
      class="mx-4 mt-0 rounded">
      <space-form-invite-user-list-item
        v-for="(u, index) in invitedMembers"
        :key="u.id"
        :user="u"
        @remove="invitedMembers.splice(index, 1)" />
    </v-list>
  </div>
</template>
<script>
export default {
  props: {
    value: {
      type: Array,
      default: null,
    },
  },
  data: () => ({
    users: [],
    selectedUser: null,
    invitedMembers: [],
  }),
  computed: {
    suggesterLabels() {
      return {
        placeholder: this.$t('SpaceSettings.inviteMembers.placeholder'),
        noDataLabel: this.$t('SpaceSettings.inviteMembers.noResults'),
      };
    },
  },
  watch: {
    selectedUser() {
      if (this.selectedUser?.providerId) {
        if (this.selectedUser
            && !this.invitedMembers.find(u => u.id === this.selectedUser.id)) {
          this.invitedMembers.unshift(this.selectedUser);
        }
        this.$nextTick().then(() => this.selectedUser = null);
      }
    },
    invitedMembers() {
      this.$emit('input', this.invitedMembers);
    },
  },
  created() {
    this.invitedMembers = this.value?.slice?.() || [];
  },
};
</script>
