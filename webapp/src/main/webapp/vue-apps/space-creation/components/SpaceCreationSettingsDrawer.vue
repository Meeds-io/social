<!--

  This file is part of the Meeds project (https://meeds.io/).

  Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

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
    @closed="reset"
    right>
    <template #title>
      {{ $t('space.creation.instantiation.settingsDrawer.title') }}
    </template>
    <template #content>
      <div class="text-header py-4 px-5">
        {{ $t('space.creation.instantiation.settingsDrawer.content.updateBtnTitle') }}
      </div>
      <translation-text-field
        v-model="settings.labelTranslations"
        :maxlength="maxLabelLength"
        :rules="rules.label"
        drawer-title="space.creation.instantiation.settingsDrawer.translateLabel"
        class="width-auto flex-grow-1 px-5"
        back-icon />
      <div class="text-header py-4 px-5">
        {{ $t('space.creation.instantiation.settingsDrawer.content.chooseTemplateTitle') }}
      </div>
      <v-radio-group
        v-model="settings.spaceCreationTemplateChoice"
        class="mt-0 px-5"
        mandatory>
        <v-radio value="anyTemplate">
          <template #label>
            <span class="ms-1"> {{ $t('space.creation.instantiation.settingsDrawer.content.chooseTemplateChoice1') }}</span>
          </template>
        </v-radio>
        <v-radio value="fewTemplates">
          <template #label>
            <span class="ms-1"> {{ $t('space.creation.instantiation.settingsDrawer.content.chooseTemplateChoice2') }}</span>
          </template>
        </v-radio>
      </v-radio-group>
      <v-autocomplete
        v-if="settings.spaceCreationTemplateChoice === 'fewTemplates'"
        v-model="search"
        :items="filteredTemplates"
        :placeholder="$t('space.creation.instantiation.settingsDrawer.content.searchTemplatePlaceholder')"
        item-text="name"
        item-value="id"
        class="mt-n2 mb-4 px-5 elevation-0 no-border"
        hide-no-data
        outlined
        dense
        @change="addTemplate" />
      <div v-if="settings.spaceCreationTemplateChoice === 'fewTemplates'" class="px-4">
        <v-chip
          v-for="template in selectedTemplates"
          :key="template.id"
          class="my-1 me-3"
          color="primary"
          close
          @click:close="removeTemplate(template)">
          {{ template.name }}
        </v-chip>
      </div>
    </template>
    <template #footer>
      <div class="d-flex align-center">
        <v-btn
          class="btn ms-auto me-2"
          @click="reset">
          {{ $t('space.creation.instantiation.cancel.button') }}
        </v-btn>
        <v-btn
          :disabled="disabled"
          :loading="loading"
          class="btn primary"
          @click="save">
          {{ $t('space.creation.instantiation.apply.button') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    labelTranslations: [],
    selectedTemplates: [],
    loading: false,
    maxLabelLength: 150,
    settings: {},
    originalSettings: {},
    search: null,
  }),
  computed: {
    filteredTemplates() {
      if (this.search) {
        return this.$root.spaceTemplates.filter(
          t => t.name.toLowerCase().includes(this.search.toLowerCase()));
      } else {
        return this.$root.spaceTemplates;
      }
    },
    modified() {
      return (this.settings.spaceCreationTemplateChoice !== this.originalSettings.spaceCreationTemplateChoice
           && JSON.stringify(this.settings.spaceTemplates) !== JSON.stringify(this.originalSettings.spaceTemplates))
          || JSON.stringify(this.settings) !== JSON.stringify(this.originalSettings);
    },
    disabled() {
      return !this.modified || Object.keys(this.settings.labelTranslations).some(k => this.settings.labelTranslations[k]?.length > this.maxLabelLength)
      || (this.settings.spaceCreationTemplateChoice === 'fewTemplates' && !this.selectedTemplates.length);
    },
    rules() {
      return {
        label: [
          v => !!v?.length || ' ',
          v => !v?.length || v.length <= this.maxLabelLength || this.$t('space.creation.instantiation.settingsDrawer.labelExceedsMaxLength', {
            0: this.maxLabelLength,
          }),
        ],
      };
    },
  },
  watch: {
    settings() {
      if (Object.keys(this.settings.labelTranslations).length === 0) {
        this.settings.labelTranslations = {[eXo.env.portal.defaultLanguage]: this.$t('space.creation.instantiation.create.button')};
        this.originalSettings.labelTranslations = {[eXo.env.portal.defaultLanguage]: this.$t('space.creation.instantiation.create.button')};
      }
    },
  },
  created() {
    this.$root.$on('space-creation-settings-open', this.open);
  },
  beforeDestroy() {
    this.$root.$off('space-creation-settings-open', this.open);
  },
  methods: {
    addTemplate(id) {
      const template = this.$root.spaceTemplates.find(t => t.id === id);
      if (template && !this.selectedTemplates.find(t => t.id === template.id)) {
        this.selectedTemplates.push(template);
      }
      this.search = null;
    },
    open() {
      this.restoreSavedSettings();
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    restoreSavedSettings() {
      this.settings = JSON.parse(JSON.stringify(this.$root.settings));
      this.originalSettings = JSON.parse(JSON.stringify(this.settings));
      if (this.settings.spaceCreationTemplateChoice === 'fewTemplates') {
        this.selectedTemplates = this.settings.spaceTemplates;
      }
    },
    reset() {
      this.close();
      this.restoreSavedSettings();
    },
    removeTemplate(template) {
      this.selectedTemplates = this.selectedTemplates.filter(
        t => t.id !== template.id
      );
    },
    save() {
      this.loading = true;
      this.settings.spaceTemplates = this.settings.spaceCreationTemplateChoice === 'anyTemplate' ? this.$root.spaceTemplates : this.selectedTemplates;
      this.$spaceCreationService.saveSettings(this.$root.saveSettingsUrl , this.settings).then(() => {
        this.$emit('updated', this.settings);
        this.$root.settings = this.settings;
        this.$root.$emit('alert-message', this.$t('space.creation.instantiation.settingsDrawer.save.success.message'), 'success');
        this.close();
      }).catch(() => {
        this.$root.$emit('alert-message', this.$t('space.creation.instantiation.settingsDrawer.save.error.message'), 'error');
      }).finally(() => this.loading = false);
    },
  },
};
</script>