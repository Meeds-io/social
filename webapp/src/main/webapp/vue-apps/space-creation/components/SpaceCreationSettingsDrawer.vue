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
        :object-id="applicationId"
        object-type="spaceCreation"
        field-name="label"
        :field-value="displayedValue"
        :drawer-title="$t('space.creation.instantiation.settingsDrawer.translateLabel')"
        class="width-auto flex-grow-1 px-5"
        no-expand-icon
        back-icon
        required
        @update:field-value="updateFieldValue"
        @input="translationUpdated" />
      <div class="text-header py-4 px-5">
        {{ $t('space.creation.instantiation.settingsDrawer.content.chooseTemplateTitle') }}
      </div>
      <v-radio-group
        v-model="spaceCreationTemplateChoice"
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
        v-if="spaceCreationTemplateChoice === 'fewTemplates'"
        v-model="selectedTemplates"
        :items="spaceTemplates"
        :placeholder="$t('space.creation.instantiation.settingsDrawer.content.searchTemplatePlaceholder')"
        item-text="name"
        item-value="id"
        class="mt-n2 mb-4 px-5 elevation-0 no-border"
        hide-no-data
        hide-selected
        hide-details
        outlined
        multiple
        return-object
        dense />
      <div v-if="spaceCreationTemplateChoice === 'fewTemplates'" class="px-4">
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
  data() {
    return {
      drawer: false,
      labelTranslations: [],
      language: eXo.env.portal.language,
      spaceCreationTemplateChoice: null,
      selectedTemplates: [],
      loading: false,
      defaultLangValue: this.$t('space.creation.instantiation.create.button'),
      translationsInitialized: false,
      currentLabelTranslations: []
    };
  },
  props: {
    savedSettings: {
      type: Object,
      default: null
    },
    saveSettingsUrl: {
      type: String,
      default: null
    }
  },
  computed: {
    disabled() {
      return (this.savedSettings?.spaceTemplates === this.selectedTemplates && this.savedSettings.spaceCreationTemplateChoice === this.spaceCreationTemplateChoice)
          || JSON.stringify(this.currentLabelTranslations) !== JSON.stringify(this.labelTranslations);
    },
    applicationId() {
      return this.$root.appId;
    },
    displayedValue() {
      return this.labelTranslations?.[this.language] || this.defaultLangValue;
    },
  },
  watch: {
    selectedTemplates() {
      if (!this.selectedTemplates.length) {
        this.spaceTemplates = this.$root.spaceTemplates;
      }
    }
  },
  created() {
    this.$root.$on('space-creation-settings-open', this.open);
  },
  beforeDestroy() {
    this.$root.$off('space-creation-settings-open', this.open);
  },
  methods: {
    translationUpdated(translations) {
      this.labelTranslations = translations;
      if (!this.translationsInitialized) {
        this.currentLabelTranslations = structuredClone(this.labelTranslations);
        this.translationsInitialized = true;
      }
    },
    updateFieldValue(value) {
      this.defaultLangValue = value;
    },
    open() {
      this.restoreSavedSettings();
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    restoreSavedSettings() {
      this.spaceTemplates = this.savedSettings?.spaceTemplates;
      this.spaceCreationTemplateChoice = this.savedSettings?.spaceCreationTemplateChoice;
      if (this.spaceCreationTemplateChoice === 'fewTemplates') {
        this.selectedTemplates = this.spaceTemplates;
      }
    },
    reset() {
      this.restoreSavedSettings();
    },
    removeTemplate(template) {
      this.selectedTemplates = this.selectedTemplates.filter(
        t => t.id !== template.id
      );
    },
    save() {
      this.loading = true;
      const settings = {
        spaceTemplates: this.spaceCreationTemplateChoice === 'fewTemplates' ? this.selectedTemplates : this.spaceTemplates,
        spaceCreationTemplateChoice: this.spaceCreationTemplateChoice
      };
      this.$spaceCreationService.saveSettings(this.saveSettingsUrl , settings).then(() => {
        this.saveLabelTranslations();
        this.$emit('updated', settings);
        this.$root.$emit('alert-message', this.$t('space.creation.instantiation.settingsDrawer.save.success.message'), 'success');
        this.close();
      }).catch(() => {
        this.$root.$emit('alert-message', this.$t('space.creation.instantiation.settingsDrawer.save.error.message'), 'error');
      }).finally(() => this.loading = false);
    },
    async saveLabelTranslations() {
      await this.$translationService.saveTranslations('spaceCreation', this.applicationId, 'label', this.labelTranslations);
      this.currentLabelTranslations = structuredClone(this.labelTranslations);
    },
  },
};
</script>