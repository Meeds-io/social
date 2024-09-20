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
  <exo-drawer
    id="spaceInvitationDrawer"
    ref="drawer"
    v-model="drawer"
    :loading="saving"
    :go-back-button="goBackButton"
    right>
    <template #title>
      {{ $t('peopleList.title.usersToInvite') }}
    </template>
    <template #content>
      <exo-identity-suggester
        v-if="!resetInput"
        v-model="selectedUser"
        :labels="suggesterLabels"
        :disabled="saving"
        :search-options="{
          spaceURL: spacePrettyName,
        }"
        :items="users"
        name="inviteMembers"
        type-of-relations="user_to_invite"
        class="ma-4"
        include-users
        include-spaces />
      <v-list
        v-if="invitedMembers?.length"
        class="mx-4 mt-0 rounded">
        <space-setting-roles-list-item
          v-for="(u, index) in invitedMembers"
          :key="u.id"
          :user="u"
          @remove="invitedMembers.splice(index, 1)" />
      </v-list>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :disabled="saving"
          class="btn me-2"
          @click="cancel">
          {{ $t('peopleList.label.cancel') }}
        </v-btn>
        <v-btn
          :loading="saving"
          :disabled="saveButtonDisabled"
          class="btn btn-primary"
          @click.prevent.stop="inviteUsers">
          {{ $t('peopleList.button.inviteUsers') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    users: [],
    drawer: false,
    saving: false,
    resetInput: false,
    goBackButton: false,
    spacePrettyName: eXo.env.portal.spaceName,
    selectedUser: null,
    invitedMembers: [],
  }),
  computed: {
    saveButtonDisabled() {
      return this.saving
        || !this.invitedMembers
        || !this.invitedMembers.length;
    },
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
        if (this.selectedUser && !this.invitedMembers.find(u => u.id === this.selectedUser.id)) {
          this.invitedMembers.unshift(this.selectedUser);
        }
        this.$nextTick().then(() => this.selectedUser = null);
      }
    },
  },
  created() {
    this.$root.$on('space-settings-invite-member', this.open);
  },
  beforeDestroy() {
    this.$root.$on('space-settings-invite-member', this.open);
  },
  methods: {
    open(goBackButton) {
      this.saving = false;
      this.goBackButton = goBackButton;
      this.spacePrettyName = eXo.env.portal.spaceName;
      this.selectedUser = null;
      this.invitedMembers = [];
      this.$refs.drawer.open();
    },
    inviteUsers() {
      this.saving = true;
      this.$spaceService.updateSpace({
        id: eXo.env.portal.spaceId,
        invitedMembers: this.invitedMembers,
      })
        .then(() => {
          this.$root.$emit('alert-message', this.$t('peopleList.label.successfulInvitation'), 'success');
          this.$root.$emit('space-settings-pending-updated');
          this.$refs.drawer.close();
        })
        .catch(() => this.$root.$emit('alert-message', this.$t('peopleList.error.errorWhensaving'), 'error'))
        .finally(() => this.saving = false);
    },
    cancel() {
      this.$refs.drawer.close();
    },
    resetSuggester() {
      this.resetInput = true;
      this.selectedUser = null;
      this.$nextTick().then(() => this.resetInput = false);
    },
  },
};
</script>
