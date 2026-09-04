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
        <div>
          {{ $t('UserSettings.security.deleteAccount.warning') }}
        </div>
        <v-checkbox
          :input-value="true"
          :aria-label="$t('UserSettings.security.deleteAccount.option.deactivateAccount')"
          class="mt-4"
          disabled
          dense
          hide-details>
          <template #label>
            <span class="font-weight-bold">
              {{ $t('UserSettings.security.deleteAccount.option.deactivateAccount') }}
            </span>
          </template>
        </v-checkbox>
        <v-checkbox
          v-if="$root.deletionAllowed"
          v-model="deleteAccount"
          :aria-label="$t('UserSettings.security.deleteAccount.option.deleteAccount')"
          :readonly="saving"
          class="mt-2"
          dense
          hide-details>
          <template #label>
            <span class="font-weight-bold">
              {{ $t('UserSettings.security.deleteAccount.option.deleteAccount') }}
            </span>
          </template>
        </v-checkbox>
        <div class="mt-6">
          {{ $t('UserSettings.security.deleteAccount.confirmMessage') }}
        </div>
        <template v-if="emailSent">
          <div class="d-flex align-center full-width mt-4">
            <v-text-field
              id="deactivateAccountOtpCode"
              ref="otpCode"
              v-model="otpCode"
              :title="$t('UserSettings.security.deleteAccount.confirmAccess.inputTitle')"
              :placeholder="$t('UserSettings.security.deleteAccount.confirmAccess.inputPlaceholder')"
              :readonly="saving"
              prepend-inner-icon="fas fa-lock icon-default-color ms-n2"
              class="border-box-sizing flex-grow-1 px-0 pt-1 pb-0 me-2"
              name="otpCode"
              aria-required="true"
              type="text"
              tabindex="0"
              required="required"
              autofocus="autofocus"
              outlined
              dense
              @keyup.enter="confirmRequest" />
            <v-btn
              :aria-label="$t('UserSettings.security.deleteAccount.resend')"
              :disabled="sendingCode || saving"
              :loading="sendingCode"
              height="40"
              class="btn"
              @click="sendOtpCode">
              {{ $t('UserSettings.security.deleteAccount.resend') }}
            </v-btn>
          </div>
        </template>
        <div v-else class="mt-4">
          {{ $t('UserSettings.security.deleteAccount.confirmAccess.sendingEmail') }}
        </div>
      </div>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :aria-label="$t('UserSettings.button.cancel')"
          class="btn me-2"
          @click="close">
          {{ $t('UserSettings.button.cancel') }}
        </v-btn>
        <v-btn
          :aria-label="$t('UserSettings.button.confirm')"
          :disabled="confirmDisabled"
          :outlined="confirmDisabled"
          :class="!confirmDisabled && 'error-color-background'"
          :dark="!confirmDisabled"
          :loading="saving"
          elevation="0"
          @click="confirmRequest">
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
    otpMethod: 'accountDeactivationEmail',
    otpCode: null,
    deleteAccount: false,
    emailSent: false,
    sendingCode: false,
    saving: false,
  }),
  computed: {
    confirmDisabled() {
      return !this.otpCode || this.saving || this.sendingCode;
    },
  },
  methods: {
    open() {
      this.otpCode = null;
      this.deleteAccount = false;
      this.emailSent = false;
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
        await this.$otpService.sendOtpCode(this.otpMethod);
      } catch (e) {
        if (e?.message === '429') {
          // throttled: the previously sent code remains valid until its expiration
          this.$root.$emit('alert-message', this.$t('UserSettings.security.otpAlreadySent'), 'warning');
        } else {
          this.$root.$emit('alert-message', this.$t('UserSettings.security.deleteAccount.otpSendError'), 'error');
        }
      } finally {
        this.sendingCode = false;
        this.emailSent = true;
      }
    },
    async confirmRequest() {
      if (!this.otpCode || this.saving) {
        return;
      }
      this.saving = true;
      try {
        await this.$accountDeactivationService.requestDeactivation(this.otpMethod, this.otpCode, this.deleteAccount);
        // site-less URL, served to anonymous users against the global site,
        // the same way as the /portal/forgot-password public page
        const accountDeactivatedPageUri = `${eXo.env.portal.context}/account-deactivated`;
        window.location.href = `${eXo.env.portal.context}/logout?initialURI=${encodeURIComponent(accountDeactivatedPageUri)}`;
      } catch (e) {
        this.saving = false;
        if (e?.message === '401') {
          this.$root.$emit('alert-message', this.$t('UserSettings.security.deleteAccount.wrongOtpCode'), 'error');
        } else if (e?.message === '403') {
          this.$root.$emit('alert-message', this.$t('UserSettings.security.deleteAccount.notAllowed'), 'error');
        } else {
          this.$root.$emit('alert-message', this.$t('UserSettings.security.deleteAccount.deactivationError'), 'error');
        }
      }
    },
  },
};
</script>
