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
    <template #content>
      <v-stepper
        v-if="drawer"
        v-model="stepper"
        class="ma-0 py-0 elevation-0"
        vertical
        flat>
        <v-stepper-step
          id="usersManagementCreationProfileStep"
          :step="1"
          class="usersManagementCreationStep ma-4 pa-0"
          editable>
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
            :extensions="extensions"
            :extension-contexts="extensionContexts"
            @update-extension-data="setExtensionData"
            @update-field="setField"
            @check-field="checkField"
            @next="next" />
        </v-stepper-content>
        <v-stepper-step
          id="usersManagementCreationRoleStep"
          :step="2"
          :editable="profileStepValid"
          class="usersManagementCreationStep ma-4 pa-0">
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
          id="usersManagementCreationInvitationStep"
          :step="3"
          :editable="invitationStepReachable"
          class="usersManagementCreationStep ma-4 pa-0">
          {{ $t('UsersManagement.creation.step.invitation') }}
        </v-stepper-step>
        <v-stepper-content :step="3" class="pa-0 ma-0 no-border">
          <users-management-creation-invitation-step
            :send-onboarding-email.sync="sendOnboardingEmail"
            :message.sync="message"
            :error-messages="fieldErrors.message"
            :saving="saving"
            @validity-updated="messageValid = $event" />
        </v-stepper-content>
      </v-stepper>
      <!-- Outside the content the drawer rebuilds at each open: the answer of a
           creation still running when the drawer is closed reaches it -->
      <users-management-creation-outcomes
        ref="outcomes"
        @refused="applyRefusal"
        @close="closeAfter" />
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
          id="usersManagementCreationNextButton"
          class="btn btn-primary"
          @click="next">
          {{ $t('UsersManagement.button.next') }}
        </v-btn>
        <v-btn
          v-else
          :disabled="saving || !currentStepValid"
          :loading="saving"
          id="usersManagementCreateButton"
          class="btn btn-primary"
          @click="create">
          {{ $t('UsersManagement.button.create') }}
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

export default {
  data: () => ({
    drawer: false,
    stepper: 0,
    saving: false,
    customizeUsername: false,
    customizePassword: false,
    sendOnboardingEmail: false,
    message: '',
    messageValid: true,
    // Counts the creations sent and the opens: an answer whose request bears
    // an earlier sequence belongs to a session the drawer has left
    creationSequence: 0,
    confirmPassword: '',
    user: {},
    fieldErrors: {},
    languages: [],
    defaultLanguage: null,
    extensions: [],
    // The context each extension's component receives, by extension id, new
    // at each open
    extensionContexts: {},
  }),
  computed: {
    fullName() {
      return `${this.user.firstName || ''} ${this.user.lastName || ''}`.trim();
    },
    // A value breaking its rule keeps Next disabled, whether the field has
    // been left yet or not
    profileRuleBroken() {
      return ['userName', 'firstName', 'lastName', 'email', 'password']
        .some(field => this.$usersManagementFieldRules.fieldRuleError(field, this.user[field]));
    },
    profileStepValid() {
      return (!this.customizeUsername || !!this.user.userName?.trim())
        && !!this.user.firstName?.trim()
        && !!this.user.lastName?.trim()
        && !!this.user.email?.trim()
        && !!this.user.language
        && (!this.customizePassword || (!!this.user.password && this.user.password === this.confirmPassword))
        && !this.profileRuleBroken;
    },
    // The third step is reachable once the two before it are valid
    invitationStepReachable() {
      return this.profileStepValid && !!this.user.role;
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
    // The extensions' fields are disabled while the account is saved, like
    // the drawer's own: what is written is what they held at the submit
    saving(saving) {
      Object.values(this.extensionContexts).forEach(context => context.saving = saving);
    },
  },
  created() {
    this.$root.$on('addNewUser', this.open);
    this.refreshExtensions();
    document.addEventListener(EXTENSIONS_UPDATED_EVENT, this.refreshExtensions);
  },
  beforeDestroy() {
    this.$root.$off('addNewUser', this.open);
    document.removeEventListener(EXTENSIONS_UPDATED_EVENT, this.refreshExtensions);
  },
  methods: {
    // The onboarding email goes back to blank before the content is rendered,
    // so the stepper never shows the previous session's message; the rest of
    // the state is reset once the drawer has opened
    open() {
      this.sendOnboardingEmail = false;
      this.message = '';
      this.messageValid = true;
      this.drawer = true;
    },
    close() {
      this.drawer = false;
    },
    // The drawer's content stays mounted after it closes: every field goes
    // back to its default when the drawer opens, on step 1
    reset() {
      this.creationSequence++;
      this.saving = false;
      this.customizeUsername = false;
      this.customizePassword = false;
      this.sendOnboardingEmail = false;
      this.message = '';
      this.messageValid = true;
      this.confirmPassword = '';
      this.fieldErrors = {};
      this.extensionContexts = this.newExtensionContexts();
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
      this.$userCreationService.getSupportedLanguages()
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
    // A field left with a value breaking its rule: the message, until the
    // next input. A value passing it leaves the field as it is, so a server
    // refusal still shown is not cleared by a mere focus; the confirmation's
    // mismatch is the step's own, shown as it is typed or left
    checkField(field) {
      const rule = this.$usersManagementFieldRules.fieldRuleError(field, this.user[field]);
      if (rule) {
        this.setError(field, this.$t(rule));
      }
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
      // What each extension holds is taken now: what is written once the
      // account exists is what the administrator submitted
      const request = {
        sequence: ++this.creationSequence,
        sendOnboardingEmail: this.sendOnboardingEmail,
        message: this.message,
        fullName: this.fullName,
        extensions: this.extensions.map(extension => ({
          extension,
          context: this.extensionContexts[extension.id],
          data: this.extensionContexts[extension.id]?.data || null,
        })),
      };
      const account = {
        userName: this.customizeUsername ? this.user.userName?.trim() : null,
        firstName: this.user.firstName.trim(),
        lastName: this.user.lastName.trim(),
        email: this.user.email.trim(),
        password: this.customizePassword ? this.user.password : null,
        language: this.user.language,
        role: this.user.role,
        enabled: true,
      };
      // The extensions' checks come before the account call: a refused field
      // takes the administrator back to the step showing it, and nothing is
      // created
      return this.validateExtensions(request).then(valid => {
        if (!this.current(request)) {
          return null;
        } else if (!valid) {
          this.stepper = 1;
          return null;
        }
        return this.$userCreationService.createAccount(account)
          .then(outcome => this.executeExtensions(outcome, request)
            .then(() => this.$refs.outcomes.handle(outcome, request)));
      }).catch(error => {
        // A rejection is either the administrator's cancel, an outcome the
        // outcomes component named, or a transport failure shown as the
        // generic error
        if (!this.$userAccountConfirmationService.isCancelled(error)) {
          this.$root.$emit('alert-message', error?.translated && error.message || this.$t('IDMManagement.error.UnknownServerError'), 'error');
        }
      }).finally(() => {
        if (this.current(request)) {
          this.saving = false;
        }
      });
    },
    refreshExtensions() {
      this.extensions = (extensionRegistry.loadExtensions(EXTENSION_APP, EXTENSION_TYPE) || [])
        .filter(extension => extension?.id && extension.vueComponent);
      const contexts = Object.assign({}, this.extensionContexts);
      this.extensions
        .filter(extension => !contexts[extension.id])
        .forEach(extension => contexts[extension.id] = this.newExtensionContext());
      this.extensionContexts = contexts;
    },
    newExtensionContexts() {
      const contexts = {};
      this.extensions.forEach(extension => contexts[extension.id] = this.newExtensionContext());
      return contexts;
    },
    newExtensionContext() {
      return {mode: 'create', data: null, saving: this.saving};
    },
    setExtensionData(extensionId, data) {
      const context = this.extensionContexts[extensionId];
      if (context) {
        context.data = data;
      }
    },
    // Each extension's check, one after the other, since each may ask for the
    // session confirmation; false as soon as one refuses
    validateExtensions(request) {
      return request.extensions.reduce((previous, {extension, context}) => previous.then(valid => {
        if (!valid || !extension.validate) {
          return valid;
        }
        return Promise.resolve(extension.validate(context)).then(result => result !== false);
      }), Promise.resolve(true));
    },
    // Once the account exists, created or created without all its parts,
    // each extension writes, with the data its context held at the submit,
    // and decides whether it has anything to write. A write that fails is
    // reported with the creation's outcome, the account being created all
    // the same. Once the drawer has opened another session, no write runs,
    // and an extension that held data is reported as not saved
    executeExtensions(outcome, request) {
      const userName = outcome?.userName || this.$userCreationService.parseIncomplete(outcome?.code)?.userName;
      const entries = request.extensions.filter(({extension}) => extension.execute);
      if (!userName || !entries.length) {
        return Promise.resolve();
      } else if (request.sequence !== this.creationSequence) {
        request.extensionsNotSaved = entries.some(({data}) => (Array.isArray(data) ? data.length > 0 : data != null));
        return Promise.resolve();
      }
      return Promise.all(entries.map(({extension, context, data}) =>
        Promise.resolve()
          .then(() => {
            context.data = data;
            return extension.execute(context, {userName});
          })
          .catch(() => request.extensionsNotSaved = true)));
    },
    // Whether the drawer still shows the session that sent a request: not
    // once it has been closed, or opened again on a new account
    current(request) {
      return this.drawer && request.sequence === this.creationSequence;
    },
    // A refused field, as the outcomes component read it: the message on
    // the field, the step holding it shown, the username opened when the
    // server would not generate one. Once the drawer has left that session,
    // the refusal is told as a message, since no field shows it any more
    applyRefusal({field, message, customizeUsername, step}, request) {
      if (!this.current(request)) {
        this.$root.$emit('alert-message', this.$t('UsersManagement.creation.notCreated', {0: message}), 'error');
        return;
      }
      this.setError(field, message);
      if (customizeUsername) {
        this.customizeUsername = true;
      }
      this.stepper = step;
    },
    // The drawer closes on the outcome of its own session only: a session
    // opened since stays as the administrator left it
    closeAfter(request) {
      if (this.current(request)) {
        this.close();
      }
    },
  },
};
</script>
