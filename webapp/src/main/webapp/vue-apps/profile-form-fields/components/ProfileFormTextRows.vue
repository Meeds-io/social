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
  <div>
    <div
      v-for="(row, index) in rows"
      :key="row.key"
      class="profileFormFieldValue d-flex align-start">
      <v-text-field
        :value="row.value"
        :placeholder="placeholder"
        :aria-label="name"
        :aria-required="index === 0 && setting.required ? 'true' : null"
        :name="setting.propertyName"
        :rules="index === 0 ? firstRowRules : rowRules"
        :error-messages="index === 0 ? error : null"
        class="pt-2"
        prepend-inner-icon="fas fa-ellipsis-h"
        autocomplete="off"
        outlined
        dense
        @input="setRow(index, $event)" />
      <v-btn
        v-if="rows.length > 1"
        :aria-label="$t('profileFormFields.removeValue', {0: name})"
        :title="$t('profileFormFields.removeValue', {0: name})"
        :disabled="disabled"
        class="profileFormFieldRemove mt-2 ms-1"
        icon
        @click="removeRow(index)">
        <v-icon size="16">fa-times</v-icon>
      </v-btn>
    </div>
    <v-btn
      :aria-label="$t('profileFormFields.addValue', {0: name})"
      :title="$t('profileFormFields.addValue', {0: name})"
      :disabled="disabled"
      class="profileFormFieldAdd"
      icon
      @click="addRow">
      <v-icon size="16">fa-plus</v-icon>
    </v-btn>
  </div>
</template>
<script>
/**
 * A multi-valued text attribute: one row per value, a + adding a row and a ×
 * removing one. The rules on all the values, Required and the size, show on
 * the first row; the format rule on each row
 */
export default {
  props: {
    setting: {
      type: Object,
      required: true,
    },
    value: {
      type: Array,
      default: null,
    },
    name: {
      type: String,
      default: null,
    },
    placeholder: {
      type: String,
      default: null,
    },
    error: {
      type: String,
      default: null,
    },
    requiredRule: {
      type: Function,
      default: null,
    },
    formatRule: {
      type: Function,
      default: null,
    },
    sizeRule: {
      type: Function,
      default: null,
    },
    disabled: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    rows: [],
    lastKey: 0,
  }),
  computed: {
    rowRules() {
      return [this.formatRule].filter(rule => rule);
    },
    firstRowRules() {
      const requiredRule = this.requiredRule && (() => this.requiredRule(this.rows.map(row => row.value)));
      return [requiredRule, this.formatRule, this.sizeRule].filter(rule => rule);
    },
  },
  created() {
    const values = this.value?.length ? this.value : [''];
    this.rows = values.map(value => this.newRow(value));
  },
  methods: {
    newRow(value) {
      return {key: ++this.lastKey, value};
    },
    setRow(index, value) {
      this.rows.splice(index, 1, Object.assign({}, this.rows[index], {value}));
      this.emitValues();
    },
    addRow() {
      this.rows.push(this.newRow(''));
    },
    removeRow(index) {
      this.rows.splice(index, 1);
      this.emitValues();
    },
    emitValues() {
      this.$emit('input', this.rows.map(row => row.value));
    },
  },
};
</script>
