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
    :loading="loading"
    allow-expand
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
            :title="$t('UserSettings.security.passwordChange.current')"
            :placeholder="$t('UserSettings.security.passwordChange.current')"
            :readonly="loading"
            name="currentPassword"
            autocomplete="current-password"
            prepend-inner-icon="fas fa-lock icon-default-color ms-n2"
            class="border-box-sizing full-width pa-0 mt-2"
            aria-required="true"
            type="password"
            tabindex="0"
            required="required"
            autofocus="autofocus"
            outlined
            dense />
          <div class="font-weight-bold mt-4">
            {{ $t('UserSettings.label.newPassword') }}
          </div>
          <v-text-field
            id="newPassword"
            ref="newPassword"
            v-model="newPassword"
            :title="$t('UserSettings.security.passwordChange.new')"
            :placeholder="$t('UserSettings.security.passwordChange.new')"
            :readonly="loading"
            name="newPassword"
            autocomplete="new-password"
            prepend-inner-icon="fas fa-lock icon-default-color ms-n2"
            class="border-box-sizing full-width pa-0 mt-2"
            aria-required="true"
            type="password"
            tabindex="0"
            required="required"
            outlined
            dense />
          <v-text-field
            id="confirmNewPassword"
            ref="confirmNewPassword"
            v-model="confirmNewPassword"
            :title="$t('UserSettings.security.passwordChange.confirmNew')"
            :placeholder="$t('UserSettings.security.passwordChange.confirmNew')"
            :readonly="loading"
            name="confirmNewPassword"
            autocomplete="new-password"
            prepend-inner-icon="fas fa-lock icon-default-color ms-n2"
            class="border-box-sizing full-width pa-0 mt-2"
            aria-required="true"
            type="password"
            tabindex="0"
            required="required"
            outlined
            dense />
        </v-card>
      </v-form>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :disabled="saving"
          class="btn me-2"
          @click="$emit('back')">
          {{ $t('UserSettings.button.cancel') }}
        </v-btn>
        <v-btn
          :loading="saving"
          :disabled="disabled"
          class="btn btn-primary"
          @click="savePassword">
          {{ $t('UserSettings.button.confirm') }}
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
    saving: false,
    displayed: true,
  }),
  computed: {
    disabled() {
      return !this.currentPassword?.length
        || !this.newPassword?.length
        || !this.confirmNewPassword?.length
        || this.saving;
    },
  },
  watch: {
    confirmNewPassword() {
      this.resetCustomValidity();
    },
  },
  created() {
    document.addEventListener('hideSettingsApps', (id) => {
      if (this.id !== id) {
        this.displayed = false;
      }
    });
    document.addEventListener('showSettingsApps', () => {
      this.displayed = true;
    });
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
    resetCustomValidity() {
      this.$refs.confirmNewPassword.$el.querySelector('input').setCustomValidity('');
    },
    savePassword() {
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
            this.$refs.form.$el.reset();
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
          .finally(() => {
            this.saving = false;
          });
      }
    },
  },
};
</script>

