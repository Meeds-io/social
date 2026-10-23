<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

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
    ref="drawer"
    id="userCreationDrawer"
    v-model="drawer"
    allow-expand
    right
    @opened="reset"
    @closed="stepper = 0">
    <template #title>{{ $t('UsersManagement.addUser') }}</template>
    <template v-if="drawer" #content>
      <v-stepper
        v-model="stepper"
        class="ma-0 py-0 elevation-0"
        vertical
        flat>
        <v-stepper-step
          :step="1"
          :complete="stepper > 1"
          :editable="stepper > 1"
          class="ma-4 pa-0"
          color="error">
          {{ $t('UsersManagement.creation.step.profile') }}
        </v-stepper-step>
        <v-stepper-content :step="1" class="pa-0 ma-0 no-border">
          <users-management-creation-profile-step
            ref="profileStep"
            :user="user"
            :customize-username.sync="customizeUsername"
            :customize-password.sync="customizePassword"
            :confirm-password.sync="confirmPassword"
            :field-errors="fieldErrors"
            :languages="languages"
            :saving="saving"
            @update-field="setField"
            @next="next" />
        </v-stepper-content>
        <v-stepper-step
          :step="2"
          :complete="stepper > 2"
          :editable="stepper > 2"
          class="ma-4 pa-0"
          color="error">
          {{ $t('UsersManagement.creation.step.role') }}
        </v-stepper-step>
        <v-stepper-content :step="2" class="pa-0 ma-0 no-border">
          <users-management-creation-role-step
            v-model="user.role"
            :error-messages="fieldErrors.role"
            :saving="saving"
            @input="clearError('role')" />
        </v-stepper-content>
        <v-stepper-step
          :step="3"
          class="ma-4 pa-0"
          color="error">
          {{ $t('UsersManagement.creation.step.invitation') }}
        </v-stepper-step>
        <v-stepper-content :step="3" class="pa-0 ma-0 no-border">
          <users-management-creation-invitation-step
            :send-onboarding-email.sync="sendOnboardingEmail"
            :message.sync="message"
            :message-max-length="messageMaxLength"
            :error-messages="fieldErrors.message"
            :validity-label="linkValidityLabel"
            :saving="saving"
            @validity-updated="messageValid = $event" />
        </v-stepper-content>
      </v-stepper>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :disabled="saving"
          class="btn me-2"
          @click="close">
          {{ $t('UsersManagement.button.cancel') }}
        </v-btn>
        <v-btn
          v-if="stepper < 3"
          :disabled="saving || !currentStepValid"
          class="btn btn-primary"
          @click="next">
          {{ $t('UsersManagement.button.next') }}
        </v-btn>
        <v-btn
          v-else
          :disabled="saving || !currentStepValid"
          :loading="saving"
          class="btn btn-primary"
          @click="create">
          {{ $t('UsersManagement.button.create') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
import * as userCreationService from '../js/UserCreationService.js';
import UsersManagementCreationOutcomes from './creation/UsersManagementCreationOutcomes.js';
import UsersManagementOnboardingSettings from './onboarding/UsersManagementOnboardingSettings.js';

export default {
  mixins: [UsersManagementCreationOutcomes, UsersManagementOnboardingSettings],
  data: () => ({
    drawer: false,
    stepper: 0,
    saving: false,
    customizeUsername: false,
    customizePassword: false,
    sendOnboardingEmail: false,
    confirmPassword: '',
    user: {},
    fieldErrors: {},
    languages: [],
    defaultLanguage: null,
  }),
  computed: {
    fullName() {
      return `${this.user.firstName || ''} ${this.user.lastName || ''}`.trim();
    },
    profileStepValid() {
      return (!this.customizeUsername || !!this.user.userName?.trim())
        && !!this.user.firstName?.trim()
        && !!this.user.lastName?.trim()
        && !!this.user.email?.trim()
        && !!this.user.language
        && (!this.customizePassword || (!!this.user.password && this.user.password === this.confirmPassword));
    },
    currentStepValid() {
      switch (this.stepper) {
      case 1: return this.profileStepValid;
      case 2: return !!this.user.role;
      case 3: return !this.sendOnboardingEmail || this.messageValid;
      default: return false;
      }
    },
  },
  watch: {
    customizeUsername() {
      if (!this.customizeUsername) {
        this.setField('userName', null);
      }
    },
    customizePassword() {
      if (!this.customizePassword) {
        this.setField('password', null);
        this.confirmPassword = '';
        this.clearError('confirmPassword');
      }
    },
    confirmPassword() {
      this.clearError('confirmPassword');
    },
  },
  created() {
    this.$root.$on('addNewUser', this.open);
  },
  beforeDestroy() {
    this.$root.$off('addNewUser', this.open);
  },
  methods: {
    open() {
      this.drawer = true;
    },
    close() {
      this.drawer = false;
    },
    // The drawer's content stays mounted after it closes: every field goes
    // back to its default when the drawer opens, on step 1
    reset() {
      this.saving = false;
      this.customizeUsername = false;
      this.customizePassword = false;
      this.sendOnboardingEmail = false;
      this.resetOnboardingEmail();
      this.confirmPassword = '';
      this.fieldErrors = {};
      this.user = {
        userName: null,
        firstName: '',
        lastName: '',
        email: '',
        language: this.defaultLanguage,
        password: null,
        role: 'INTERNAL',
      };
      this.stepper = 1;
      userCreationService.getSupportedLanguages()
        .then(configuration => {
          this.languages = configuration.languages;
          this.defaultLanguage = configuration.defaultLanguage;
          if (!this.languages.some(language => language.value === this.user.language)) {
            this.user.language = this.languages.some(language => language.value === this.defaultLanguage)
              ? this.defaultLanguage
              : this.languages[0]?.value || null;
          }
        })
        .catch(() => this.$root.$emit('alert-message', this.$t('IDMManagement.error.UnknownServerError'), 'error'));
      this.$nextTick(() => this.$refs.profileStep?.$el?.querySelector('input[name="firstName"]')?.focus());
    },
    setField(field, value) {
      this.user = Object.assign({}, this.user, {[field]: value});
      this.clearError(field);
    },
    clearError(field) {
      if (this.fieldErrors[field]) {
        this.fieldErrors = Object.assign({}, this.fieldErrors, {[field]: null});
      }
    },
    setError(field, message) {
      this.fieldErrors = Object.assign({}, this.fieldErrors, {[field]: message});
    },
    next() {
      if (this.stepper === 1 && this.customizePassword && this.user.password !== this.confirmPassword) {
        this.setError('confirmPassword', this.$t('UsersManagement.newPasswordsDoesNotMatch'));
        return;
      }
      if (this.currentStepValid && this.stepper < 3) {
        this.stepper++;
      }
    },
    create() {
      if (!this.currentStepValid || this.saving) {
        return;
      }
      this.saving = true;
      return userCreationService.createAccount({
        userName: this.customizeUsername ? this.user.userName?.trim() : null,
        firstName: this.user.firstName.trim(),
        lastName: this.user.lastName.trim(),
        email: this.user.email.trim(),
        password: this.customizePassword ? this.user.password : null,
        language: this.user.language,
        role: this.user.role,
        enabled: true,
      }).then(outcome => {
        if (outcome.userName) {
          return this.invite(outcome.userName);
        } else if (outcome.status === 400) {
          this.showRefusal(outcome.code);
        } else {
          this.showFailure(outcome.code);
        }
      }).catch(error => {
        // A rejection is either the administrator's cancel, an outcome the
        // mixin named, or a transport failure shown as the generic error
        if (!this.$userAccountConfirmationService.isCancelled(error)) {
          this.$root.$emit('alert-message', error?.translated && error.message || this.$t('IDMManagement.error.UnknownServerError'), 'error');
        }
      }).finally(() => this.saving = false);
    },
  },
};
</script>
