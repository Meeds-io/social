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
    :go-back-button="goBackButton"
    :loading="saving"
    right>
    <template #title>
      {{ $t('peopleList.title.usersToInvite') }}
    </template>
    <template #content>
      <exo-identity-suggester
        v-if="!resetInput"
        v-model="selectedUser"
        class="ma-4"
        :disabled="saving"
        include-spaces
        include-users
        :items="users"
        :labels="suggesterLabels"
        name="inviteMembers"
        :search-options="{
          spaceURL: spacePrettyName,
          filterType: 'accessible',
        }"
        type-of-relations="user_to_invite" />
      <v-list
        v-if="invitedMembers?.length"
        class="mx-4 mt-0 rounded">
        <space-setting-role-list-item
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
          class="btn me-2"
          :disabled="saving"
          @click="cancel">
          {{ $t('peopleList.label.cancel') }}
        </v-btn>
        <v-btn
          class="btn btn-primary"
          :disabled="saveButtonDisabled"
          :loading="saving"
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
      saveButtonDisabled () {
        return this.saving
          || !this.invitedMembers
          || !this.invitedMembers.length;
      },
      suggesterLabels () {
        return {
          placeholder: this.$t('SpaceSettings.inviteMembers.placeholder'),
          noDataLabel: this.$t('SpaceSettings.inviteMembers.noResults'),
        };
      },
    },
    watch: {
      selectedUser () {
        if (this.selectedUser?.providerId) {
          if (this.selectedUser && !this.invitedMembers.find(u => u.id === this.selectedUser.id)) {
            this.invitedMembers.unshift(this.selectedUser);
          }
          this.$nextTick().then(() => this.selectedUser = null);
        }
      },
    },
    created () {
      this.$root.$on('space-settings-invite-member', this.open);
    },
    beforeUnmount () {
      this.$root.$off('space-settings-invite-member', this.open);
    },
    methods: {
      open (goBackButton) {
        this.saving = false;
        this.goBackButton = goBackButton;
        this.spacePrettyName = eXo.env.portal.spaceName;
        this.selectedUser = null;
        this.invitedMembers = [];
        this.$refs.drawer.open();
      },
      inviteUsers () {
        this.saving = true;
        this.$spaceService.updateSpace({
          id: this.$root.spaceId,
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
      cancel () {
        this.$refs.drawer.close();
      },
      resetSuggester () {
        this.resetInput = true;
        this.selectedUser = null;
        this.$nextTick().then(() => this.resetInput = false);
      },
    },
  };
</script>
