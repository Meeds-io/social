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
          @input="clearError('firstName')"
          @blur="checkField('firstName')" />
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
          @input="clearError('lastName')"
          @blur="checkField('lastName')" />
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
          @input="clearError('email')"
          @blur="checkField('email')" />
        <v-select
          v-model="user.language"
          :items="languages"
          :disabled="saving || !languages.length"
          :error-messages="fieldErrors.language"
          :placeholder="$t('UsersManagement.creation.preferredLanguage')"
          :menu-props="{ offsetY: true }"
          class="userFormLanguageSelect pt-2"
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
          @input="clearError('password')"
          @blur="checkField('password')" />
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
          id="usersManagementUpdateButton"
          class="btn btn-primary"
          @click="saveUser">
          {{ $t('UsersManagement.button.update') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
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
    // Counts the opens and the updates sent: an answer whose request bears
    // an earlier sequence belongs to an account the drawer has left
    sessionSequence: 0,
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
    // A value breaking its rule keeps Update disabled, whether the field has
    // been left yet or not; a blank password leaves it unchanged
    ruleBroken() {
      return ['firstName', 'lastName', 'email', 'password']
        .some(field => this.$usersManagementFieldRules.fieldRuleError(field, this.user[field]));
    },
    valid() {
      return this.user.enabled != null
        && !!this.user.firstName?.trim()
        && !!this.user.lastName?.trim()
        && !!this.user.email?.trim()
        && !this.passwordMismatch
        && !this.ruleBroken;
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
      this.sessionSequence++;
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
      this.checkStoredFields();
      this.drawer = true;
      this.$userCreationService.getSupportedLanguages()
        .then(configuration => this.languages = configuration.languages)
        .catch(() => this.languages = []);
      // The stored preferred language: none stored leaves the select empty,
      // and the update then leaves it unchanged. Opened by a username alone
      // (an account created without its language), the account read fills
      // the rest
      this.$userCreationService.getAccount(row.userName)
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
            this.checkStoredFields();
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
    // A field left with a value breaking its rule: the message, until the
    // next input. A value passing it leaves the field as it is, so a server
    // refusal still shown is not cleared by a mere focus
    checkField(field) {
      const rule = this.$usersManagementFieldRules.fieldRuleError(field, this.user[field]);
      if (rule) {
        this.setError(field, this.$t(rule));
      }
    },
    // A stored name or email the rule refuses says so from the opening rather
    // than through a disabled button. The rule reads every character, while the
    // server skips a name's last one: a stored name whose last character the
    // rule refuses is corrected here before the account is updated
    checkStoredFields() {
      ['firstName', 'lastName', 'email'].forEach(field => this.checkField(field));
    },
    saveUser() {
      if (this.saving || !this.valid) {
        return;
      }
      this.saving = true;
      const request = {
        sequence: ++this.sessionSequence,
        fullName: this.user.fullname || this.user.userName,
      };
      return this.$userCreationService.updateAccount({
        userName: this.user.userName,
        firstName: this.user.firstName.trim(),
        lastName: this.user.lastName.trim(),
        email: this.user.email.trim(),
        password: this.user.password || null,
        language: this.user.language || null,
        enabled: this.user.enabled,
      }).then(outcome => {
        if (outcome.ok) {
          this.$root.$emit('alert-message', this.$t('UsersManagement.edit.success', {0: request.fullName}), 'success');
          this.$root.$emit('refreshUsers');
          if (this.current(request)) {
            this.close();
          }
        } else if (outcome.status === 500) {
          this.showFailure(outcome.code, request);
        } else {
          this.showRefusal(outcome.code, request);
        }
      }).catch(error => {
        if (!this.$userAccountConfirmationService.isCancelled(error)) {
          this.tell(this.$t('IDMManagement.error.UnknownServerError'), request, true);
        }
      }).finally(() => {
        // The lock is the request's own: an account opened since keeps its
        if (request.sequence === this.sessionSequence) {
          this.saving = false;
        }
      });
    },
    // Whether the drawer still shows the account that sent a request: not
    // once it has been closed, or opened again on another account
    current(request) {
      return this.drawer && request.sequence === this.sessionSequence;
    },
    // An error of an update, told as it is while the drawer still shows the
    // account it was sent for, and with that account's name once the drawer
    // has left it, so the message is never read as the open account's. A
    // refused field means nothing was written; any other answer may follow a
    // partial write, so its wrapper leaves the outcome to the message
    tell(message, request, mayBeWritten = false) {
      if (this.current(request)) {
        this.$root.$emit('alert-message', message, 'error');
      } else {
        this.$root.$emit('alert-message', this.$t(mayBeWritten ? 'UsersManagement.edit.errorFor' : 'UsersManagement.edit.notUpdated', {0: request.fullName, 1: message}), 'error');
      }
    },
    // A refused field: its code on the field, translated when a label exists.
    // Once the drawer has left that account, the refusal is told as a
    // message, since no field shows it any more. An answer naming no field
    // (a gateway's status among them) may follow a write
    showRefusal(code, request) {
      const refusal = this.$userCreationService.parseRefusal(code);
      if (!refusal) {
        this.tell(this.translateCode(code, request.fullName) || this.$t('IDMManagement.error.UnknownServerError'), request, true);
      } else if (this.current(request)) {
        this.setError(refusal.field, this.translateCode(code, request.fullName) || refusal.detail || code);
      } else {
        this.tell(this.translateCode(code, request.fullName) || refusal.detail || code, request);
      }
    },
    // An update the server could not prove: the drawer stays open on the
    // error while it still shows the account, and the table shows what the
    // account now holds
    showFailure(code, request) {
      this.tell(this.translateCode(code, request.fullName) || this.$t('IDMManagement.error.UnknownServerError'), request, true);
      this.$root.$emit('refreshUsers');
    },
    // The code's label under the edit, creation or generic errors, with the
    // account's name as its parameter, the one the request was sent for
    translateCode(code, fullName = this.user.fullname || this.user.userName) {
      return this.$userCreationService.translateCode(
        key => this.$t(key, {0: fullName}),
        code,
        ['UsersManagement.edit.error.', 'UsersManagement.creation.error.', 'UsersManagement.error.']);
    },
  },
};
</script>
