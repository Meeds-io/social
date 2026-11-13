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
  <div class="profileFormFields">
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
        :error="errors[setting.propertyName]"
        :disabled="disabled"
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
import {hasValue} from '../js/ProfileFormFieldRules.js';

/**
 * The attributes the account form lists, one field each, in the settings'
 * order: rendered by a host at its insertion point with the context it keeps,
 * whose data receives the values the administrator enters and whose saving,
 * set while the host saves, disables the fields
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
  }),
  computed: {
    // The host saves the account: its fields and these are disabled
    disabled() {
      return !!this.context?.saving;
    },
  },
  watch: {
    context(newContext, oldContext) {
      registry.unregister(oldContext, this);
      registry.register(newContext, this);
      if (this.loadState) {
        registry.setLoadState(newContext, this.loadState);
      }
    },
  },
  // The list is read when the form shows the fields, never when the page loads
  created() {
    registry.register(this.context, this);
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
  beforeDestroy() {
    registry.unregister(this.context, this);
  },
  methods: {
    // Each change the administrator makes, and only then, gives the host the
    // values to write
    update(propertyName, value) {
      this.values = Object.assign({}, this.values, {[propertyName]: value});
      this.errors = Object.assign({}, this.errors, {[propertyName]: null});
      this.formError = null;
      this.$emit('update:data', this.toData());
    },
    // The fields' rules, then the server's on the values to create, once the
    // list has answered; a list that could not be read leaves the server's
    // check, which refuses an account missing a Required attribute
    validate() {
      this.formError = null;
      // A list that answers while the check waits is rendered on the next
      // tick: the form's rules are read once it is
      return this.load.then(() => this.$nextTick()).then(() => {
        if (this.loadState === 'empty') {
          return true;
        } else if (this.loadState === 'listed' && !this.$refs.form.validate()) {
          return false;
        }
        return this.$profileFormFieldService.validateAccountFields(this.toData())
          .then(outcome => {
            if (!outcome.ok) {
              this.showRefusal(outcome.refusal);
            }
            return outcome.ok;
          });
      });
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
    toData() {
      return this.settings
        .filter(setting => hasValue(this.values[setting.propertyName]))
        .map(setting => {
          const value = this.values[setting.propertyName];
          return setting.multiValued
            ? {propertyName: setting.propertyName, values: value.filter(hasValue).map(String)}
            : {propertyName: setting.propertyName, value: String(value)};
        });
    },
  },
};
</script>
