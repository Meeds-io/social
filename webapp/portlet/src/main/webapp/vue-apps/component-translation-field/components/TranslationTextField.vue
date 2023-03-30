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
  <div class="translation-text-field">
    <v-text-field
      v-if="isI18N"
      :id="id"
      :name="id"
      :placeholder="placeholder"
      :value="$t(defaultLanguageValue)"
      class="border-box-sizing pt-0"
      type="text"
      outlined
      readonly
      dense>
      <template #append>
        <v-btn
          class="mt-n2 pt-2px"
          icon
          @click="defaultLanguageValue = null">
          <v-icon :color="iconColor">far fa-times-circle</v-icon>
        </v-btn>
      </template>
    </v-text-field>
    <v-text-field
      v-else
      v-model="defaultLanguageValue"
      :id="id"
      :name="id"
      :placeholder="placeholder"
      :required="required"
      :aria-required="required"
      class="border-box-sizing pt-0"
      type="text"
      outlined
      dense>
      <template #append>
        <v-btn
          :title="iconTitle"
          class="mt-n2 pt-2px"
          icon
          @click="openDrawer">
          <v-icon :color="iconColor">fas fa-language</v-icon>
        </v-btn>
      </template>
    </v-text-field>
    <translation-drawer
      ref="translationDrawer"
      v-model="valuesPerLanguage"
      :drawer-title="drawerTitle"
      :default-language="defaultLanguage"
      :supported-languages="supportedLanguages"
      @input="$emit('input', $event)" />
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
    required: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    defaultLanguageValue: null,
    valuesPerLanguage: {},
  }),
  computed: {
    isI18N() {
      return this.$te(this.defaultLanguageValue);
    },
    translationsCount() {
      return Object.keys(this.valuesPerLanguage).length;
    },
    iconColor() {
      return this.translationsCount > 1 ? 'primary' : '';
    },
    iconTitle() {
      return this.translationsCount > 1 ? this.$t('translationDrawer.existingTranslationsTooltip', {0: this.translationsCount - 1}) : this.$t('translationDrawer.noTranslationsTooltip');
    },
  },
  watch: {
    value: {
      immediate: true,
      handler: function() {
        this.valuesPerLanguage = this.value && JSON.parse(JSON.stringify(this.value)) || {};
        this.defaultLanguageValue = this.valuesPerLanguage[this.defaultLanguage] || '';
      },
    },
    defaultLanguageValue() {
      if (this.defaultLanguageValue !== this.valuesPerLanguage[this.defaultLanguage]) {
        this.valuesPerLanguage[this.defaultLanguage] = this.defaultLanguageValue;
        this.$emit('input', this.valuesPerLanguage);
      }
    },
  },
  methods: {
    openDrawer() {
      this.$refs.translationDrawer.open();
    },
  },
};
</script>