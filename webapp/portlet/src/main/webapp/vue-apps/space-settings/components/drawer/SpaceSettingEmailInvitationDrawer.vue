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
    id="spaceEmailInvitationDrawer"
    ref="spaceInvitationDrawer"
    :loading="saving"
    :go-back-button="goBackButton"
    :right="!$vuetify.rtl"
    allow-expand>
    <template slot="title">
      {{ $t('peopleList.title.usersToInvite') }}
    </template>
    <template slot="content">
      <div class="pa-4">
        <v-card
          ref="emailInput"
          :placeholder="$t('SpaceSettings.invitation.emailPlaceholder')"
          contenteditable="true"
          min-height="40px"
          class="input-placeholder pa-2 full-width text-wap v-card v-card--flat v-sheet theme--light border-color border-radius"
          flat
          @keyup.enter.prevent.stop="addEmails" />
      </div>
      <v-list
        v-if="invitedMembers?.length || emailInvitations?.length"
        class="mx-4 mt-0 rounded externalList"
        subheader>
        <space-setting-invitation-list-item
          v-for="(u, index) in emailInvitations"
          :key="u.userEmail"
          :invitation="u"
          email-only
          @remove="emailInvitations.splice(index, 1)" />
        <space-setting-roles-list-item
          v-for="(u, index) in invitedMembers"
          :key="u.id"
          :user="u"
          email-subtitle
          @remove="invitedMembers.splice(index, 1)" />
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
          :disabled="disabled"
          class="btn btn-primary"
          @click.prevent.stop="inviteUsers">
          {{ $t('peopleList.button.inviteUsers') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
const emailRegex = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}])|(([a-zA-Z\-\d]+\.)+[a-zA-Z]{2,24}))$/; // NOSONAR
export default {
  data: () => ({
    emailInvitations: [],
    invitedMembers: [],
    saving: false,
    goBackButton: false,
  }),
  computed: {
    disabled() {
      return !this.emails.length && !this.invitedMembers.length;
    },
    suggesterLabels() {
      return {
        placeholder: this.$t('peopleList.label.inviteMembers'),
        noDataLabel: this.$t('SpaceSettings.invitationHelpTooltip'),
      };
    },
    emails() {
      return this.emailInvitations.filter(u => u.status === 'pending')
        .map(u => u.userEmail);
    },
  },
  created() {
    this.$root.$on('space-settings-invite-email', this.open);
  },
  beforeDestroy() {
    this.$root.$on('space-settings-invite-email', this.open);
  },
  methods: {
    open(goBackButton) {
      this.saving = false;
      this.goBackButton = goBackButton;
      this.emailInvitations = [];
      this.$refs.spaceInvitationDrawer.open();
    },
    async inviteUsers() {
      this.saving = true;
      try {
        await this.$spaceService.updateSpace({
          id: eXo.env.portal.spaceId,
          invitedMembers: this.invitedMembers,
          externalInvitedUsers: this.emails,
        });
        this.$root.$emit('alert-message', this.$t('peopleList.label.successfulInvitation'), 'success');
        this.$root.$emit('space-settings-pending-updated');
        this.$refs.spaceInvitationDrawer.close();
      } catch (e) {
        this.$root.$emit('alert-message', this.$t('peopleList.error.errorWhensaving'), 'error');
      } finally {
        this.saving = false;
      }
    },
    cancel() {
      this.$refs.spaceInvitationDrawer.close();
    },
    addEmails() {
      const input = this.$refs.emailInput?.$el.innerText?.trim();
      if (input?.length) {
        const emails = input.split(/[, ]/g).map(s => s.trim());
        emails.forEach(this.addEmail);
        this.$refs.emailInput.$el.innerText = '';
      }
    },
    async addEmail(email) {
      if (!email?.length) {
        return;
      }
      if (!emailRegex.test(email)) {
        this.emailInvitations.unshift({
          userEmail: email,
          status: 'invalidEmail',
        });
        return;
      }
      if (this.emailInvitations.find(em => em.userEmail.toLowerCase() === email.toLowerCase())) {
        this.emailInvitations.unshift({
          userEmail: email,
          status: 'alreadyAddedInList',
        });
        return;
      }
      const user = await this.$userService.getUserByEmail(email);
      if (user?.id && user?.id !== 'null') {
        const data = await this.$spaceService.isSpaceMember(eXo.env.portal.spaceId, user.remoteId);
        if (data.isMember === 'true') {
          this.emailInvitations.unshift({
            userEmail: email,
            status: 'alreadySpaceMember',
          });
        } else if (!this.invitedMembers.find(u => u.remoteId === user.remoteId)) {
          user.email = email;
          this.invitedMembers.unshift(user);
        }
      } else if (this.$root.externalInvitations.find(invitation => !invitation.expired && invitation.userEmail.toLowerCase() === email.toLowerCase())) {
        this.emailInvitations.unshift({
          userEmail: email,
          status: 'alreadyInvited',
        });
      } else {
        this.emailInvitations.unshift({
          userEmail: email,
          status: 'pending',
        });
      }
    },
  },
};
</script>
