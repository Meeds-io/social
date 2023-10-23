<template>
  <v-card class="card-border-radius overflow-hidden" flat>
    <v-toolbar
      class="border-box-sizing"
      flat>
      <v-btn
        class="mx-1"
        icon
        height="36"
        width="36"
        @click="$emit('back')">
        <v-icon size="20">
          {{ $vuetify.rtl && 'mdi-arrow-right' || 'mdi-arrow-left' }}
        </v-icon>
      </v-btn>
      <v-toolbar-title class="ps-0">
        {{ $t('UserSettings.security') }}
      </v-toolbar-title>
      <v-spacer />
    </v-toolbar>

    <v-form
      ref="form"
      class="mt-6 mx-10 px-5">
      <!-- Added for accessibility -->
      <input
        id="username"
        name="username"
        autocomplete="username"
        class="hidden">

      <v-card flat>
        <v-card-text class="d-flex flex-grow-1 text-no-wrap text-left font-weight-bold pt-0 pb-2">
          {{ $t('UserSettings.label.currentPassword') }}*
        </v-card-text>
        <v-card-text class="d-flex pa-0">
          <input
            ref="currentPassword"
            v-model="currentPassword"
            type="password"
            autocomplete="current-password"
            class="ignore-vuetify-classes flex-grow-1"
            required>
        </v-card-text>
        <v-card-text class="d-flex flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('UserSettings.label.newPassword') }}*
        </v-card-text>
        <v-card-text class="d-flex pa-0">
          <input
            ref="newPassword"
            v-model="newPassword"
            type="password"
            autocomplete="new-password"
            class="ignore-vuetify-classes flex-grow-1"
            required>
        </v-card-text>
        <v-card-text class="d-flex flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('UserSettings.label.confirmNewPassword') }}*
        </v-card-text>
        <v-card-text class="d-flex pa-0">
          <input
            ref="confirmNewPassword"
            v-model="confirmNewPassword"
            type="password"
            autocomplete="new-password"
            class="ignore-vuetify-classes flex-grow-1"
            required>
        </v-card-text>

        <v-card-actions class="my-6">
          <v-spacer />
          <v-btn
            :disabled="saving"
            class="btn me-2"
            @click="$emit('back')">
            {{ $t('UserSettings.button.cancel') }}
          </v-btn>
          <v-btn
            :loading="saving"
            :disabled="saving"
            class="btn btn-primary"
            @click="savePassword">
            {{ $t('UserSettings.button.confirm') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-form>
  </v-card>
</template>

<script>
const USER_NOT_FOUND_ERROR_CODE = 'USER_NOT_FOUND';
const WRONG_USER_PASSWORD_ERROR_CODE = 'WRONG_USER_PASSWORD';
const PASSWORD_UNKNOWN_ERROR_CODE = 'PASSWORD_UNKNOWN_ERROR_CODE';
const UNCHANGED_NEW_PASSWORD_ERROR_CODE = 'UNCHANGED_NEW_PASSWORD';

export default {
  data: () => ({
    username: eXo.env.portal.userName,
    currentPassword: null,
    newPassword: null,
    confirmNewPassword: null,
    saving: false,
    displayed: true,
  }),
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
    resetCustomValidity() {
      this.$refs.confirmNewPassword.setCustomValidity('');
    },
    savePassword() {
      this.resetCustomValidity();

      if (!this.$refs.form.$el.reportValidity()) {
        return;
      }

      if (this.confirmNewPassword !== this.newPassword) {
        this.$refs.confirmNewPassword.setCustomValidity(this.$t('UserSettings.label.newPasswordsDoesNotMatch'));
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

