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
  <profile-form-text-rows
    v-if="kind === 'rows'"
    :setting="setting"
    :value="value"
    :name="name"
    :placeholder="placeholder"
    :error="error"
    :required-rule="requiredRule"
    :format-rule="formatRule"
    :size-rule="sizeRule"
    :disabled="disabled"
    :icon="icon"
    :background-color="backgroundColor"
    :input-class="inputClass"
    :button-class="buttonClass"
    class="profileFormField"
    @input="$emit('input', $event)" />
  <profile-form-user-field
    v-else-if="kind === 'user'"
    :setting="setting"
    :value="value"
    :name="name"
    :placeholder="placeholder"
    :error="error"
    :rules="requiredRule ? [requiredRule] : []"
    class="profileFormField"
    @input="$emit('input', $event)" />
  <v-select
    v-else-if="kind === 'select'"
    :value="value"
    :items="options"
    :item-text="optionLabel"
    :multiple="setting.multiValued"
    :chips="setting.multiValued"
    :deletable-chips="setting.multiValued"
    :placeholder="placeholder"
    :aria-label="name"
    :aria-required="setting.required ? 'true' : null"
    :name="setting.propertyName"
    :rules="requiredRule ? [requiredRule] : []"
    :error-messages="error"
    :menu-props="{ offsetY: true, contentClass: 'profileFormOptionMenu' }"
    :prepend-inner-icon="icon"
    :background-color="backgroundColor"
    :class="inputClass"
    class="profileFormField"
    item-value="id"
    clearable
    outlined
    dense
    @change="$emit('input', $event)">
    <template #item="{ item }">
      <v-list-item-content class="profileFormOptionItem">
        <v-list-item-title>{{ optionLabel(item) }}</v-list-item-title>
      </v-list-item-content>
    </template>
    <!-- A chip can be removed only while the select is: its close button is
         reachable from the keyboard -->
    <template #selection="{ item, parent, disabled: selectDisabled }">
      <v-chip
        v-if="setting.multiValued"
        :close="!selectDisabled"
        :disabled="selectDisabled"
        class="profileFormFieldChip"
        @click:close="parent.selectItem(item)">
        {{ optionLabel(item) }}
      </v-chip>
      <span v-else class="profileFormFieldSelection text-truncate">{{ optionLabel(item) }}</span>
    </template>
  </v-select>
  <v-text-field
    v-else
    :value="value"
    :type="setting.propertyType === 'email' ? 'email' : 'text'"
    :placeholder="placeholder"
    :aria-label="name"
    :aria-required="setting.required ? 'true' : null"
    :name="setting.propertyName"
    :rules="valueRules"
    :error-messages="error"
    :prepend-inner-icon="icon"
    :background-color="backgroundColor"
    :class="inputClass"
    class="profileFormField"
    autocomplete="off"
    outlined
    dense
    @input="$emit('input', $event)" />
</template>
<script>
import {hasValue, isValidEmail, storedLength, MAX_STORED_LENGTH} from '../js/ProfileFormFieldRules.js';
import {optionLabel} from '../js/ProfileFormFieldValues.js';

/**
 * One attribute's field, drawn as the hosting form's own fields are: a text
 * field, a select over the attribute's options, rows of values for a
 * multi-valued text, a search on the users for a user attribute
 */
export default {
  props: {
    setting: {
      type: Object,
      required: true,
    },
    value: {
      type: [String, Number, Array],
      default: null,
    },
    // On an account's edition, the stored values, which the format rules
    // accept as they are
    unchangedValues: {
      type: Array,
      default: () => [],
    },
    // On an account's edition, the items a dropdown adds for stored values
    // no option is
    legacyOptions: {
      type: Array,
      default: () => [],
    },
    error: {
      type: String,
      default: null,
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    // Drawn in the registration form, whose own fields are white on the
    // page's background
    registration: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    icon() {
      return this.registration ? 'fas fa-ellipsis-h ms-n2 grey--text text--lighten-1' : 'fas fa-ellipsis-h';
    },
    backgroundColor() {
      return this.registration ? 'white' : null;
    },
    inputClass() {
      return this.registration ? 'login-username border-box-sizing' : 'pt-2';
    },
    buttonClass() {
      return this.registration ? null : 'mt-2';
    },
    kind() {
      if (this.setting.propertyType === 'user') {
        return 'user';
      } else if (this.setting.dropdownList) {
        return 'select';
      } else if (this.setting.multiValued) {
        return 'rows';
      } else {
        return 'text';
      }
    },
    // The attribute's label in the viewer's language, else its translated
    // name, else its technical name, as the attributes' administration shows it
    name() {
      const language = eXo?.env?.portal?.language || 'en';
      const label = this.setting.labels?.find?.(resolved => resolved.language === language)?.label;
      if (label) {
        return label;
      }
      const key = `profileSettings.property.name.${this.setting.propertyName}`;
      const translated = this.$t(key);
      return translated !== key ? translated : this.setting.propertyName;
    },
    options() {
      return (this.setting.propertyOptions || []).concat(this.legacyOptions);
    },
    placeholder() {
      return this.$t('profileFormFields.enterAttribute', {0: this.name});
    },
    requiredRule() {
      return this.setting.required
        ? value => hasValue(value) || this.$t('profileSettings.message.field.required')
        : null;
    },
    // Format rules on each value entered, never on a stored value left as it
    // is, which another writer may have stored unchecked; the size, on the
    // attribute's stored form, as a courtesy before the server's own count
    formatRule() {
      const unchecked = value => !hasValue(value) || this.unchangedValues.includes(String(value));
      if (!this.setting.multiValued && this.setting.propertyType === 'email') {
        return value => unchecked(value) || isValidEmail(value) || this.$t('profile.formField.invalidEmail');
      } else if (!this.setting.multiValued && ['call', 'messaging'].includes(this.setting.propertyType)) {
        return value => unchecked(value) || this.$utils.isValidPhone(value) || this.$t('profileFormFields.invalidPhone');
      } else if (this.setting.propertyName === 'urls') {
        return value => unchecked(value) || this.$utils.isValidUrl(value) || this.$t('profileFormFields.invalidUrl');
      }
      return null;
    },
    sizeRule() {
      return () => storedLength(this.value) <= MAX_STORED_LENGTH || this.$t('profile.formField.tooLong');
    },
    valueRules() {
      return [this.requiredRule, this.formatRule, this.sizeRule].filter(rule => rule);
    },
  },
  methods: {
    optionLabel,
  },
};
</script>
