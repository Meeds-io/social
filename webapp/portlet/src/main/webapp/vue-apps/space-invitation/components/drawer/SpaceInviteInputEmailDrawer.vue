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
      <div class="pa-4 d-flex flex-column">
        <v-card
          ref="emailInput"
          :placeholder="$t('SpaceSettings.invitation.emailPlaceholder')"
          contenteditable="true"
          min-height="40px"
          class="input-placeholder pa-2 full-width text-wap v-card v-card--flat v-sheet theme--light border-color border-radius"
          flat
          @keydown.enter.prevent.stop="addEmails"
          @keyup="updateInput" />
        <div class="d-flex">
          <div class="flex-grow-1 flex-shrink-1 text-subtitle text-wrap my-auto me-2 py-1">
            {{ hintMessage }}
          </div>
          <div v-show="emailInput?.length" class="flex-grow-0 flex-shrink-0 me-n1">
            <v-btn
              :title="$t('SpaceSetting.invitation.addEmails')"
              :disabled="!emailInput"
              color="success"
              small
              icon
              tile
              @click="addEmails">
              <v-icon size="22">fa-check</v-icon>
            </v-btn>
            <v-btn
              :title="$t('SpaceSetting.invitation.clearEmails')"
              :disabled="!emailInput"
              color="error"
              small
              icon
              tile
              @click="resetInput">
              <v-icon size="22">fa-times</v-icon>
            </v-btn>
          </div>
        </div>
      </div>
      <v-list
        v-if="invitedMembers?.length || emailInvitations?.length"
        class="mx-4 mt-0 rounded externalList"
        subheader>
        <space-invite-email-list-item
          v-for="(u, index) in emailInvitations"
          :key="u.userEmail"
          :invitation="u"
          email-only
          @remove="removeEmailInvitation(index)" />
        <space-setting-role-list-item
          v-for="(u, index) in invitedMembers"
          :key="u.id"
          :user="u"
          email-subtitle
          @remove="removeMemberInvitation(index)" />
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
export default {
  data: () => ({
    emailInvitations: [],
    invitedMembers: [],
    saving: false,
    goBackButton: false,
    emailInput: '',
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
      return this.emailInvitations.filter(u => u.status === 'pending' || u.status === 'alreadyInvited')
        .map(u => u.userEmail);
    },
    alreadSentInvitations() {
      return this.emailInvitations.filter(u => u.invitationId);
    },
    hintMessage() {
      return this.emailInput?.length
        && !this.$root.isMobile
        && this.$t('SpaceSetting.invitation.hintToConfirm')
        || '';
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
      this.invitedMembers = [];
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
        this.cleanOldInvitations();
        this.$refs.spaceInvitationDrawer.close();
      } catch (e) {
        this.$root.$emit('alert-message', this.$t('peopleList.error.errorWhensaving'), 'error');
      } finally {
        this.saving = false;
      }
    },
    cleanOldInvitations() {
      if (this.alreadSentInvitations?.length) {
        Promise.all(this.alreadSentInvitations.slice()
          .map(i => this.$spaceService.declineExternalInvitation(this.$root.space.id, i.invitationId)))
          .finally(() => this.$root.$emit('space-settings-pending-updated'));
      }
    },
    addEmails() {
      if (this.emailInput?.length) {
        this.emailInput
          .trim()
          .split(/,/g)
          .map(s => s.trim())
          .forEach(this.addEmail);
        this.resetInput();
      }
    },
    async addEmail(email) {
      if (!email?.length) {
        return;
      }
      email = email.trim();
      if (!(/^[^\s@]{1,100}@[^\s@]{1,100}\.[^\s@]{1,10}$/g).test(email)) {
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
      } else {
        const existingInvitations = this.$root.externalInvitations.filter(invitation => invitation.userEmail.toLowerCase() === email.toLowerCase());
        if (existingInvitations?.length) {
          existingInvitations.forEach(i => {
            i.status = 'deprecatedInvitation';
            i.hidden = true;
            this.emailInvitations.unshift(i);
          });
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
      }
    },
    cancel() {
      this.$refs.spaceInvitationDrawer.close();
    },
    updateInput() {
      this.emailInput = this.$refs.emailInput?.$el.innerText?.trim();
    },
    resetInput() {
      this.$refs.emailInput.$el.innerText = '';
      this.emailInput = '';
    },
    async removeEmailInvitation(index) {
      const deletedEmail = this.emailInvitations.splice(index, 1)?.[0]?.userEmail;
      await this.$nextTick();
      if (!this.emails?.find(e => e?.toLowerCase?.() === deletedEmail?.toLowerCase?.()) // No valid emails with same entry
          && this.emailInvitations.find(i => i.userEmail?.toLowerCase?.() === deletedEmail?.toLowerCase?.())) { // has invalid email inputs
        // Delete all invalid email inputs
        this.emailInvitations = this.emailInvitations.filter(i => i.userEmail?.toLowerCase?.() !== deletedEmail?.toLowerCase?.());
      }
    },
    removeMemberInvitation(index) {
      this.invitedMembers.splice(index, 1);
    },
  },
};
</script>
