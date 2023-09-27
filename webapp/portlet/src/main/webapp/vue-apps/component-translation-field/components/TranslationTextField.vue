<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 
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
  <div v-if="translationConfiguration" class="translation-text-field">
    <div v-if="$slots.default || $slots.title" class="d-flex">
      <div v-if="$slots.title" class="flex-grow-1">
        <slot name="title"></slot>
      </div>
      <div class="d-flex me-n1">
        <v-btn
          :title="iconTitle"
          :class="buttonClass"
          class="mt-n1 pt-2px"
          icon
          @click="openDrawer">
          <v-icon size="20" :color="iconColor">fas fa-language</v-icon>
        </v-btn>
      </div>
    </div>
    <slot v-if="$slots.default"></slot>
    <v-text-field
      v-else-if="isI18N"
      :id="id"
      :name="id"
      :placeholder="placeholder"
      :rules="rules || []"
      :value="$t(defaultLanguageValue)"
      :autofocus="autofocus"
      :hide-details="noRulesValidation"
      class="border-box-sizing width-auto pt-0"
      type="text"
      outlined
      readonly
      dense>
      <template v-if="!$slots.title" #append>
        <div class="mt-n2">
          <v-btn
            :title="iconTitle"
            class="my-auto pt-2px"
            icon
            @click="defaultLanguageValue = null">
            <v-icon :color="iconColor">far fa-times-circle</v-icon>
          </v-btn>
        </div>
      </template>
    </v-text-field>
    <v-text-field
      v-else
      v-model="defaultLanguageValue"
      :id="id"
      :name="id"
      :placeholder="placeholder"
      :required="required || null"
      :aria-required="required"
      :autofocus="autofocus"
      :maxlength="maxlength"
      :rules="rules || []"
      :hide-details="noRulesValidation"
      class="border-box-sizing width-auto pt-0"
      type="text"
      outlined
      dense>
      <template #append>
        <div v-if="!$slots.title" class="mt-n2">
          <v-btn
            :title="iconTitle"
            class="my-auto pt-2px"
            icon
            @click="openDrawer">
            <v-icon size="20" :color="iconColor">fas fa-language</v-icon>
          </v-btn>
        </div>
      </template>
    </v-text-field>
    <translation-drawer
      ref="translationDrawer"
      v-model="valuesPerLanguage"
      :object-type="objectType"
      :object-id="objectId"
      :field-name="fieldName"
      :drawer-title="drawerTitle"
      :default-language="defaultLocale"
      :supported-languages="supportedLocales"
      :back-icon="backIcon"
      :max-length="maxlength"
      :rich-editor="richEditor"
      :rich-editor-oembed="richEditorOembed"
      :no-expand-icon="noExpandIcon"
      :rules="rules || []"
      @input="emitUpdateValues" />
  </div>
</template>
<script>
export default {
  props: {
    id: {
      type: String,
      default: null,
    },
    value: {
      type: Object,
      default: null,
    },
    placeholder: {
      type: String,
      default: null,
    },
    drawerTitle: {
      type: String,
      default: null,
    },
    defaultLanguage: {
      type: String,
      default: null,
    },
    supportedLanguages: {
      type: Array,
      default: null,
    },
    objectType: {
      type: String,
      default: null,
    },
    objectId: {
      type: String,
      default: null,
    },
    fieldValue: {
      type: String,
      default: null,
    },
    fieldName: {
      type: String,
      default: null,
    },
    backIcon: {
      type: Boolean,
      default: false,
    },
    autofocus: {
      type: Boolean,
      default: false,
    },
    required: {
      type: Boolean,
      default: false,
    },
    richEditor: {
      type: Boolean,
      default: false,
    },
    richEditorOembed: {
      type: Boolean,
      default: false,
    },
    buttonClass: {
      type: String,
      default: null,
    },
    maxlength: {
      type: Number,
      default: () => 255,
    },
    rules: {
      type: Array,
      default: null,
    },
    noExpandIcon: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    defaultLanguageValue: null,
    valuesPerLanguage: {},
    translationConfiguration: null,
  }),
  computed: {
    isI18N() {
      return this.$te(this.defaultLanguageValue);
    },
    noRulesValidation() {
      return !this.defaultLanguageValue || !this.rules?.length;
    },
    defaultLocale() {
      return this.defaultLanguage || this.translationConfiguration?.defaultLanguage;
    },
    supportedLocales() {
      return this.supportedLanguages || this.translationConfiguration?.supportedLanguages;
    },
    translationsCount() {
      return this.valuesPerLanguage && Object.keys(this.valuesPerLanguage).length || 0;
    },
    iconColor() {
      return this.translationsCount > 1 ? 'primary' : '';
    },
    iconTitle() {
      return this.translationsCount > 1 ? this.$t('translationDrawer.existingTranslationsTooltip', {0: this.translationsCount - 1}) : this.$t('translationDrawer.noTranslationsTooltip');
    },
    serverSideFetch() {
      return this.objectType && this.objectId && this.fieldName;
    },
  },
  watch: {
    value: {
      immediate: true,
      handler: function() {
        this.valuesPerLanguage = this.value && JSON.parse(JSON.stringify(this.value)) || {};
        this.defaultLanguageValue = this.defaultLocale && this.valuesPerLanguage[this.defaultLocale] || '';
      },
    },
    defaultLanguageValue() {
      if (this.defaultLocale && (!this.defaultLanguageValue || this.defaultLanguageValue !== this.valuesPerLanguage[this.defaultLocale])) {
        this.updateTranslationMap();
      }
    },
  },
  created() {
    this.$translationService.getTranslationConfiguration()
      .then(configuration => this.translationConfiguration = configuration)
      .then(() => this.serverSideFetch && this.$translationService.getTranslations(this.objectType, this.objectId, this.fieldName))
      .then(translations => {
        if (this.serverSideFetch && translations && Object.keys(translations).length) {
          this.valuesPerLanguage = translations;
        } else {
          this.valuesPerLanguage = this.value && JSON.parse(JSON.stringify(this.value)) || {};
        }
      })
      .then(() => this.init())
      .then(() => this.$nextTick())
      .finally(() => this.$emit('initialized'));
  },
  methods: {
    init() {
      if (this.valuesPerLanguage[this.defaultLocale]) {
        this.defaultLanguageValue = this.valuesPerLanguage[this.defaultLocale];
      } else {
        this.defaultLanguageValue = this.fieldValue || this.valuesPerLanguage[this.defaultLocale] || '';
      }
      this.updateTranslationMap();
    },
    updateTranslationMap() {
      this.valuesPerLanguage[this.defaultLocale] = this.defaultLanguageValue || '';
      this.$emit('input', this.valuesPerLanguage);
      this.$emit('update:field-value', this.defaultLanguageValue);
    },
    emitUpdateValues() {
      this.defaultLanguageValue = this.valuesPerLanguage[this.defaultLocale];
      this.$emit('input', this.valuesPerLanguage);
      this.$emit('update:field-value', this.defaultLanguageValue);
    },
    openDrawer() {
      this.$refs.translationDrawer.open();
    },
    // To keep, used by parent to update value in case of need
    setValue(value) {
      this.defaultLanguageValue = value;
    },
  },
};
</script>