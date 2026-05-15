<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

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
    id="SpaceMembersDrawer"
    ref="drawer"
    v-model="drawer"
    :loading="saving"
    no-x-scroll
    right
    @expand-updated="expanded = $event">
    <template #title>
      {{ $t('UserSettings.security.emailChange.drawerTitle') }}
    </template>
    <template v-if="drawer" #content>
      <v-form
        ref="form"
        class="ma-5">
        <v-card flat>
          <div class="font-weight-bold">
            {{ $t('UserSettings.security.emailChange.current') }}
          </div>
          <v-text-field
            id="email"
            ref="email"
            v-model="email"
            :aria-label="$t('UserSettings.security.emailChange.current')"
            readonly
            name="email"
            autocomplete="email"
            prepend-inner-icon="fas fa-envelope icon-default-color ms-n2"
            class="border-box-sizing full-width pa-0 mt-2"
            aria-required="true"
            type="email"
            required="required"
            outlined
            dense />
          <div class="font-weight-bold mt-4">
            {{ $t('UserSettings.security.emailChange.new') }}
          </div>
          <v-text-field
            id="newEmail"
            ref="newEmail"
            v-model="newEmail"
            :aria-label="$t('UserSettings.security.emailChange.newPlaceholder')"
            :placeholder="$t('UserSettings.security.emailChange.newPlaceholder')"
            :readonly="saving"
            name="newEmail"
            autocomplete="email"
            prepend-inner-icon="fas fa-envelope icon-default-color ms-n2"
            class="border-box-sizing full-width pa-0 mt-2"
            aria-required="true"
            type="email"
            required="required"
            outlined
            dense />
          <div class="d-flex flex-column justify-center align-center full-width mt-4">
            <template v-if="emailSent">
              <div class="full-width text-start">
                {{ $t('UserSettings.security.emailChange.confirmAccess.checkEmail') }}
              </div>
              <div class="d-flex align-center full-width mt-1">
                <v-text-field
                  id="otpCode"
                  ref="otpCode"
                  v-model="otpCode"
                  :aria-label="$t('UserSettings.security.emailChange.confirmAccess.inputTitle')"
                  :placeholder="$t('UserSettings.security.emailChange.confirmAccess.inputPlaceholder')"
                  :readonly="saving"
                  prepend-inner-icon="fas fa-lock icon-default-color ms-n2"
                  class="border-box-sizing flex-grow-1 px-0 pt-1 pb-0 me-2"
                  name="otpCode"
                  aria-required="true"
                  type="text"
                  required="required"
                  outlined
                  dense
                  @keyup.enter="verify" />
                <v-btn
                  :disabled="sendingCode"
                  :loading="sendingCode && operation === 'sendingEmail'"
                  height="40"
                  class="btn"
                  @click="sendOtpCode">
                  {{ $t('UserSettings.security.emailChange.resend') }}
                </v-btn>
              </div>
            </template>
            <div v-else>
              {{ $t('UserSettings.security.emailChange.confirmAccess.sendingEmail') }}
            </div>
          </div>
        </v-card>
      </v-form>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :disabled="saving"
          class="btn me-2"
          @click="close">
          {{ $t('UserSettings.button.cancel') }}
        </v-btn>
        <v-btn
          :loading="saving"
          :disabled="disabled"
          class="btn btn-primary"
          @click="save">
          {{ $t('UserSettings.button.save') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    username: eXo.env.portal.userName,
    otpMethod: 'email',
    otpCode: null,
    emailSent: false,
    operation: null,
    email: null,
    newEmail: null,
    saving: false,
    sendingCode: false,
  }),
  computed: {
    disabled() {
      return !this.email?.length
        || !this.newEmail?.length
        || !this.otpCode
        || this.sendingCode
        || this.saving;
    },
  },
  watch: {
    newEmail() {
      this.resetCustomValidity();
    },
    otpCode() {
      this.resetCustomValidity();
    },
  },
  methods: {
    async open() {
      this.newEmail = null;
      this.otpCode = null;
      this.emailSent = false;
      this.sendOtpCode();
      this.$refs.drawer.open();
      this.user = await this.$userService.getUser(this.username);
      this.email = this.user?.email;
    },
    close() {
      this.$refs.drawer.close();
    },
    async sendOtpCode() {
      this.operation = 'sendingEmail';
      this.sendingCode = true;
      try {
        await this.$otpService.sendOtpCode(this.otpMethod);
      } finally {
        this.sendingCode = false;
        this.emailSent = true;
      }
    },
    resetCustomValidity() {
      this.$refs.newEmail.$el.querySelector('input').setCustomValidity('');
      this.$refs.otpCode.$el.querySelector('input').setCustomValidity('');
    },
    save() {
      this.resetCustomValidity();
      if (!this.$refs.form.$el.reportValidity()) {
        return;
      } else if (this.$refs.form.validate()
          && this.$refs.form.$el.reportValidity()) {
        this.saving = true;
        this.$userService.updateProfileField(this.username, 'email', this.newEmail, this.otpMethod, this.otpCode)
          .then(() => {
            this.$root.$emit('alert-message', this.$t('UserSettings.security.emailChange.success'), 'success');
            this.close();
          })
          .catch(e => {
            const error = String(e);
            if (error.indexOf('OTP_CODE_WRONG') > -1) {
              this.$root.$emit('alert-message', this.$t('UserSettings.security.emailChange.wrongOtpCode'), 'error');
            } else if (error.indexOf('ALREADY_EXISTS') > -1) {
              this.$root.$emit('alert-message', this.$t('UsersManagement.message.userWithSameEmailAlreadyExists'), 'error');
            } else {
              this.$root.$emit('alert-message', error.replace('EMAIL:', ''), 'error');
            }
          })
          .finally(() => this.saving = false);
      }
    },
  },
};
</script>

