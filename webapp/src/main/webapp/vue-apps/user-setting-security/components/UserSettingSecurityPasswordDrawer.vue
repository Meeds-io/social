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
      {{ $t('UserSettings.security.passwordChange.drawerTitle') }}
    </template>
    <template v-if="drawer" #content>
      <v-form
        ref="form"
        class="ma-5">
        <!-- Added for accessibility -->
        <input
          id="username"
          :value="username"
          name="username"
          autocomplete="username"
          class="d-none"
          required>
        <v-card flat>
          <div class="font-weight-bold">
            {{ $t('UserSettings.label.currentPassword') }}
          </div>
          <v-text-field
            id="currentPassword"
            ref="currentPassword"
            v-model="currentPassword"
            :aria-label="$t('UserSettings.security.passwordChange.current')"
            :placeholder="$t('UserSettings.security.passwordChange.current')"
            :readonly="saving"
            :type="currentPasswordType"
            name="currentPassword"
            autocomplete="current-password"
            prepend-inner-icon="fas fa-lock icon-default-color ms-n2"
            class="border-box-sizing full-width pa-0 mt-2"
            aria-required="true"
            required="required"
            outlined
            dense>
            <template #append>
              <v-tooltip bottom>
                <template #activator="{on, attrs}">
                  <v-btn
                    v-bind="attrs"
                    v-on="on"
                    :aria-label="currentPasswordTypeTooltip"
                    class="mt-n2"
                    icon
                    @click="switchPasswordType(1)">
                    <v-icon size="16">{{ currentPasswordIcon }}</v-icon>
                  </v-btn>
                </template>
                <span>{{ currentPasswordTypeTooltip }}</span>
              </v-tooltip>
            </template>
          </v-text-field>
          <div class="font-weight-bold mt-4">
            {{ $t('UserSettings.label.newPassword') }}
          </div>
          <v-text-field
            id="newPassword"
            ref="newPassword"
            v-model="newPassword"
            :aria-label="$t('UserSettings.security.passwordChange.new')"
            :placeholder="$t('UserSettings.security.passwordChange.new')"
            :readonly="saving"
            :type="newPasswordType"
            name="newPassword"
            autocomplete="new-password"
            prepend-inner-icon="fas fa-lock icon-default-color ms-n2"
            class="border-box-sizing full-width pa-0 mt-2"
            aria-required="true"
            required="required"
            outlined
            dense>
            <template #append>
              <v-tooltip bottom>
                <template #activator="{on, attrs}">
                  <v-btn
                    v-bind="attrs"
                    v-on="on"
                    :aria-label="newPasswordTypeTooltip"
                    class="mt-n2"
                    icon
                    @click="switchPasswordType(2)">
                    <v-icon size="16">{{ newPasswordIcon }}</v-icon>
                  </v-btn>
                </template>
                <span>{{ newPasswordTypeTooltip }}</span>
              </v-tooltip>
            </template>
          </v-text-field>
          <v-text-field
            id="confirmNewPassword"
            ref="confirmNewPassword"
            v-model="confirmNewPassword"
            :aria-label="$t('UserSettings.security.passwordChange.confirmNew')"
            :placeholder="$t('UserSettings.security.passwordChange.confirmNew')"
            :readonly="saving"
            :type="confirmNewPasswordType"
            name="confirmNewPassword"
            autocomplete="new-password"
            prepend-inner-icon="fas fa-lock icon-default-color ms-n2"
            class="border-box-sizing full-width pa-0 mt-2"
            aria-required="true"
            required="required"
            outlined
            dense>
            <template #append>
              <v-tooltip bottom>
                <template #activator="{on, attrs}">
                  <v-btn
                    v-bind="attrs"
                    v-on="on"
                    :aria-label="confirmNewPasswordTypeTooltip"
                    class="mt-n2"
                    icon
                    @click="switchPasswordType(3)">
                    <v-icon size="16">{{ confirmNewPasswordIcon }}</v-icon>
                  </v-btn>
                </template>
                <span>{{ confirmNewPasswordTypeTooltip }}</span>
              </v-tooltip>
            </template>
          </v-text-field>
          <div class="text-sub-title mt-2">
            <div
              :class="{
                'success--text': passwordMatch,
                'error--text': confirmNewPassword?.length && !passwordMatch,
              }"
              class="d-flex align-center">
              <v-icon size="12" class="me-2">fa-check</v-icon> {{ $t('UserSettings.security.passwordChange.condition.passwordMatch') }}
            </div>
            <div :class="{'success--text': passwordMinCharacters}" class="d-flex align-center">
              <v-icon size="12" class="me-2">fa-check</v-icon> {{ $t('UserSettings.security.passwordChange.condition.passwordMinCharacters') }}
            </div>
            <div :class="{'success--text': passwordOneUppercase}" class="d-flex align-center">
              <v-icon size="12" class="me-2">fa-check</v-icon> {{ $t('UserSettings.security.passwordChange.condition.passwordOneUppercase') }}
            </div>
            <div :class="{'success--text': passwordOneLowercase}" class="d-flex align-center">
              <v-icon size="12" class="me-2">fa-check</v-icon> {{ $t('UserSettings.security.passwordChange.condition.passwordOneLowercase') }}
            </div>
            <div :class="{'success--text': passwordOneNumber}" class="d-flex align-center">
              <v-icon size="12" class="me-2">fa-check</v-icon> {{ $t('UserSettings.security.passwordChange.condition.passwordOneNumber') }}
            </div>
            <div :class="{'success--text': passwordOneSpecialCharacter}" class="d-flex align-center">
              <v-icon size="12" class="me-2">fa-check</v-icon> {{ $t('UserSettings.security.passwordChange.condition.passwordOneSpecialCharacter') }}
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
const USER_NOT_FOUND_ERROR_CODE = 'USER_NOT_FOUND';
const WRONG_USER_PASSWORD_ERROR_CODE = 'WRONG_USER_PASSWORD';
const PASSWORD_UNKNOWN_ERROR_CODE = 'PASSWORD_UNKNOWN_ERROR_CODE';
const UNCHANGED_NEW_PASSWORD_ERROR_CODE = 'UNCHANGED_NEW_PASSWORD';

export default {
  data: () => ({
    drawer: false,
    username: eXo.env.portal.userName,
    currentPassword: null,
    newPassword: null,
    confirmNewPassword: null,
    currentPasswordType: 'password',
    newPasswordType: 'password',
    confirmNewPasswordType: 'password',
    saving: false,
  }),
  computed: {
    disabled() {
      return !this.currentPassword?.length
        || !this.newPassword?.length
        || !this.confirmNewPassword?.length
        || !this.passwordMatch
        || !this.passwordMinCharacters
        || !this.passwordOneUppercase
        || !this.passwordOneLowercase
        || !this.passwordOneNumber
        || !this.passwordOneSpecialCharacter
        || this.saving;
    },
    passwordMatch() {
      return this.newPassword?.length
        && this.newPassword === this.confirmNewPassword;
    },
    passwordMinCharacters() {
      return this.newPassword?.length > 8;
    },
    passwordOneUppercase() {
      return this.newPassword?.length && /[A-Z]+/.test(this.newPassword);
    },
    passwordOneLowercase() {
      return this.newPassword?.length && /[a-z]+/.test(this.newPassword);
    },
    passwordOneNumber() {
      return this.newPassword?.length && /\d+/.test(this.newPassword);
    },
    passwordOneSpecialCharacter() {
      return this.newPassword?.length && /[^\da-zA-Z]/.test(this.newPassword);
    },
    currentPasswordHidden() {
      return this.currentPasswordType === 'password';
    },
    currentPasswordIcon() {
      return this.currentPasswordHidden ? 'fa-eye' : 'fa-eye-slash';
    },
    currentPasswordTypeTooltip() {
      return this.currentPasswordHidden ? this.$t('UserSettings.security.passwordChange.viewPassword') : this.$t('UserSettings.security.passwordChange.hidePassword');
    },
    newPasswordHidden() {
      return this.newPasswordType === 'password';
    },
    newPasswordIcon() {
      return this.newPasswordHidden ? 'fa-eye' : 'fa-eye-slash';
    },
    newPasswordTypeTooltip() {
      return this.newPasswordHidden ? this.$t('UserSettings.security.passwordChange.viewPassword') : this.$t('UserSettings.security.passwordChange.hidePassword');
    },
    confirmNewPasswordHidden() {
      return this.confirmNewPasswordType === 'password';
    },
    confirmNewPasswordIcon() {
      return this.confirmNewPasswordHidden ? 'fa-eye' : 'fa-eye-slash';
    },
    confirmNewPasswordTypeTooltip() {
      return this.confirmNewPasswordHidden ? this.$t('UserSettings.documents.webdav.viewPassword') : this.$t('UserSettings.documents.webdav.hidePassword');
    },
  },
  watch: {
    confirmNewPassword() {
      this.resetCustomValidity();
    },
  },
  methods: {
    open() {
      this.currentPassword = null;
      this.newPassword = null;
      this.confirmNewPassword = null;
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    switchPasswordType(code) {
      if (code === 1) {
        this.currentPasswordType = this.currentPasswordHidden ? 'text' : 'password';
      } else if (code === 2) {
        this.newPasswordType = this.newPasswordHidden ? 'text' : 'password';
      } else {
        this.confirmNewPasswordType = this.confirmNewPasswordHidden ? 'text' : 'password';
      }
    },
    resetCustomValidity() {
      this.$refs.confirmNewPassword.$el.querySelector('input').setCustomValidity('');
    },
    save() {
      this.resetCustomValidity();

      if (!this.$refs.form.$el.reportValidity()) {
        return;
      }

      if (this.confirmNewPassword !== this.newPassword) {
        this.$refs.confirmNewPassword.$el.querySelector('input').setCustomValidity(this.$t('UserSettings.label.newPasswordsDoesNotMatch'));
        if (!this.$refs.form.$el.reportValidity()) {
          return;
        }
      }

      if (this.$refs.form.validate() && this.$refs.form.$el.reportValidity()) {
        this.saving = true;
        this.$userService.changePassword(eXo.env.portal.userName, this.currentPassword, this.newPassword)
          .then(() => {
            this.$root.$emit('alert-message', this.$t('UserSettings.label.changePasswordSuccess'), 'success');
            this.close();
          })
          .catch(e => {
            let error = String(e);

            if (error.indexOf(WRONG_USER_PASSWORD_ERROR_CODE) > -1) {
              error = this.$t('UserSettings.label.wrongCurrentPassword');
            } else if (error.indexOf(USER_NOT_FOUND_ERROR_CODE) > -1) {
              error = this.$t('UserSettings.label.accountNotExist');
            } else if (error.indexOf(PASSWORD_UNKNOWN_ERROR_CODE) > -1) {
              error = this.$t('UserSettings.label.changePasswordFail');
            } else if (error.indexOf(UNCHANGED_NEW_PASSWORD_ERROR_CODE) > -1) {
              error = this.$t('UserSettings.label.changePasswordIdentical');
            }
            this.$root.$emit('alert-message', error, 'error');
          })
          .finally(() => this.saving = false);
      }
    },
  },
};
</script>

