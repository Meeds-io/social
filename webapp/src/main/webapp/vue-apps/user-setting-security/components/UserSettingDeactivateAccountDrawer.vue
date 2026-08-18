<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

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
    id="UserSettingDeactivateAccountDrawer"
    ref="drawer"
    v-model="drawer"
    no-x-scroll
    right>
    <template #title>
      {{ $t('UserSettings.security.deleteAccount.drawerTitle') }}
    </template>
    <template v-if="drawer" #content>
      <div class="ma-5">
        <div class="font-weight-bold">
          {{ $t('UserSettings.security.deleteAccount.warning') }}
        </div>
        <div class="mt-4">
          {{ $t('UserSettings.security.deleteAccount.confirmMessage') }}
        </div>
        <div class="d-flex flex-column justify-center align-center full-width mt-4">
          <template v-if="emailSent">
            <v-text-field
              id="deactivateAccountOtpCode"
              ref="otpCode"
              v-model="otpCode"
              :title="$t('UserSettings.security.deleteAccount.confirmAccess.inputTitle')"
              :placeholder="$t('UserSettings.security.deleteAccount.confirmAccess.inputPlaceholder')"
              :readonly="verifying || codeVerified"
              prepend-inner-icon="fas fa-lock icon-default-color ms-n2"
              class="border-box-sizing full-width px-0 pt-1 pb-0"
              name="otpCode"
              aria-required="true"
              type="text"
              tabindex="0"
              required="required"
              autofocus="autofocus"
              outlined
              dense
              @keyup.enter="verify" />
            <div class="d-flex mt-2">
              <v-btn
                :aria-label="$t('UserSettings.security.deleteAccount.resend')"
                :disabled="sendingCode || verifying || codeVerified"
                :loading="sendingCode"
                class="btn"
                @click="sendOtpCode">
                {{ $t('UserSettings.security.deleteAccount.resend') }}
              </v-btn>
              <div class="px-2"></div>
              <v-btn
                v-if="!codeVerified"
                :aria-label="$t('UserSettings.security.deleteAccount.verify')"
                :disabled="sendingCode || verifying || !otpCode"
                :loading="verifying"
                color="primary"
                class="btn"
                @click="verify">
                {{ $t('UserSettings.security.deleteAccount.verify') }}
              </v-btn>
              <div
                v-else
                class="d-flex align-center success--text">
                <v-icon size="16" class="success--text me-1">fa-check</v-icon>
                {{ $t('UserSettings.security.deleteAccount.codeVerified') }}
              </div>
            </div>
          </template>
          <div v-else>
            {{ $t('UserSettings.security.deleteAccount.confirmAccess.sendingEmail') }}
          </div>
        </div>
      </div>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :aria-label="$t('UserSettings.button.cancel')"
          :disabled="verifying"
          class="btn me-2"
          @click="close">
          {{ $t('UserSettings.button.cancel') }}
        </v-btn>
        <v-btn
          :aria-label="$t('UserSettings.button.confirm')"
          :disabled="!codeVerified"
          color="error"
          class="btn"
          @click="confirm">
          {{ $t('UserSettings.button.confirm') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data: () => ({
    drawer: false,
    otpMethod: 'accountDeletionEmail',
    otpCode: null,
    emailSent: false,
    sendingCode: false,
    verifying: false,
    codeVerified: false,
  }),
  watch: {
    otpCode() {
      if (!this.verifying) {
        this.codeVerified = false;
      }
    },
  },
  methods: {
    open() {
      this.otpCode = null;
      this.emailSent = false;
      this.codeVerified = false;
      this.sendOtpCode();
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    async sendOtpCode() {
      this.sendingCode = true;
      try {
        this.otpCode = null;
        this.codeVerified = false;
        await this.$otpService.sendOtpCode(this.otpMethod);
      } finally {
        this.sendingCode = false;
        this.emailSent = true;
      }
    },
    async verify() {
      if (!this.otpCode || this.codeVerified || this.verifying) {
        return;
      }
      this.verifying = true;
      try {
        await this.$otpService.validateOtpCode(this.otpMethod, this.otpCode);
        this.codeVerified = true;
      } catch {
        this.$root.$emit('alert-message', this.$t('UserSettings.security.deleteAccount.wrongOtpCode'), 'error');
      } finally {
        this.verifying = false;
      }
    },
    confirm() {
      // the confirmation action (options recap + state-changing call) is delivered by EXO-89276
    },
  },
};
</script>
