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
          :readonly="accountSaved"
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
          :readonly="accountSaved"
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
          :readonly="accountSaved"
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
          :readonly="accountSaved"
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
        <!-- The fields other applications add to the account, between the
             preferred language and the password -->
        <div
          v-if="extensions.length"
          id="userFormAdditionalFields">
          <template v-for="extension in extensions">
            <component
              :is="extension.vueComponent"
              v-if="extensionContexts[extension.id]"
              :key="extension.id"
              :context="extensionContexts[extension.id]"
              @update:data="setExtensionData(extension.id, $event)"
              @update:blocking="setExtensionBlocking(extension.id, $event)" />
          </template>
        </div>
        <v-text-field
          v-model="user.password"
          :readonly="accountSaved"
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
          :readonly="accountSaved"
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
// The extension point where other applications add fields to the account
const EXTENSION_APP = 'UsersManagement';
const EXTENSION_TYPE = 'account-form';
const EXTENSIONS_UPDATED_EVENT = `extension-${EXTENSION_APP}-${EXTENSION_TYPE}-updated`;

/**
 * Edits an existing account: names, email, preferred language and, for an
 * account of the internal store, its password, typed twice; then the fields
 * other applications add to it, written once the account is
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
    extensions: [],
    // The context each extension's component receives, by extension id, new
    // at each open
    extensionContexts: {},
    // The account is saved and the extensions' fields are not: its fields
    // are read-only and Save writes the extensions' fields alone
    accountSaved: false,
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
    // An extension that could not read what it shows asks that nothing be
    // saved until the drawer is opened again
    extensionsBlocking() {
      return Object.values(this.extensionContexts).some(context => context?.blocking);
    },
    valid() {
      return this.user.enabled != null
        && !!this.user.firstName?.trim()
        && !!this.user.lastName?.trim()
        && !!this.user.email?.trim()
        && !this.passwordMismatch
        && !this.ruleBroken
        && !this.extensionsBlocking;
    },
  },
  watch: {
    // The extensions' fields are disabled while the account is saved, like
    // the drawer's own: what is written is what they held at the submit
    saving(saving) {
      Object.values(this.extensionContexts).forEach(context => context.saving = saving);
    },
  },
  created() {
    this.$root.$on('editUser', this.editUser);
    this.refreshExtensions();
    document.addEventListener(EXTENSIONS_UPDATED_EVENT, this.refreshExtensions);
  },
  beforeDestroy() {
    this.$root.$off('editUser', this.editUser);
    document.removeEventListener(EXTENSIONS_UPDATED_EVENT, this.refreshExtensions);
  },
  methods: {
    // The component stays mounted after the drawer closes: every field is
    // set again from the account at each open, both passwords cleared
    editUser(row) {
      this.sessionSequence++;
      this.saving = false;
      this.accountSaved = false;
      this.fieldErrors = {};
      this.confirmPassword = '';
      this.confirmPasswordLeft = false;
      // The values an edit cancelled on one account entered never reach the
      // next save, on that account or another
      this.extensionContexts = this.newExtensionContexts(row.userName);
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
    async saveUser() {
      if (this.saving || !this.valid) {
        return;
      } else if (this.accountSaved) {
        return this.saveExtensions();
      }
      this.saving = true;
      const request = this.newRequest();
      try {
        // The extensions' checks come before the account call: a refused
        // field shows its error, and nothing is written
        const valid = await this.validateExtensions(request);
        if (!valid || !this.current(request)) {
          return;
        }
        const outcome = await this.$userCreationService.updateAccount({
          userName: this.user.userName,
          firstName: this.user.firstName.trim(),
          lastName: this.user.lastName.trim(),
          email: this.user.email.trim(),
          password: this.user.password || null,
          language: this.user.language || null,
          enabled: this.user.enabled,
        });
        if (outcome.ok) {
          this.$root.$emit('refreshUsers');
          this.showSaved(await this.executeExtensions(request), request);
        } else if (outcome.status === 500) {
          this.showFailure(outcome.code, request);
        } else {
          this.showRefusal(outcome.code, request);
        }
      } catch (error) {
        if (!this.$userAccountConfirmationService.isCancelled(error)) {
          this.tell(this.$t('IDMManagement.error.UnknownServerError'), request, true);
        }
      } finally {
        // The lock is the request's own: an account opened since keeps its
        if (request.sequence === this.sessionSequence) {
          this.saving = false;
        }
      }
    },
    // A Save once the account is saved and the extensions' fields are not:
    // those fields' write alone, never the account's again
    saveExtensions() {
      this.saving = true;
      const request = this.newRequest();
      return this.executeExtensions(request)
        .then(saved => this.showSaved(saved, request))
        .finally(() => {
          if (request.sequence === this.sessionSequence) {
            this.saving = false;
          }
        });
    },
    // What each extension holds is taken at the submit: what is written once
    // the account is saved is what the administrator submitted
    newRequest() {
      return {
        sequence: ++this.sessionSequence,
        userName: this.user.userName,
        fullName: this.user.fullname || this.user.userName,
        extensions: this.extensions.map(extension => ({
          extension,
          context: this.extensionContexts[extension.id],
          data: this.extensionContexts[extension.id]?.data || null,
        })),
      };
    },
    // The account and its extensions' fields saved: the drawer closes. The
    // account saved and those fields not: while the drawer still shows it,
    // it stays open with the fields filled, the passwords cleared, since a
    // password sent again would be changed again, and the account's fields
    // read-only; once the drawer has left it, the message names it
    showSaved(saved, request) {
      if (saved) {
        this.$root.$emit('alert-message', this.$t('UsersManagement.edit.success', {0: request.fullName}), 'success');
        if (this.current(request)) {
          this.close();
        }
      } else if (this.current(request)) {
        this.accountSaved = true;
        this.user = {...this.user, password: null};
        this.confirmPassword = '';
        this.confirmPasswordLeft = false;
        this.$root.$emit('alert-message', this.$t('UsersManagement.edit.fieldsNotSaved'), 'error');
      } else {
        this.$root.$emit('alert-message', this.$t('UsersManagement.edit.fieldsNotSavedFor', {0: request.fullName}), 'error');
      }
    },
    refreshExtensions() {
      this.extensions = (extensionRegistry.loadExtensions(EXTENSION_APP, EXTENSION_TYPE) || [])
        .filter(extension => extension?.id && extension.vueComponent);
      const contexts = {...this.extensionContexts};
      this.extensions
        .filter(extension => !contexts[extension.id])
        .forEach(extension => contexts[extension.id] = this.newExtensionContext(this.user.userName));
      this.extensionContexts = contexts;
    },
    newExtensionContexts(userName) {
      const contexts = {};
      this.extensions.forEach(extension => contexts[extension.id] = this.newExtensionContext(userName));
      return contexts;
    },
    newExtensionContext(userName) {
      return {mode: 'edit', username: userName, data: null, saving: this.saving, blocking: false};
    },
    setExtensionData(extensionId, data) {
      const context = this.extensionContexts[extensionId];
      if (context) {
        context.data = data;
      }
    },
    setExtensionBlocking(extensionId, blocking) {
      const context = this.extensionContexts[extensionId];
      if (context) {
        context.blocking = !!blocking;
      }
    },
    // Each extension's check, one after the other, since each may ask for the
    // session confirmation; false as soon as one refuses
    async validateExtensions(request, index = 0) {
      const entry = request.extensions[index];
      if (!entry) {
        return true;
      } else if (entry.extension.validate && (await entry.extension.validate(entry.context)) === false) {
        return false;
      }
      return this.validateExtensions(request, index + 1);
    },
    // Once the account is saved, each extension writes, with the data its
    // context held at the submit, and decides whether it has anything to
    // write; true when all have written. Once the drawer has been opened
    // again, on that account or another, no write runs, and an extension
    // that held data has not saved
    executeExtensions(request) {
      const entries = request.extensions.filter(({extension}) => extension.execute);
      if (request.sequence !== this.sessionSequence) {
        return Promise.resolve(!entries.some(({data}) => (Array.isArray(data) ? data.length > 0 : data != null)));
      }
      let saved = true;
      return Promise.all(entries.map(({extension, context, data}) =>
        Promise.resolve()
          .then(() => {
            context.data = data;
            return extension.execute(context, {userName: request.userName});
          })
          .catch(() => saved = false)))
        .then(() => saved);
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
