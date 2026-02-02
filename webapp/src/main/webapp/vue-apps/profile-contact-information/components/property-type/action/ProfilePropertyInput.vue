<!--
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 -->

<template>
  <div>
    <v-card-text class="d-flex flex-grow-1 text-no-wrap pb-2">
      {{ resolvedName }}
      <span v-if="isRequired">*</span>
    </v-card-text>
    <v-card-text class="d-flex py-0">
      <v-card-text
        :title="disabledFieldTitle"
        class="d-flex pa-0 flex-grow-1">
        <v-text-field
          v-model="localValue"
          :disabled="disabled"
          :type="inputType"
          :required="isRequired"
          :rules="rulesInput"
          :ref="`${propertyName}Input`"
          maxlength="2000"
          hide-details="auto"
          class="pt-0"
          dense
          outlined
          @input="$emit('updated', localValue)"
          @change="$emit('updated', localValue)" />
      </v-card-text>
      <profile-hide-property-button
        :property="property" />
    </v-card-text>
  </div>
</template>

<script>

export default {
  props: {
    property: {
      type: Object,
      default: null
    },
    resolvedName: {
      type: String,
      default: null
    },
    disabledFieldTitle: {
      type: String,
      default: null
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      localValue: this.property?.value || '',
    };
  },
  watch: {
    'property.value'(newVal) {
      this.localValue = newVal;
    }
  },
  computed: {
    isRequired() {
      return this.property?.required;
    },
    propertyName() {
      return this.property?.propertyName;
    },
    inputType() {
      return (
        this.property?.propertyName === 'email' ||
          this.property?.propertyType === 'email'
      ) ? 'email' : 'text';
    },
    rulesInput() {
      const rules = [];
      if (this.isRequired) {
        rules.push(v => !!v
            || this.$t('profileContactInformation.message.field.required'));
      }
      switch (this.property?.propertyType) {
      case 'call':
      case 'messaging':
        rules.push(v => !v || this.$utils.isValidPhone(v)
            || this.$t('profileContactInformation.message.field.call.invalid'));
        break;
      case 'email':
        rules.push(v => !v || this.$utils.isValidEmail(v)
            || this.$t('profileContactInformation.message.field.email.invalid'));
        break;
      default:
        break;
      }
      return rules;
    },
  }
};
</script>