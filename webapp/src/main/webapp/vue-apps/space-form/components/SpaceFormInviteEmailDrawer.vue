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
    ref="drawer"
    v-model="drawer"
    :right="!$vuetify.rtl"
    go-back-button
    allow-expand>
    <template slot="title">
      {{ $t('spacesList.title.usersToInvite') }}
    </template>
    <template v-if="drawer" slot="content">
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
        <space-form-invite-email-list-item
          v-for="(u, index) in emailInvitations"
          :key="u.userEmail"
          :invitation="u"
          email-only
          @remove="removeEmailInvitation(index)" />
        <space-form-invite-user-list-item
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
          class="btn me-2"
          @click="close">
          {{ $t('spacesList.label.cancel') }}
        </v-btn>
        <v-btn
          class="btn btn-primary"
          @click.prevent.stop="apply">
          {{ $t('spacesList.button.add') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
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
    drawer: false,
    emailInvitations: [],
    invitedMembers: [],
    emailInput: '',
  }),
  computed: {
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
    this.$root.$on('space-form-invite-email', this.open);
  },
  beforeDestroy() {
    this.$root.$off('space-form-invite-email', this.open);
  },
  methods: {
    async open() {
      this.emailInvitations = this.value?.map?.(e => ({
        userEmail: e,
        status: 'pending',
      })) || [];
      this.invitedMembers = [];
      this.$refs.drawer.open();
      await this.$nextTick();
      window.setTimeout(() => {
        this.$refs?.emailInput?.$el?.focus();
      }, 500);
    },
    close() {
      this.$refs.drawer.close();
    },
    apply() {
      this.$emit('input', this.emails);
      if (this.invitedMembers?.length) {
        console.warn('this.invitedMembers', this.invitedMembers);
        this.$emit('update-members', this.invitedMembers);
      }
      this.close();
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
        const data = await this.$spaceService.isSpaceMember(this.$root.spaceId, user.remoteId);
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
        this.emailInvitations.unshift({
          userEmail: email,
          status: 'pending',
        });
      }
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
