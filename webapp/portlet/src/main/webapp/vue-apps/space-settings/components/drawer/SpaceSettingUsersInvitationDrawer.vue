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
    ref="spaceInvitationDrawer"
    :loading="saving"
    :go-back-button="goBackButton"
    right
    class="spaceInvitationDrawer">
    <template slot="title">
      {{ $t('peopleList.title.usersToInvite') }}
    </template>
    <template slot="content">
      <form
        ref="form3"
        id="spaceUserInvitationForm"
        name="spaceUserInvitationForm"
        :disabled="saving"
        @keypress="checkExternalInvitation">
        <exo-identity-suggester
          v-if="!resetInput"
          ref="autoFocusInput3"
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
      </form>
      <v-list
        v-if="invitedMembers?.length || externalInvitedUsers?.length"
        class="mx-4 mt-0 rounded externalList"
        subheader>
        <space-setting-invitation-list-item
          v-for="u in externalInvitedUsers"
          :key="u.id"
          :invitation="u"
          email-only
          @remove="removeExternalInvitation(u)" />
        <space-setting-roles-list-item
          v-for="u in invitedMembers"
          :key="u.id"
          :user="u"
          @remove="removeInvitation(u)" />
      </v-list>
    </template>
    <template slot="footer">
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
const emailRegex = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}])|(([a-zA-Z\-\d]+\.)+[a-zA-Z]{2,24}))$/;
export default {
  data: () => ({
    users: [],
    saving: false,
    resetInput: false,
    goBackButton: false,
    spacePrettyName: eXo.env.portal.spaceName,
    selectedUser: null,
    invitedMembers: [],
    includeExternalUser: false,
    externalInvitedUsers: [],
    externalInvitationsSent: [],
  }),
  computed: {
    saveButtonDisabled() {
      return this.saving
        || !this.invitedMembers
        || !this.invitedMembers.length
        && !this.includeExternalUser;
    },
    suggesterLabels() {
      return {
        placeholder: this.$t('peopleList.label.inviteMembers'),
        noDataLabel: this.$t('SpaceSettings.invitationHelpTooltip'),
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
      this.includeExternalUser = false;
      this.spacePrettyName = eXo.env.portal.spaceName;
      this.selectedUser = null;
      this.invitedMembers = [];
      this.externalInvitedUsers = [];
      this.$refs.spaceInvitationDrawer.open();
      this.$spaceService.findSpaceExternalInvitationsBySpaceId(eXo.env.portal.spaceId)
        .then(invitations => this.externalInvitationsSent = invitations);
    },
    cancel() {
      this.$refs.spaceInvitationDrawer.close();
    },
    inviteUsers() {
      this.saving = true;
      this.$spaceService.updateSpace({
        id: eXo.env.portal.spaceId,
        invitedMembers: this.invitedMembers,
        externalInvitedUsers: this.externalInvitedUsers
      })
        .then(() => {
          this.$root.$emit('alert-message', this.$t('peopleList.label.successfulInvitation'), 'success');
          this.$root.$emit('space-settings-pending-updated');
          this.$refs.spaceInvitationDrawer.close();
        })
        .catch(() => this.$root.$emit('alert-message', this.$t('peopleList.error.errorWhensaving'), 'error'))
        .finally(() => this.saving = false);
    },
    checkExternalInvitationInput() {
      this.verifyExternalEmails();
    },
    checkExternalInvitation(event) {
      if (event.key === 'Enter') {
        event.preventDefault();
        this.verifyExternalEmails();
      } else if (Number(event.keyCode) === 32) {
        this.verifyExternalEmails();
      }
    },
    verifyExternalEmails() {
      const input = this.$refs.autoFocusInput3.searchTerm;
      const emails = input ? input.split(/[, ]/g) : [];
      emails.forEach(this.verifyExternalEmail);
    },
    async verifyExternalEmail(email) {
      if (!emailRegex.test(email) || this.externalInvitedUsers.find(em => em.toLowerCase() === email.toLowerCase())) {
        return;
      }
      const user = await this.$userService.getUserByEmail(email);
      if (user?.id && user?.id !== 'null') {
        const data = await this.$spaceService.isSpaceMember(eXo.env.portal.spaceId, user.remoteId);
        if (data.isMember === 'true') {
          this.$root.$emit('alert-message-html', `<span style="font-style: italic;">${email}</span> ${this.$t('peopleList.label.alreadyMember')}`, 'warning');
        } else if (!this.invitedMembers.find(u => u.remoteId === user.remoteId)) {
          this.invitedMembers.unshift(user, this.invitedMembers);
        }
        this.resetSuggester();
      } else {
        this.includeExternalUser = true;
        if (this.externalInvitationsSent.find(u => u.userEmail === email)) {
          this.$refs.autoFocusInput3?.$el?.querySelector?.('input')?.blur?.();
          this.$root.$emit('alert-message-html', this.$t('peopleList.label.alreadyInvited'), 'warning');
          this.resetSuggester();
        } else if (this.externalInvitedUsers.indexOf(email) === -1) {
          this.externalInvitedUsers.unshift(email);
          this.resetSuggester();
        }
      }
    },
    resetSuggester() {
      this.resetInput = true;
      this.selectedUser = null;
      this.$nextTick().then(() => this.resetInput = false);
    },
    removeExternalInvitation(user) {
      const index = this.externalInvitedUsers.indexOf(user);
      if (index > -1) {
        this.externalInvitedUsers.splice(index, 1);
      }
    },
    removeInvitation(invitation) {
      this.externalInvitationsSent.splice(this.externalInvitationsSent.indexOf(invitation),1);
      this.$spaceService.declineExternalInvitation(eXo.env.portal.spaceId, invitation.invitationId);
    },
  },
};
</script>
