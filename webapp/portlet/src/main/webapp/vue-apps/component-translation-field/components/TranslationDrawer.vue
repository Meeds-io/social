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
    :go-back-button="backIcon"
    :allow-expand="!noExpandIcon"
    right
    fixed
    disable-pull-to-refresh
    @closed="reset">
    <template #title>
      {{ $t(title) }}
    </template>
    <template v-if="translations" #content>
      <v-form ref="form">
        <v-row
          v-for="(language, index) in existingLanguages"
          :key="language"
          class="mx-0 mb-0 mt-4 max-width-fit"
          no-gutters>
          <v-col cols="8" class="px-2">
            <v-card
              v-if="richEditor"
              class="text-truncate border-color rounder full-height pe-2 d-flex align-center"
              flat
              @click="openOrCloseEditor(index)">
              <v-btn
                icon
                @click.prevent.stop="openOrCloseEditor(index)">
                <v-icon size="24">{{ editorIndex === index && 'fa-chevron-up' || 'fa-chevron-down' }}</v-icon>
              </v-btn>
              <v-card
                v-if="editorIndex !== index"
                max-height="2em"
                v-sanitized-html="translations[language] || ''"
                class="d-flex text-truncate full-width mt-2"
                flat />
            </v-card>
            <v-text-field
              v-else
              :name="`${language}-translation-value`"
              :value="translations[language]"
              :autofocus="language === defaultLanguage && 'autofocus'"
              :disabled="loading"
              :rules="rules || []"
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
          <v-col
            v-if="richEditor && editorIndex === index"
            class="px-2 py-4"
            cols="12">
            <rich-editor
              ref="translationEditor"
              :value="translations[language]"
              :max-length="maxLength"
              :tag-enabled="false"
              :oembed="richEditorOembed"
              autofocus
              @input="updateValue(language, $event)" />
          </v-col>
        </v-row>
      </v-form>
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
          :loading="loading"
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
    objectType: {
      type: String,
      default: null,
    },
    objectId: {
      type: String,
      default: null,
    },
    fieldName: {
      type: String,
      default: null,
    },
    maxLength: {
      type: Number,
      default: () => 255,
    },
    save: {
      type: Boolean,
      default: false,
    },
    backIcon: {
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
    noExpandIcon: {
      type: Boolean,
      default: false,
    },
    rules: {
      type: Array,
      default: null,
    },
  },
  data: () => ({
    drawer: false,
    loading: false,
    title: null,
    translations: null,
    editorIndex: null,
    existingLanguages: [],
  }),
  computed: {
    languages() {
      return Object.keys(this.supportedLanguages)
        .sort((a, b) => this.supportedLanguages[a].localeCompare(this.supportedLanguages[b]));
    },
    remainingLanguages() {
      return this.languages && this.languages.slice().filter(lang => this.existingLanguages.indexOf(lang) < 0) || [];
    },
    hasRemainingLanguages() {
      return this.remainingLanguages.length > 0;
    },
    serverSideFetch() {
      return this.objectType && this.objectId && this.fieldName;
    },
  },
  watch: {
    loading() {
      if (this.loading) {
        this.$refs.drawer.startLoading();
      } else {
        this.$refs.drawer.endLoading();
      }
    },
  },
  methods: {
    open() {
      this.title = this.drawerTitle || 'translationDrawer.defaultTitle';
      this.editorIndex = null;
      this.translations = this.value && JSON.parse(JSON.stringify(this.value)) || {};
      this.refreshExistingLanguages(true);
      this.$refs.drawer.open();
    },
    apply() {
      if (!this.$refs.form.validate()) {
        return;
      }
      if (this.serverSideFetch && this.save) {
        this.loading = true;
        this.$translationService.saveTranslations(this.objectType, this.objectId, this.fieldName, this.translations)
          .then(() => {
            this.$emit('input', this.translations);
            this.close();
          })
          .finally(() => this.loading = false);
      } else {
        this.$emit('input', this.translations);
        this.close();
      }
    },
    close() {
      this.$refs.drawer.close();
    },
    reset() {
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
    openOrCloseEditor(index) {
      if (this.editorIndex === index) {
        this.editorIndex = null;
      } else {
        this.editorIndex = index;
      }
    },
  },
};
</script>
