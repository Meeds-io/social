<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2026 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

-->
<template>
  <exo-drawer
    id="userOnboardDrawer"
    ref="drawer"
    v-model="drawer"
    allow-expand
    right
    @opened="reset">
    <template #title>{{ $t('UsersManagement.onboard.title') }}</template>
    <template v-if="drawer" #content>
      <users-management-onboarding-email-form
        :message.sync="message"
        :message-max-length="messageMaxLength"
        :validity-label="linkValidityLabel"
        :saving="sending"
        editor-id="userOnboardMessageEditor"
        class="ma-4"
        @validity-updated="messageValid = $event" />
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :disabled="sending"
          class="btn me-2"
          @click="close">
          {{ $t('UsersManagement.button.cancel') }}
        </v-btn>
        <v-btn
          :disabled="sending || !messageValid"
          :loading="sending"
          id="usersManagementOnboardInviteButton"
          class="btn btn-primary"
          @click="invite">
          {{ $t('UsersManagement.onboard.invite') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
import * as userCreationService from '../js/UserCreationService.js';
import UsersManagementOnboardingSettings from './onboarding/UsersManagementOnboardingSettings.js';

/**
 * Sends, or sends again, the onboarding email of an account that never
 * signed in, opened from its row or from the Pending Users drawer
 */
export default {
  mixins: [UsersManagementOnboardingSettings],
  data: () => ({
    drawer: false,
    sending: false,
    user: null,
  }),
  created() {
    this.$root.$on('openOnboardUser', this.open);
  },
  beforeDestroy() {
    this.$root.$off('openOnboardUser', this.open);
  },
  methods: {
    open(user) {
      this.user = user;
      this.drawer = true;
    },
    close() {
      this.drawer = false;
    },
    // The component stays mounted after the drawer closes: the message goes
    // back to the proposed one at each open
    reset() {
      this.sending = false;
      this.resetOnboardingEmail();
    },
    invite() {
      if (this.sending || !this.messageValid || !this.user?.userName) {
        return;
      }
      this.sending = true;
      const fullName = this.user.fullname || this.user.userName;
      return userCreationService.sendInvitation(this.user.userName, this.message)
        .then(sent => {
          if (sent) {
            this.$root.$emit('alert-message', this.$t('UsersManagement.onboard.sent', {0: fullName}), 'success');
            this.$root.$emit('refreshUsers');
            this.$root.$emit('pending-users-changed');
            this.close();
          } else {
            this.$root.$emit('alert-message', this.$t('UsersManagement.onboard.notSent', {0: fullName}), 'error');
          }
        })
        .finally(() => this.sending = false);
    },
  },
};
</script>
