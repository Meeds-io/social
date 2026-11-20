<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2026 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

-->
<template>
  <div class="people-directory-attribute">
    <v-card
      :aria-label="label"
      :aria-describedby="showWarning && warningId || null"
      class="people-directory-attribute-card pa-3"
      role="group"
      outlined
      flat>
      <div class="d-flex align-center">
        <div class="text-subtitle-1 text-color text-truncate">{{ label }}</div>
        <v-spacer />
        <v-btn
          v-if="canAddValue"
          ref="addValueButton"
          :title="$t('peopleList.settings.attribute.addValues', {0: label})"
          :aria-label="$t('peopleList.settings.attribute.addValues', {0: label})"
          :aria-expanded="String(picking)"
          class="people-directory-attribute-add-value"
          icon
          @click="togglePicker">
          <v-icon size="21">fa-plus</v-icon>
        </v-btn>
        <v-btn
          ref="removeButton"
          :title="$t('peopleList.settings.attribute.remove', {0: label})"
          :aria-label="$t('peopleList.settings.attribute.remove', {0: label})"
          class="people-directory-attribute-remove"
          icon
          @click="$emit('remove')">
          <v-icon size="21" color="error">fa-trash</v-icon>
        </v-btn>
      </div>
      <!-- the field's label for assistive technologies; the design shows
           its text as the placeholder -->
      <label
        v-if="picking"
        :for="valueInputId"
        class="d-sr-only">
        {{ $t('peopleList.settings.attribute.valueSearch') }}
      </label>
      <v-autocomplete
        v-if="picking"
        :id="valueInputId"
        ref="valueInput"
        v-model="selectedValue"
        :items="values"
        :loading="loading"
        :search-input.sync="query"
        :placeholder="$t('peopleList.settings.attribute.valueSearch')"
        :no-data-text="$t('peopleList.settings.attribute.noValue')"
        item-text="label"
        item-value="value"
        class="people-directory-attribute-value-input mt-2 mb-0 mx-0 pa-0 elevation-0 no-border"
        no-filter
        outlined
        dense
        hide-details
        return-object
        @change="selectValue">
        <template #item="{item}">
          <v-list-item-content class="people-directory-attribute-value-option">
            <v-list-item-title>{{ item.label }}</v-list-item-title>
          </v-list-item-content>
        </template>
      </v-autocomplete>
      <div v-if="attribute.values.length" class="d-flex flex-wrap mt-2">
        <v-chip
          v-for="value in attribute.values"
          :key="value.value"
          :color="valuesRefused ? null : 'primary'"
          :class="valuesRefused && 'error-color-background'"
          text-color="white"
          class="people-directory-value-chip me-2 mb-2"
          dark
          small>
          {{ value.label }}
          <!-- the platform's Vuetify renders a chip's own close icon
               without its close-label: this button carries the name -->
          <v-btn
            :ref="`removeValue-${value.value}`"
            :aria-label="$t('peopleList.settings.attribute.removeValue', {0: value.label})"
            :title="$t('peopleList.settings.attribute.removeValue', {0: value.label})"
            class="people-directory-value-remove ms-1 me-n1"
            width="18"
            height="18"
            icon
            dark
            @click="removeValue(value)">
            <v-icon size="12" aria-hidden="true">fa-times</v-icon>
          </v-btn>
        </v-chip>
      </div>
    </v-card>
    <div
      v-if="showWarning"
      :id="warningId"
      class="people-directory-attribute-warning d-flex align-start mt-1">
      <v-icon
        size="14"
        class="icon-default-color me-2 mt-1"
        aria-hidden="true">
        fa-exclamation-triangle
      </v-icon>
      <span class="caption text-sub-title">
        {{ attribute.available ? $t('peopleList.settings.attribute.valuesRefused') : $t('peopleList.settings.attribute.unavailable') }}
      </span>
    </div>
  </div>
</template>
<script>
export default {
  props: {
    // {name, values: [{value, label}], available}
    attribute: {
      type: Object,
      required: true,
    },
    label: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    picking: false,
    loading: false,
    query: null,
    values: [],
    selectedValue: null,
    searchTimeout: null,
    abortController: null,
    // PeopleDirectoryService.MAX_VALUES_PER_ATTRIBUTE and MAX_VALUE_LENGTH
    maxValues: 20,
    maxValueLength: 150,
  }),
  computed: {
    // an attribute the administrator hid has no value to offer
    canAddValue() {
      return this.attribute.available && this.attribute.values.length < this.maxValues;
    },
    valueInputId() {
      return `${this.$root.appId}AttributeValue-${this.attribute.name}`.replace(/[^\w-]/g, '_');
    },
    warningId() {
      return `${this.$root.appId}AttributeWarning-${this.attribute.name}`.replace(/[^\w-]/g, '_');
    },
    // the warning goes with the card's values: none left, nothing to warn about
    showWarning() {
      return this.invalid && this.attribute.values.length > 0;
    },
    // a card the directory refuses: its attribute or its values
    invalid() {
      return !this.attribute.available || this.valuesRefused;
    },
    // values another writer stored beyond what the directory accepts: the
    // only case whose chips are red, the values themselves being wrong
    valuesRefused() {
      return this.attribute.values.length > this.maxValues
        || this.attribute.values.some(v => !v.value?.trim?.() || v.value.length > this.maxValueLength);
    },
  },
  watch: {
    query() {
      // one request on the last keystroke
      window.clearTimeout(this.searchTimeout);
      this.searchTimeout = window.setTimeout(this.searchValues, 300);
    },
  },
  beforeDestroy() {
    window.clearTimeout(this.searchTimeout);
    this.abortController?.abort?.();
  },
  methods: {
    // the trash button when the card offers no '+': an unavailable attribute,
    // or the values at their maximum
    focus() {
      (this.$refs.addValueButton || this.$refs.removeButton)?.$el?.focus?.();
    },
    togglePicker() {
      this.picking = !this.picking;
      if (this.picking) {
        this.searchValues();
        this.$nextTick(() => this.$refs.valueInput?.focus?.());
      }
    },
    // the values users already have for the attribute, as the server lists
    // them to an editor: the directory matches them exactly
    searchValues() {
      this.abortController?.abort?.();
      const abortController = new AbortController();
      this.abortController = abortController;
      this.loading = true;
      const params = new URLSearchParams({
        attribute: this.attribute.name,
        q: (this.query || '').substring(0, this.maxValueLength),
      });
      const url = this.$root.attributeValuesResourceUrl;
      const separator = url.includes('?') ? '&' : '?';
      return fetch(`${url}${separator}${params.toString()}`, {
        method: 'GET',
        credentials: 'include',
        signal: abortController.signal,
      }).then(resp => {
        if (!resp?.ok) {
          throw new Error('Error while reading the attribute values');
        }
        return resp.json();
      }).then(values => {
        const selected = new Set(this.attribute.values.map(v => v.value));
        this.values = (values || []).filter(v => !selected.has(v.value));
      }).catch(e => {
        if (e?.name !== 'AbortError') {
          this.values = [];
        }
      }).finally(() => {
        // an aborted request settles while the next one is in flight
        if (this.abortController === abortController) {
          this.loading = false;
        }
      });
    },
    selectValue(value) {
      if (!value?.value) {
        return;
      }
      this.$emit('add-value', value);
      this.values = this.values.filter(v => v.value !== value.value);
      this.$nextTick(() => {
        this.selectedValue = null;
        this.query = null;
      });
    },
    removeValue(value) {
      const index = this.attribute.values.indexOf(value);
      this.$emit('remove-value', value);
      // the focus goes to the next value's remove button, else the previous
      // one, never straight to the trash a second Enter would delete the
      // attribute with (RGAA 12.9)
      this.$nextTick(() => this.focusValue(index));
    },
    focusValue(index) {
      const values = this.attribute.values;
      const next = values.length && values[Math.min(index, values.length - 1)];
      const button = next && this.$refs[`removeValue-${next.value}`]?.[0];
      if (button) {
        button.$el.focus();
      } else {
        this.focus();
      }
    },
  },
};
</script>
