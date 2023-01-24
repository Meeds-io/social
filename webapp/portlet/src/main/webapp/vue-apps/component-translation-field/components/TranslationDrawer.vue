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
  <exo-drawer
    ref="drawer"
    v-model="drawer"
    id="translationDrawer"
    allow-expand
    right
    disable-pull-to-refresh>
    <template #title>
      <span class="text-capitalize-first-letter">
        {{ $t(title) }}
      </span>
    </template>
    <template v-if="translations" #content>
      <v-row
        v-for="language in existingLanguages"
        :key="language"
        class="mx-0 mb-0 mt-4 max-width-fit"
        no-gutters>
        <v-col cols="8" class="px-2">
          <v-text-field
            :name="`${language}-translation-value`"
            :value="translations[language]"
            :autofocus="language === defaultLanguage && 'autofocus'"
            class="border-box-sizing pt-0"
            type="text"
            outlined
            dense
            @input="updateValue(language, $event)" />
        </v-col>
        <v-col cols="4">
          <div class="d-flex max-width-fit">
            <div class="flex-grow-1 text-truncate">
              <select
                :disabled="language === defaultLanguage"
                :title="supportedLanguages[language]"
                class="max-width-fit ignore-vuetify-classes my-0"
                @change="changeLanguage(language, $event)">
                <option :value="language">
                  {{ supportedLanguages[language] }}
                </option>
                <option
                  v-for="remainingLanguage in remainingLanguages"
                  :key="remainingLanguage"
                  :value="remainingLanguage">
                  {{ supportedLanguages[remainingLanguage] }}
                </option>
              </select>
            </div>
            <div class="flex-grow-0">
              <v-btn
                v-if="language === defaultLanguage"
                :disabled="!hasRemainingLanguages"
                icon
                @click="addValue">
                <v-icon>fas fa-plus</v-icon>
              </v-btn>
              <v-btn
                v-else
                icon
                @click="removeValue(language)">
                <v-icon>fas fa-minus</v-icon>
              </v-btn>
            </div>
          </div>
        </v-col>
      </v-row>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          class="btn me-2"
          @click="close">
          {{ $t('translationDrawer.cancel') }}
        </v-btn>
        <v-btn
          class="btn btn-primary"
          @click="apply">
          {{ $t('translationDrawer.apply') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    value: {
      type: Object,
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
      type: Object,
      default: null,
    },
  },
  data: () => ({
    drawer: false,
    title: null,
    translations: null,
    existingLanguages: [],
  }),
  computed: {
    languages() {
      return Object.keys(this.supportedLanguages)
        .sort((a, b) => this.supportedLanguages[a].localeCompare(this.supportedLanguages[b]));
    },
    remainingLanguages() {
      return this.languages.slice().filter(lang => this.existingLanguages.indexOf(lang) < 0);
    },
    hasRemainingLanguages() {
      return this.remainingLanguages.length > 0;
    },
  },
  methods: {
    open() {
      this.title = this.drawerTitle || 'translationDrawer.defaultTitle';
      this.translations = this.value && JSON.parse(JSON.stringify(this.value)) || {};
      this.refreshExistingLanguages(true);
      this.$refs.drawer.open();
    },
    apply() {
      this.$emit('input', this.translations);
      this.close();
    },
    close() {
      this.$refs.drawer.close();
      this.translations = null;
    },
    refreshExistingLanguages(sort) {
      if (sort) {
        this.existingLanguages = Object.keys(this.translations).slice()
          .sort((a, b) => {
            if (a === this.defaultLanguage) {
              return -1;
            } else if (b === this.defaultLanguage) {
              return 1;
            } else {
              return this.supportedLanguages[a].localeCompare(this.supportedLanguages[b]);
            }
          });
      } else {
        this.existingLanguages = Object.keys(this.translations).slice();
      }
    },
    addValue() {
      if (this.hasRemainingLanguages) {
        this.translations[this.remainingLanguages[0]] = this.translations[this.defaultLanguage];
        this.refreshExistingLanguages();
      }
    },
    removeValue(language) {
      delete this.translations[language];
      this.refreshExistingLanguages();
    },
    changeLanguage(language, event) {
      const newLanguage = event?.target?.value;
      if (newLanguage && this.supportedLanguages[newLanguage]) {
        this.translations[newLanguage] = this.translations[language];
        delete this.translations[language];
        this.refreshExistingLanguages();
      }
    },
    updateValue(language, value) {
      this.translations[language] = value;
    },
  },
};
</script>
