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
  <exo-drawer
    ref="drawer"
    v-model="drawer"
    :loading="saving"
    class="people-list-settings-drawer"
    right>
    <template #title>
      {{ $t('peopleList.settings.drawer.title') }}
    </template>
    <template v-if="drawer" #content>
      <div :key="openCount" class="pa-5">
        <div class="mb-4 text-header">{{ $t('peopleList.settings.displayOptions') }}</div>
        <div class="d-flex align-center text-start">
          <div class="text-subtitle-1 text-color">
            {{ $t('peopleList.settings.header') }}
          </div>
          <v-spacer />
          <v-switch
            :id="headerSwitchId"
            v-model="headerEnabled"
            :aria-label="$t('peopleList.settings.header')"
            class="people-list-header-switch ma-0 width-fit-content"
            hide-details />
        </div>
        <translation-text-field
          v-if="headerEnabled"
          :id="`${$root.appId}HeaderInput`"
          v-model="headerTranslations"
          :default-language="defaultLanguage"
          :placeholder="$t('peopleList.settings.header.placeholder')"
          :maxlength="maxHeaderLength"
          :required="false"
          drawer-title="peopleList.settings.header"
          class="people-list-header-field width-auto flex-grow-1 mt-2"
          back-icon
          autofocus />
        <div class="d-flex align-center text-start mt-4">
          <div class="text-subtitle-1 text-color">{{ $t('peopleList.settings.pageSize') }}</div>
          <v-spacer />
          <number-input
            v-model="pageSize"
            :label="$t('peopleList.settings.pageSize')"
            :minus-title="$t('peopleList.settings.pageSize.decrease')"
            :plus-title="$t('peopleList.settings.pageSize.increase')"
            :min="minPageSize"
            :max="maxPageSize"
            :step="1"
            class="people-list-page-size ms-4"
            @valid="pageSizeTyped = $event" />
        </div>
        <div class="mt-8 mb-4 text-header">{{ $t('peopleList.settings.filterOptions') }}</div>
        <people-list-attribute-filter
          v-model="attributes"
          :enabled.sync="attributeFilterEnabled" />
      </div>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :disabled="saving"
          class="people-list-settings-cancel btn me-2"
          @click="close">
          {{ $t('peopleList.settings.cancel') }}
        </v-btn>
        <v-tooltip :disabled="!applyTooltip" top>
          <template #activator="{on, attrs}">
            <div
              v-on="on"
              v-bind="attrs">
              <v-btn
                :disabled="!canSave"
                :loading="saving"
                class="people-list-settings-apply btn btn-primary"
                elevation="0"
                @click="save">
                {{ $t('peopleList.settings.apply') }}
              </v-btn>
            </div>
          </template>
          <span>{{ applyTooltip }}</span>
        </v-tooltip>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    saving: false,
    // PeopleDirectoryService MIN/MAX_PAGE_SIZE and MAX_HEADER_LENGTH: a value
    // outside them is refused by the portlet
    minPageSize: 1,
    maxPageSize: 50,
    maxHeaderLength: 150,
    // <number-input> reads its value once: the content is re-keyed per open
    openCount: 0,
    headerEnabled: false,
    headerTranslations: {},
    pageSize: 12,
    // whether the typed text is within the bounds: <number-input> emits the
    // clamped value for an out-of-range text, so its value alone cannot tell
    pageSizeTyped: true,
    attributeFilterEnabled: false,
    // PeopleDirectoryService MAX_ATTRIBUTES, MAX_VALUES_PER_ATTRIBUTE and
    // MAX_VALUE_LENGTH: the portlet refuses more
    maxAttributes: 10,
    maxValues: 20,
    maxValueLength: 150,
    // [{name, values: [{value, label}], available}]
    attributes: [],
  }),
  computed: {
    headerSwitchId() {
      return `${this.$root.appId}HeaderSwitch`;
    },
    // the header labels to store: none when the header is OFF, blank ones dropped
    headerToSave() {
      if (!this.headerEnabled) {
        return {};
      }
      const translations = {};
      Object.keys(this.headerTranslations || {})
        .filter(language => this.headerTranslations[language]?.trim?.()?.length)
        .forEach(language => translations[language] = this.headerTranslations[language]);
      return translations;
    },
    defaultLanguage() {
      return this.$root.defaultLanguage || this.$root.language || 'en';
    },
    // the field the editor fills is the default language's: it is the label
    // every viewer without a translation in their own language sees
    headerMissing() {
      return this.headerEnabled && !this.headerToSave[this.defaultLanguage];
    },
    headerTooLong() {
      return Object.values(this.headerToSave).some(label => label.length > this.maxHeaderLength);
    },
    // the attribute filter to store: none when the toggle is OFF
    attributeFilterToSave() {
      const attributeFilter = {};
      if (this.attributeFilterEnabled) {
        this.attributes.forEach(attribute => attributeFilter[attribute.name] = attribute.values.map(v => v.value));
      }
      return attributeFilter;
    },
    // ON with no attribute or an attribute without value
    attributeValuesMissing() {
      return this.attributeFilterEnabled
        && (!this.attributes.length || this.attributes.some(attribute => !attribute.values.length));
    },
    // an attribute no longer available, which the portlet refuses
    attributeUnavailable() {
      return this.attributeFilterEnabled && this.attributes.some(attribute => !attribute.available);
    },
    // more attributes or values than the directory accepts, or a blank or
    // too long value: only another writer of the window stores those
    attributeFilterRefused() {
      return this.attributeFilterEnabled
        && (this.attributes.length > this.maxAttributes
          || this.attributes.some(attribute => attribute.values.length > this.maxValues
            || attribute.values.some(v => !v.value?.trim?.() || v.value.length > this.maxValueLength)));
    },
    attributeFilterIncomplete() {
      return this.attributeValuesMissing || this.attributeUnavailable || this.attributeFilterRefused;
    },
    applyTooltip() {
      if (this.headerMissing) {
        return this.$t('peopleList.settings.header.required');
      } else if (this.attributeUnavailable) {
        return this.$t('peopleList.settings.attribute.unavailable');
      } else if (this.attributeFilterRefused) {
        return this.$t('peopleList.settings.attribute.valuesRefused');
      } else if (this.attributeValuesMissing) {
        return this.$t('peopleList.settings.attribute.valuesRequired');
      }
      return null;
    },
    validPageSize() {
      return this.pageSizeTyped
        && Number.isInteger(this.pageSize) && this.pageSize >= this.minPageSize && this.pageSize <= this.maxPageSize;
    },
    // the settings the form would store
    toSave() {
      return {
        headerTranslations: this.headerToSave,
        pageSize: this.pageSize,
        attributeFilter: this.attributeFilterToSave,
      };
    },
    // nothing to apply while the form holds the stored settings
    changed() {
      return !this.isStored(this.toSave);
    },
    canSave() {
      return !this.saving
        && this.changed
        && !this.headerMissing
        && !this.headerTooLong
        && !this.attributeFilterIncomplete
        && this.validPageSize;
    },
  },
  created() {
    this.$root.$on('people-list-settings-open', this.open);
  },
  beforeDestroy() {
    this.$root.$off('people-list-settings-open', this.open);
  },
  methods: {
    open() {
      // the labels are strings: a shallow copy detaches the form from the root
      const headerTranslations = {...(this.$root.headerTranslations || {})};
      this.headerEnabled = Object.keys(headerTranslations).length > 0;
      this.headerTranslations = headerTranslations;
      this.pageSize = this.$root.pageSize;
      this.pageSizeTyped = true;
      // a value stored twice is stored once by the portlet
      this.attributes = (this.$root.attributes || []).map(attribute => ({
        ...attribute,
        values: (attribute.values || []).filter((v, i, values) => values.findIndex(other => other.value === v.value) === i),
      }));
      this.attributeFilterEnabled = this.attributes.length > 0;
      this.openCount++;
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    /**
     * Posts the preferences (header, page size, attribute filter), then
     * re-reads what the portlet stored: a
     * refusal of the action (edit right lost, value rejected) is answered
     * with a 200, so the snackbar follows the stored values, never the HTTP
     * status.
     *
     * @returns {Promise<void>|undefined} the save request, if the form is savable
     */
    save() {
      if (!this.canSave) {
        return;
      }
      this.saving = true;
      const toSave = this.toSave;
      const body = new URLSearchParams({
        headerTranslations: JSON.stringify(toSave.headerTranslations),
        pageSize: String(toSave.pageSize),
        attributeFilter: JSON.stringify(toSave.attributeFilter),
      }).toString();
      return fetch(this.$root.saveSettingsUrl, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body,
      })
        .then(() => this.$root.refreshSettings())
        .then(() => {
          if (this.isStored(toSave)) {
            this.$root.$emit('alert-message', this.$t('peopleList.settings.saved.success'), 'success');
            this.$root.$emit('people-list-directory-updated');
            this.close();
          } else {
            this.$root.$emit('alert-message', this.$t('peopleList.settings.saved.error'), 'error');
          }
        })
        .catch(() => this.$root.$emit('alert-message', this.$t('peopleList.settings.saved.error'), 'error'))
        .finally(() => this.saving = false);
    },
    isStored(toSave) {
      const storedAttributeFilter = {};
      (this.$root.attributes || []).forEach(attribute => storedAttributeFilter[attribute.name] = attribute.values.map(v => v.value));
      return this.canonical({
        headerTranslations: this.$root.headerTranslations,
        pageSize: this.$root.pageSize,
        attributeFilter: storedAttributeFilter,
      }) === this.canonical(toSave);
    },
    // a key-order-independent rendering: the stored translations come back
    // from the server in another order than they were posted
    canonical(value) {
      if (value === null || typeof value !== 'object' || Array.isArray(value)) {
        return JSON.stringify(value);
      }
      return JSON.stringify(Object.keys(value).sort((a, b) => a.localeCompare(b)).map(key => [key, this.canonical(value[key])]));
    },
  },
};
</script>
