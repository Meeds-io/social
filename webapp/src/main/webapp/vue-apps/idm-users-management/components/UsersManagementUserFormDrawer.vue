<template>
  <exo-drawer
    id="userFormDrawer"
    ref="drawer"
    v-model="drawer"
    allow-expand
    right
    @opened="focusFirstName">
    <template #title>{{ $t('UsersManagement.editUser') }}</template>
    <template v-if="drawer" #content>
      <form
        ref="form"
        class="px-4 py-2"
        autocomplete="off"
        @submit.prevent="saveUser">
        <v-text-field
          v-model="user.firstName"
          :placeholder="$t('UsersManagement.creation.enterFirstName')"
          :disabled="saving"
          :error-messages="fieldErrors.firstName"
          class="pt-2"
          name="firstName"
          autocomplete="off"
          maxlength="2000"
          prepend-inner-icon="fa-user"
          outlined
          dense
          required
          @input="clearError('firstName')" />
        <v-text-field
          v-model="user.lastName"
          :placeholder="$t('UsersManagement.creation.enterLastName')"
          :disabled="saving"
          :error-messages="fieldErrors.lastName"
          class="pt-2"
          name="lastName"
          autocomplete="off"
          maxlength="2000"
          prepend-inner-icon="fa-user-alt"
          outlined
          dense
          required
          @input="clearError('lastName')" />
        <v-text-field
          v-model="user.email"
          :placeholder="$t('UsersManagement.creation.enterEmail')"
          :disabled="saving"
          :error-messages="fieldErrors.email"
          class="pt-2"
          name="email"
          type="email"
          autocomplete="off"
          maxlength="2000"
          prepend-inner-icon="far fa-envelope"
          outlined
          dense
          required
          @input="clearError('email')" />
        <v-select
          v-model="user.language"
          :items="languages"
          :disabled="saving || !languages.length"
          :error-messages="fieldErrors.language"
          :placeholder="$t('UsersManagement.creation.preferredLanguage')"
          :menu-props="{ offsetY: true }"
          class="pt-2"
          item-text="text"
          item-value="value"
          prepend-inner-icon="fas fa-language"
          outlined
          dense
          @change="clearError('language')">
          <template #selection="{ item }">
            <span class="text-truncate">
              {{ $t('UsersManagement.creation.preferredLanguage') }}: {{ item.text }}
            </span>
          </template>
        </v-select>
        <v-text-field
          v-model="user.password"
          :placeholder="$t('UsersManagement.edit.updatePassword')"
          :disabled="saving || !user.isInternal"
          :error-messages="fieldErrors.password"
          class="pt-2"
          name="password"
          type="password"
          autocomplete="new-password"
          prepend-inner-icon="fa-lock"
          outlined
          dense
          @input="clearError('password')" />
        <v-text-field
          v-model="confirmPassword"
          :placeholder="$t('UsersManagement.creation.confirmPassword')"
          :disabled="saving || !user.isInternal"
          :error-messages="confirmPasswordErrors"
          class="pt-2"
          name="confirmPassword"
          type="password"
          autocomplete="new-password"
          prepend-inner-icon="fa-lock"
          outlined
          dense
          @blur="confirmPasswordLeft = true" />
      </form>
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
          :disabled="saving || !valid"
          :loading="saving"
          class="btn btn-primary"
          @click="saveUser">
          {{ $t('UsersManagement.button.update') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
import * as userCreationService from '../js/UserCreationService.js';

/**
 * Edits an existing account: names, email, preferred language and, for an
 * account of the internal store, its password, typed twice
 */
export default {
  data: () => ({
    drawer: false,
    saving: false,
    user: {},
    confirmPassword: '',
    confirmPasswordLeft: false,
    fieldErrors: {},
    languages: [],
  }),
  computed: {
    passwordMismatch() {
      return (this.user.password || '') !== (this.confirmPassword || '');
    },
    // Update stays disabled while the two passwords differ, so the mismatch
    // is shown on the confirmation as soon as it is typed or left
    confirmPasswordErrors() {
      return this.passwordMismatch && (this.confirmPassword || this.confirmPasswordLeft)
        ? this.$t('UsersManagement.newPasswordsDoesNotMatch')
        : null;
    },
    valid() {
      return this.user.enabled != null
        && !!this.user.firstName?.trim()
        && !!this.user.lastName?.trim()
        && !!this.user.email?.trim()
        && !this.passwordMismatch;
    },
  },
  created() {
    this.$root.$on('editUser', this.editUser);
  },
  beforeDestroy() {
    this.$root.$off('editUser', this.editUser);
  },
  methods: {
    // The component stays mounted after the drawer closes: every field is
    // set again from the account at each open, both passwords cleared
    editUser(row) {
      this.saving = false;
      this.fieldErrors = {};
      this.confirmPassword = '';
      this.confirmPasswordLeft = false;
      this.user = {
        userName: row.userName,
        firstName: row.firstName || '',
        lastName: row.lastName || '',
        email: row.email || '',
        enabled: row.enabled,
        isInternal: row.isInternal,
        fullname: row.fullname || row.fullName,
        language: null,
        password: null,
      };
      this.drawer = true;
      userCreationService.getSupportedLanguages()
        .then(configuration => this.languages = configuration.languages)
        .catch(() => this.languages = []);
      // The stored preferred language: none stored leaves the select empty,
      // and the update then leaves it unchanged. Opened by a username alone
      // (an account created without its language), the account read fills
      // the rest
      userCreationService.getAccount(row.userName)
        .then(account => {
          if (account?.userName === this.user.userName) {
            this.user = Object.assign({}, this.user, {
              firstName: this.user.firstName || account.firstName || '',
              lastName: this.user.lastName || account.lastName || '',
              email: this.user.email || account.email || '',
              enabled: this.user.enabled ?? account.enabled,
              isInternal: this.user.isInternal ?? account.isInternal,
              fullname: this.user.fullname || account.fullName,
              language: this.user.language || account.language || null,
            });
          }
        })
        .catch(() => {
          // Opened by a username alone, the account is unknown until read:
          // an update now would send an unknown status
          if (this.user.userName === row.userName && this.user.enabled == null) {
            this.$root.$emit('alert-message', this.$t('IDMManagement.error.UnknownServerError'), 'error');
            this.close();
          }
        });
    },
    focusFirstName() {
      this.$nextTick(() => this.$refs.form?.querySelector('input[name="firstName"]')?.focus());
    },
    close() {
      this.drawer = false;
    },
    clearError(field) {
      if (this.fieldErrors[field]) {
        this.fieldErrors = Object.assign({}, this.fieldErrors, {[field]: null});
      }
    },
    setError(field, message) {
      this.fieldErrors = Object.assign({}, this.fieldErrors, {[field]: message});
    },
    saveUser() {
      if (this.saving || !this.valid) {
        return;
      }
      this.saving = true;
      return userCreationService.updateAccount({
        userName: this.user.userName,
        firstName: this.user.firstName.trim(),
        lastName: this.user.lastName.trim(),
        email: this.user.email.trim(),
        password: this.user.password || null,
        language: this.user.language || null,
        enabled: this.user.enabled,
      }).then(outcome => {
        if (outcome.ok) {
          this.$root.$emit('alert-message', this.$t('UsersManagement.edit.success', {0: this.user.fullname || this.user.userName}), 'success');
          this.$root.$emit('refreshUsers');
          this.close();
        } else if (outcome.status === 500) {
          this.showFailure(outcome.code);
        } else {
          this.showRefusal(outcome.code);
        }
      }).catch(error => {
        if (!this.$userAccountConfirmationService.isCancelled(error)) {
          this.$root.$emit('alert-message', this.$t('IDMManagement.error.UnknownServerError'), 'error');
        }
      }).finally(() => this.saving = false);
    },
    // A refused field: its code on the field, translated when a label exists
    showRefusal(code) {
      const refusal = userCreationService.parseRefusal(code);
      if (refusal) {
        this.setError(refusal.field, this.translateCode(code) || refusal.detail || code);
      } else {
        this.$root.$emit('alert-message', this.translateCode(code) || this.$t('IDMManagement.error.UnknownServerError'), 'error');
      }
    },
    // An update the server could not prove: the drawer stays open on the
    // error, and the table shows what the account now holds
    showFailure(code) {
      this.$root.$emit('alert-message', this.translateCode(code) || this.$t('IDMManagement.error.UnknownServerError'), 'error');
      this.$root.$emit('refreshUsers');
    },
    translateCode(code) {
      const suffix = (code || '').replace(/:/g, '.');
      for (const prefix of ['UsersManagement.edit.error.', 'UsersManagement.creation.error.', 'UsersManagement.error.']) {
        const label = this.$t(`${prefix}${suffix}`, {0: this.user.fullname || this.user.userName});
        if (label !== `${prefix}${suffix}`) {
          return label;
        }
      }
      return null;
    },
  },
};
</script>
