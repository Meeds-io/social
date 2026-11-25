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
  <div class="people-list-attribute-filter">
    <div class="d-flex align-center text-start">
      <div class="text-subtitle-1 text-color">
        {{ $t('peopleList.settings.attributeFilter') }}
      </div>
      <v-spacer />
      <v-switch
        :id="switchId"
        :input-value="enabled"
        :aria-label="$t('peopleList.settings.attributeFilter')"
        class="people-list-attribute-filter-switch ma-0 width-fit-content"
        hide-details
        @change="$emit('update:enabled', $event)"
        @keydown.native.enter.exact.prevent="$event.repeat || $emit('update:enabled', !enabled)" />
    </div>
    <template v-if="enabled">
      <!-- the field's label for assistive technologies; the design shows
           its text as the placeholder -->
      <label
        v-if="canAddAttribute"
        :for="attributeSearchId"
        class="d-sr-only">
        {{ $t('peopleList.settings.attributeSearch') }}
      </label>
      <v-autocomplete
        v-if="canAddAttribute"
        :id="attributeSearchId"
        ref="attributeSearch"
        v-model="selectedAttribute"
        :items="selectableSettings"
        :placeholder="$t('peopleList.settings.attributeSearch')"
        :no-data-text="$t('peopleList.settings.attributeSearch.noData')"
        :aria-describedby="attributesMissing && attributesMissingId || null"
        item-value="propertyName"
        item-text="label"
        class="people-list-attribute-search mt-2 mb-0 mx-0 pa-0 elevation-0 no-border"
        outlined
        dense
        hide-details
        return-object
        @change="addAttribute">
        <template #item="{item}">
          <v-list-item-content class="people-directory-attribute-suggestion">
            <v-list-item-title>{{ item.label }}</v-list-item-title>
          </v-list-item-content>
        </template>
      </v-autocomplete>
      <!-- why Apply is disabled, read with the field; text, not color -->
      <div
        v-if="attributesMissing"
        :id="attributesMissingId"
        class="people-list-attributes-missing caption error-color mt-1">
        {{ $t('peopleList.settings.attribute.required') }}
      </div>
      <people-list-attribute-card
        v-for="(attribute, index) in value"
        :key="attribute.name"
        :attribute="attribute"
        :label="getLabel(attribute.name)"
        class="mt-4"
        @add-value="addValue(index, $event)"
        @remove-value="removeValue(index, $event)"
        @remove="removeAttribute(index)" />
    </template>
  </div>
</template>
<script>
export default {
  props: {
    // [{name, values: [{value, label}], available}]
    value: {
      type: Array,
      default: () => [],
    },
    enabled: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    settings: [],
    selectedAttribute: null,
    // PeopleDirectoryService.MAX_ATTRIBUTES: more is refused by the portlet
    maxAttributes: 10,
  }),
  computed: {
    switchId() {
      return `${this.$root.appId}AttributeFilterSwitch`;
    },
    attributeSearchId() {
      return `${this.$root.appId}AttributeSearch`;
    },
    attributesMissingId() {
      return `${this.$root.appId}AttributesMissing`;
    },
    canAddAttribute() {
      return this.value.length < this.maxAttributes;
    },
    // ON with no attribute: Apply is disabled
    attributesMissing() {
      return this.enabled && !this.value.length;
    },
    // the attributes viewers can filter on in the advanced filter, but email,
    // which the directory does not filter on, and those already selected
    selectableSettings() {
      const selected = new Set(this.value.map(attribute => attribute.name));
      return this.settings
        .filter(setting => setting.active && setting.visible && setting.parentId === null)
        .filter(setting => setting.propertyName !== 'email' && !selected.has(setting.propertyName))
        .map(setting => ({propertyName: setting.propertyName, label: this.getLabel(setting.propertyName)}));
    },
  },
  created() {
    this.$profileSettingsService.getSettings()
      .then(settings => this.settings = settings?.settings || []);
  },
  methods: {
    getLabel(propertyName) {
      const setting = this.settings.find(s => s.propertyName === propertyName);
      const lang = eXo?.env?.portal?.language || 'en';
      const label = setting?.labels?.find?.(v => v.language === lang)?.label;
      if (label) {
        return label;
      }
      const key = `profileSettings.property.name.${propertyName}`;
      return this.$te(key) ? this.$t(key) : propertyName;
    },
    addAttribute(setting) {
      if (!setting?.propertyName) {
        return;
      }
      this.$emit('input', [...this.value, {name: setting.propertyName, values: [], available: true}]);
      // the field is emptied once the menu closed
      this.$nextTick(() => this.selectedAttribute = null);
    },
    addValue(index, value) {
      const attribute = this.value[index];
      if (attribute.values.some(v => v.value === value.value)) {
        return;
      }
      this.$emit('input', this.value.map((a, i) => (i === index ? {...a, values: [...a.values, value]} : a)));
    },
    removeValue(index, value) {
      this.$emit('input', this.value.map((a, i) => (i === index ? {...a, values: a.values.filter(v => v.value !== value.value)} : a)));
    },
    removeAttribute(index) {
      this.$emit('input', this.value.filter((a, i) => i !== index));
      this.$nextTick(() => this.$refs.attributeSearch?.focus?.());
    },
  },
};
</script>
