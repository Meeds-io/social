<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2026 Meeds Association contact@meeds.io

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
  <!-- Busy while the list, and on edition the account's values, are read -->
  <div
    :aria-busy="loadState === null ? 'true' : 'false'"
    class="profileFormFields">
    <!-- @submit.prevent: inside the host's form, Enter in a field must not
         submit this one natively -->
    <!-- Disabled while the host saves: the fields' inputs and their chips
         through the form, the rows' buttons through the field -->
    <v-form
      v-if="settings.length"
      ref="form"
      :disabled="disabled"
      @submit.prevent>
      <profile-form-field
        v-for="setting in settings"
        :key="setting.propertyName"
        :setting="setting"
        :value="values[setting.propertyName]"
        :unchanged-values="unchangedValues[setting.propertyName]"
        :legacy-options="legacyOptions[setting.propertyName]"
        :error="errors[setting.propertyName]"
        :disabled="disabled"
        :registration="loginPageMode"
        @input="update(setting.propertyName, $event)" />
    </v-form>
    <div
      v-if="formError"
      class="error--text caption pt-2"
      role="alert">
      {{ formError }}
    </div>
  </div>
</template>
<script>
import * as registry from '../js/ProfileFormFieldsRegistry.js';
import {toData, toEditionFields, toOnboardingFields} from '../js/ProfileFormFieldValues.js';

/**
 * The attributes a form lists, one field each, in the settings' order:
 * rendered by a host at its insertion point with the context it keeps, whose
 * data receives the values entered and whose saving, set while the host
 * saves, disables the fields. On an account's edition, the fields hold the
 * user's values, and the data only the attributes the administrator changed.
 * On registration, the list is read with the page's token, and nothing is
 * rendered on the e-mail-verification branch, which sends no attribute. On
 * onboarding, the list and the account's values are read with the page's
 * token, every listed attribute gets a field, and the data holds only the
 * attributes the user changed
 */
export default {
  props: {
    context: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    settings: [],
    values: {},
    errors: {},
    formError: null,
    // The list's load, and its outcome once answered: 'listed', 'empty' or
    // 'failed'
    load: null,
    loadState: null,
    // On edition, by attribute: the stored value as it is compared, the
    // values its format rules accept unchanged, and the dropdown items for
    // stored values no option is
    snapshot: {},
    unchangedValues: {},
    legacyOptions: {},
  }),
  computed: {
    // The host saves the account: its fields and these are disabled
    disabled() {
      return !!this.context?.saving;
    },
    editMode() {
      return this.context?.mode === 'edit';
    },
    registerMode() {
      return this.context?.mode === 'register';
    },
    onboardMode() {
      return this.context?.mode === 'onboard';
    },
    // The anonymous forms of the login pages, whose fields are drawn in their
    // own style and checked by their one request
    loginPageMode() {
      return this.registerMode || this.onboardMode;
    },
  },
  watch: {
    context(newContext, oldContext) {
      registry.unregister(oldContext, this);
      registry.register(newContext, this);
      if (this.editMode) {
        // Another account, or the same one opened again: its stored values
        this.loadFields();
      } else if (this.loadState) {
        registry.setLoadState(newContext, this.loadState);
      }
    },
  },
  // The list is read when the host renders the fields: when a drawer opens, or
  // with the registration form once the page's token is checked
  created() {
    registry.register(this.context, this);
    this.loadFields();
  },
  beforeDestroy() {
    registry.unregister(this.context, this);
  },
  methods: {
    loadFields() {
      if (this.editMode) {
        this.loadEditionFields();
        return;
      } else if (this.registerMode) {
        this.loadRegistrationFields();
        return;
      } else if (this.onboardMode) {
        this.loadOnboardingFields();
        return;
      }
      this.load = this.$profileFormFieldService.getAccountFormSettings()
        .then(settings => {
          this.settings = settings;
          this.loadState = settings.length ? 'listed' : 'empty';
        })
        .catch(() => {
          this.loadState = 'failed';
          this.$root.$emit('alert-message', this.$t('profileFormFields.notLoaded'), 'error');
        })
        .finally(() => registry.setLoadState(this.context, this.loadState));
    },
    // The list and the user's values, read together. An answer is applied
    // only while the host still gives the context it was read for, so a late
    // one never fills another account's form. When the list read fails, or
    // the user read fails while the list holds an attribute, no field is
    // shown and the host is asked not to save, until the form is opened
    // again; with no attribute listed, nothing is shown and nothing blocked
    loadEditionFields() {
      const context = this.context;
      this.settings = [];
      this.loadState = null;
      this.formError = null;
      // The user's values matter only once the list holds an attribute: their
      // read failing blocks nothing on a form that lists none
      const userRead = this.$userService.getUser(context.username, 'settings')
        .then(user => user?.properties || [], () => null);
      this.load = Promise.all([
        this.$profileFormFieldService.getAccountFormSettings(),
        userRead,
      ]).then(([settings, properties]) => {
        if (context !== this.context) {
          return;
        } else if (settings.length && !properties) {
          throw new Error('The user\'s values could not be read');
        }
        this.prefill(toEditionFields(settings, properties || []));
        this.loadState = this.settings.length ? 'listed' : 'empty';
      }).catch(() => {
        if (context === this.context) {
          this.loadState = 'failed';
          this.formError = this.$t('profileFormFields.notLoadedReopen');
          this.$emit('update:blocking', true);
        }
      }).finally(() => {
        if (context === this.context) {
          registry.setLoadState(context, this.loadState);
        }
      });
    },
    // The registration's list, read with the page's token. When it cannot be
    // read, the form cannot tell which attributes are required: an error is
    // shown and the registration is refused until the page is reloaded
    loadRegistrationFields() {
      if (this.context?.branch === 'verifyEmail') {
        this.loadState = 'empty';
        this.load = Promise.resolve();
        return;
      }
      this.load = this.$profileFormFieldService.getRegistrationFormSettings(this.context?.token)
        .then(settings => {
          this.settings = settings;
          this.loadState = settings.length ? 'listed' : 'empty';
        })
        .catch(() => {
          this.loadState = 'failed';
          this.formError = this.$t('profileFormFields.notLoadedReload');
        })
        .finally(() => registry.setLoadState(this.context, this.loadState));
    },
    // The onboarding's list and the values the link's account holds, read
    // with the page's token in one answer. The page renders this form only
    // once its own token check answered, so a link used or expired since
    // reads no field, and the save reports it. When the answer cannot be
    // read, the required attributes are unknown: an error is shown and the
    // onboarding is refused until the page is reloaded
    loadOnboardingFields() {
      this.load = this.$profileFormFieldService.getOnboardingFormFields(this.context?.token)
        .then(answer => {
          this.prefill(toOnboardingFields(answer.fields, answer.values));
          this.loadState = this.settings.length ? 'listed' : 'empty';
        })
        .catch(() => {
          this.loadState = 'failed';
          this.formError = this.$t('profileFormFields.notLoadedReload');
        })
        .finally(() => registry.setLoadState(this.context, this.loadState));
    },
    prefill(fields) {
      this.values = fields.values;
      this.snapshot = fields.snapshot;
      this.unchangedValues = fields.unchangedValues;
      this.legacyOptions = fields.legacyOptions;
      this.errors = {};
      this.settings = fields.settings;
    },
    // Each change entered, and only then, gives the host the values to write
    update(propertyName, value) {
      this.values = Object.assign({}, this.values, {[propertyName]: value});
      this.errors = Object.assign({}, this.errors, {[propertyName]: null});
      this.formError = null;
      this.$emit('update:data', this.toData());
    },
    // The fields' rules, then the server's on the values to write, once the
    // list has answered. A login page's list that could not be read keeps its
    // message until the page is reloaded
    validate() {
      if (!this.editMode && !(this.loginPageMode && this.loadState === 'failed')) {
        this.formError = null;
      }
      // A list that answers while the check waits is rendered on the next
      // tick: the form's rules are read once it is
      return this.load.then(() => this.$nextTick())
        .then(() => {
          if (this.editMode) {
            return this.validateEdition();
          } else if (this.loginPageMode) {
            return this.validateRegistration();
          }
          return this.validateCreation();
        });
    },
    // The fields' rules only, Required on every field, touched or not: the
    // registration or onboarding request is the server's check, which refuses
    // before the account exists or the password is set. A list that could
    // not be read refuses, since the required attributes are unknown
    validateRegistration() {
      if (this.loadState === 'failed') {
        return false;
      }
      return this.loadState !== 'listed' || this.$refs.form.validate();
    },
    // A list that could not be read leaves the server's check, which refuses
    // an account missing a Required attribute
    validateCreation() {
      if (this.loadState === 'empty') {
        return true;
      } else if (this.loadState === 'listed' && !this.$refs.form.validate()) {
        return false;
      }
      return this.$profileFormFieldService.validateAccountFields(this.toData())
        .then(outcome => this.readOutcome(outcome));
    },
    // Required on every field, touched or not; the server's check only when
    // an attribute changed, against the values the profile holds
    validateEdition() {
      if (this.loadState === 'failed') {
        return false;
      } else if (this.loadState !== 'listed') {
        return true;
      }
      this.formError = null;
      if (!this.$refs.form.validate()) {
        return false;
      }
      const data = this.toData();
      if (!data.length) {
        return true;
      }
      return this.$profileFormFieldService.validateUserFields(this.context.username, data)
        .then(outcome => this.readOutcome(outcome));
    },
    // The fields' own rules, with no call: what a Save after a failed write
    // checks before it sends that write alone
    checkRules() {
      if (this.loadState === 'failed') {
        return false;
      }
      return this.loadState !== 'listed' || this.$refs.form.validate();
    },
    readOutcome(outcome) {
      if (!outcome.ok) {
        this.showRefusal(outcome.refusal);
      }
      return outcome.ok;
    },
    // A refusal goes on the field of the attribute it names, else on the form
    showRefusal(refusal) {
      const message = refusal?.code ? this.$t(refusal.code) : this.$t('profileFormFields.notChecked');
      if (refusal?.propertyName && this.settings.some(setting => setting.propertyName === refusal.propertyName)) {
        this.errors = Object.assign({}, this.errors, {[refusal.propertyName]: message});
      } else {
        this.formError = message;
      }
    },
    // On an account's edition and on onboarding, the attributes changed from
    // the values the account holds; on creation and registration, the filled
    // ones
    toData() {
      return toData(this.settings, this.values, this.editMode || this.onboardMode ? this.snapshot : null);
    },
  },
};
</script>
