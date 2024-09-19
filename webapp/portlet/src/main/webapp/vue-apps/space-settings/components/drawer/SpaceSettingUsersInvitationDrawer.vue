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
    :loading="savingSpace"
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
        :disabled="savingSpace || spaceSaved"
        @keypress="checkExternalInvitation">
        <exo-identity-suggester
          v-if="!resetInput"
          ref="autoFocusInput3"
          v-model="selectedUser"
          :labels="suggesterLabels"
          :disabled="savingSpace || spaceSaved"
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
      <v-list class="mx-4 rounded externalList" subheader>
        <v-list-item
          v-for="user in externalInvitedUsers"
          :key="user">
          <v-badge
            bottom
            color="white"
            bordered
            offset-x="33"
            offset-y="26"
            class="externalBadge pa-0">
            <span slot="badge">
              <v-icon class="helpIcon">fa-help</v-icon>
            </span>
            <v-list-item-avatar class="ms-0">
              <v-img
                :src="defaultAvatar" />
            </v-list-item-avatar>
          </v-badge>
          <v-list-item-content>
            <v-list-item-title class="externalUserEmail" v-text="user" />
            <v-list-item-subtitle class="subEmail">{{ $t('peopleList.label.pending') }}</v-list-item-subtitle>
          </v-list-item-content>
          <v-btn
            :title="$t('peopleList.label.clickToDecline')"
            icon
            @click="removeExternalInvitation(user)">
            <v-icon>fa-times</v-icon>
          </v-btn>
        </v-list-item>
      </v-list>
      <v-list
        v-if="invitedMembers.length"
        class="mx-4 mt-0 rounded externalList"
        subheader>
        <v-list-item
          v-for="invitation in invitedMembers"
          :key="invitation">
          <v-list-item-content>
            <user-avatar
              v-if="invitation?.providerId === 'organization'"
              :username="invitation?.remoteId" />
            <space-avatar
              v-else-if="invitation?.providerId === 'space'"
              :space-pretty-name="invitation?.remoteId"
              class="spaceAvatar" />
          </v-list-item-content>
          <v-btn
            :title="$t('peopleList.label.clickToDecline')"
            icon
            @click="removeInvitation(invitation)">
            <v-icon>fa-times</v-icon>
          </v-btn>
        </v-list-item>
        <v-list-item
          v-for="invitation in externalInvitedUsers"
          :key="invitation">
          <v-badge
            bottom
            bordered
            color="white"
            offset-x="33"
            offset-y="26"
            class="externalBadge pa-0">
            <span slot="badge"><i class="uiIconSocUserProfile"></i><v-icon color="white" class="helpIcon">mdi-help</v-icon></span>
            <v-list-item-avatar class="ms-0">
              <v-img
                :src="defaultAvatar" />
            </v-list-item-avatar>
          </v-badge>
          <v-list-item-content>
            <v-list-item-title class="externalUserEmail" v-text="invitation.userEmail" />
            <v-list-item-subtitle v-if="!invitation.expired" class="subEmail">{{ $t('peopleList.label.invitationSent') }}</v-list-item-subtitle>
            <v-list-item-subtitle
              :title="$t('peopleList.label.invitationExpiredToolTip')"
              v-else
              class="subExpired">
              {{ $t('peopleList.label.invitationExpired') }}
            </v-list-item-subtitle>
          </v-list-item-content>
          <v-btn
            :title="$t('peopleList.label.clickToDecline')"
            icon
            @click="removeInvitation(invitation)">
            <v-icon>fa-times</v-icon>
          </v-btn>
        </v-list-item>
      </v-list>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :disabled="savingSpace || spaceSaved"
          class="btn me-2"
          @click="cancel">
          {{ $t('peopleList.label.cancel') }}
        </v-btn>
        <v-btn
          :loading="savingSpace"
          :disabled="saveButtonDisabled"
          class="btn btn-primary"
          @click="inviteUsers">
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
    savingSpace: false,
    spaceSaved: false,
    resetInput: false,
    spacePrettyName: eXo.env.portal.spaceName,
    selectedUser: null,
    invitedMembers: [],
    includeExternalUser: false,
    externalInvitedUsers: [],
    externalInvitationsSent: [],
  }),
  computed: {
    saveButtonDisabled() {
      return this.savingSpace || this.spaceSaved || !this.invitedMembers || !this.invitedMembers.length && !this.includeExternalUser;
    },
    suggesterLabels() {
      return {
        placeholder: this.$t('peopleList.label.inviteMembers'),
        noDataLabel: this.$t('SpaceSettings.invitationHelpTooltip'),
      };
    },
    defaultAvatar() {
      return `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/default-image/avatar`;
    },
    invitedUser() {
      return this.invitedMembers.length === 1 && this.invitedMembers[0] && this.invitedMembers[0].profile;
    },
    invitedUserFullName() {
      return this.invitedUser && this.invitedUser.fullName || this.$t('peopleList.label.users');
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
    open() {
      this.savingSpace = false;
      this.spaceSaved = false;
      this.includeExternalUser = false;
      this.spacePrettyName = eXo.env.portal.spaceName;
      this.selectedUser = null;
      this.invitedMembers = [];
      this.externalInvitedUsers = [];
      this.$refs.spaceInvitationDrawer.open();
      this.$spaceService.findSpaceExternalInvitationsBySpaceId(eXo.env.portal.spaceId)
        .then(invitations => {
          this.externalInvitationsSent = invitations;
        });
    },
    cancel() {
      this.$refs.spaceInvitationDrawer.close();
    },
    inviteUsers(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }

      if (this.spaceSaved || this.savingSpace) {
        return;
      }
      this.savingSpace = true;
      this.$spaceService.updateSpace({
        id: eXo.env.portal.spaceId,
        invitedMembers: this.invitedMembers,
        externalInvitedUsers: this.externalInvitedUsers
      })
        .then(() => {
          this.spaceSaved = true;
          this.$root.$emit('space-settings-invitations-sent', this.externalInvitedUsers.slice());
          this.$root.$emit('alert-message', this.$t('peopleList.label.successfulInvitation', {0: this.invitedUserFullName}), 'success');
          this.$refs.spaceInvitationDrawer.close();
        })
        .catch(e => {
          console.error('Error updating space ', this.invitedMembers, e);
          this.$root.$emit('alert-message', this.$t('peopleList.error.errorWhenSavingSpace'), 'error');
        })
        .finally(() => this.savingSpace = false);
    },
    checkExternalInvitationInput() {
      this.getExternalEmail();
    },
    checkExternalInvitation(event) {
      if (event.key === 'Enter') {
        event.preventDefault();
        this.getExternalEmail();
      } else if (Number(event.keyCode) === 32) {
        this.getExternalEmail();
      }
    },
    getExternalEmail() {
      const reg = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,24}))$/;
      const input = this.$refs.autoFocusInput3.searchTerm;
      const emails = input ? input.split(/[, ]/g) : [];
      emails.forEach(email => {
        if (reg.test(email)) {
          this.$userService.getUserByEmail(email)
            .then(user => {
              if (user.id !== 'null') {
                this.$spaceService.isSpaceMember(eXo.env.portal.spaceId, user.remoteId).then(data => {
                  if (data.isMember === 'true') {
                    this.$root.$emit('alert-message-html', `<span style="font-style: italic;">${email}</span> ${this.$t('peopleList.label.alreadyMember')}`, 'warning');
                  } else {
                    this.users.unshift(user);
                    const indexOfuser = this.invitedMembers.findIndex(u => u.remoteId === user.remoteId);
                    if (indexOfuser === -1) {
                      setTimeout(() => this.invitedMembers.unshift(user), 0);
                    }
                  }
                });
                this.resetSuggester();
              } else {
                this.includeExternalUser = true;
                const user = this.externalInvitationsSent.find(invited => invited.userEmail === email);
                if (user) {
                  this.$refs.autoFocusInput3?.$el?.querySelector?.('input')?.blur?.();
                  this.$root.$emit('alert-message-html', this.$t('peopleList.label.alreadyInvited'), 'warning');
                  this.resetSuggester();
                } else if (this.externalInvitedUsers.indexOf(email) === -1) {
                  this.externalInvitedUsers.unshift(email);
                  this.resetSuggester();
                }
              }
            });
        }
      });
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
