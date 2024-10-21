<!--

  This file is part of the Meeds project (https://meeds.io/).

  Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

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
    :confirm-close="modified"
    :confirm-close-labels="closeConfirmLabels"
    class="spaceTemplateNameFormDrawer"
    right>
    <template #title>
      {{ isNew && $t('spaceTemplate.add.drawer.newTemplate') || $t('spaceTemplate.add.drawer.editTemplate') }}
    </template>
    <template v-if="drawer && spaceTemplate" #content>
      <div class="pa-4" flat>
        <div class="d-flex">
          <translation-text-field
            id="spaceTemplateName"
            v-model="nameTranslations"
            :field-value.sync="name"
            :placeholder="$t('spaceTemplate.namePlaceholder')"
            :maxlength="maxNameLength"
            :object-id="spaceTemplateId"
            :rules="rules.name"
            object-type="spaceTemplate"
            field-name="name"
            drawer-title="spaceTemplate.nameDrawerTitle"
            class="width-auto flex-grow-1 pb-1"
            back-icon
            required
            @initialized="setOriginalInfo">
            <template #title>
              {{ $t('spaceTemplate.nameLabel') }}
            </template>
          </translation-text-field>
        </div>
        <div class="pa-0">
          <translation-text-field
            ref="descriptionTranslation"
            v-model="descriptionTranslations"
            :field-value.sync="description"
            :maxlength="maxDescriptionLength"
            :object-id="spaceTemplateId"
            :rules="rules.description"
            object-type="spaceTemplate"
            field-name="description"
            drawer-title="spaceTemplate.descriptionDrawerTitle"
            class="ma-1px mt-4"
            back-icon
            rich-editor
            @initialized="setOriginalInfo">
            <template #title>
              {{ $t('spaceTemplate.descriptionLabel') }}
            </template>
            <rich-editor
              id="spaceTemplateDescription"
              ref="spaceTemplateDescriptionEditor"
              v-model="description"
              :placeholder="$t('spaceTemplate.descriptionPlaceholder')"
              :max-length="maxDescriptionLength"
              :tag-enabled="false"
              ck-editor-type="spaceTemplateDescription" />
          </translation-text-field>
          <font-icon-input
            v-model="spaceTemplate.icon"
            class="mt-4" />
          <div class="d-flex align-center justify-center mt-4">
            <v-btn
              :disabled="disabled"
              class="btn btn-primary"
              @click="openCharacteristicsDrawer">
              {{ isNew && $t('spaceTemplate.start') || $t('spaceTemplate.next') }}
            </v-btn>
          </div>
        </div>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    isNew: false,
    initialized: false,
    characteristicsInformationModified: false,
    spaceTemplate: null,
    bannerUploadId: null,
    bannerData: null,
    name: null,
    description: null,
    maxNameLength: 50,
    maxDescriptionLength: 1300,
    nameTranslations: {},
    originalNameTranslations: null,
    descriptionTranslations: {},
    originalDescriptionTranslations: null,
  }),
  computed: {
    rules() {
      return {
        name: [
          v => !!v?.length || ' ',
          v => !v?.length || v.length <= this.maxNameLength || this.$t('spaceTemplate.nameExceedsMaxLength', {
            0: this.maxNameLength,
          }),
        ],
        description: [
          v => !v?.length || this.$utils.htmlToText(v).length <= this.maxDescriptionLength || this.$t('spaceTemplate.descriptionExceedsMaxLength', {
            0: this.maxDescriptionLength,
          }),
        ],
      };
    },
    descriptionText() {
      return this.description && this.$utils.htmlToText(this.description) || '';
    },
    modified() {
      return this.characteristicsInformationModified
        || (JSON.stringify(this.originalNameTranslations) !== JSON.stringify(this.nameTranslations))
        || (JSON.stringify(this.originalDescriptionTranslations) !== JSON.stringify(this.descriptionTranslations));
    },
    closeConfirmLabels() {
      return {
        title: this.$t('spaceTemplate.closeConfirmLabels.title'),
        message: this.$t('spaceTemplate.closeConfirmLabels.message'),
        ok: this.$t('spaceTemplate.closeConfirmLabels.ok'),
        cancel: this.$t('spaceTemplate.closeConfirmLabels.cancel'),
      };
    },
    disabled() {
      return !this.name?.length
          || this.name.length > this.maxNameLength
          || this.descriptionText.length > this.maxDescriptionLength;
    },
    spaceTemplateId() {
      return this.spaceTemplate?.id;
    },
  },
  watch: {
    description() {
      if (this.$refs.descriptionTranslation) {
        this.$refs.descriptionTranslation.setValue(this.description);
      }
      if (this.$refs.spaceTemplateDescriptionEditor?.editor && this.description !== this.$refs.spaceTemplateDescriptionEditor.inputVal) {
        this.$refs.spaceTemplateDescriptionEditor.editor.setData(this.description);
      }
    },
  },
  created() {
    this.$root.$on('space-templates-name-open', this.open);
  },
  beforeDestroy() {
    this.$root.$off('space-templates-name-open', this.open);
  },
  methods: {
    open(spaceTemplate, name, nameTranslations, description, descriptionTranslations, modified, bannerUploadId, bannerData) {
      this.isNew = !spaceTemplate?.id;
      this.characteristicsInformationModified = modified;
      this.spaceTemplate = spaceTemplate || {
        name: null,
        description: null,
        bannerFileId: null,
        icon: 'fa-people-arrows',
        enabled: true,
        order: 0,
        permissions: this.$root.usersPermission,
        spaceLayoutPermissions: this.$root.administratorsPermission,
        spaceDeletePermissions: this.$root.administratorsPermission,
        spaceFields: ['name', 'invitation', 'properties', 'access'],
        spaceDefaultVisibility: 'PRIVATE',
        spaceDefaultRegistration: 'OPEN',
        spaceAllowContentCreation: true,
      };
      this.bannerUploadId = bannerUploadId;
      this.bannerData = bannerData;
      this.name = name || spaceTemplate.name || null;
      this.nameTranslations = nameTranslations || null;
      this.description = description || null;
      this.descriptionTranslations = descriptionTranslations || null;
      this.initialized = false;
      this.$refs.drawer.open();
    },
    setOriginalInfo() {
      if (!this.initialized) {
        this.originalNameTranslations = JSON.parse(JSON.stringify(this.nameTranslations));
        this.originalDescriptionTranslations = JSON.parse(JSON.stringify(this.descriptionTranslations));
      }
    },
    async close() {
      this.spaceTemplate = null;
      this.nameTranslations = null;
      this.descriptionTranslations = null;
      this.originalNameTranslations = null;
      this.originalDescriptionTranslations = null;
      this.characteristicsInformationModified = false;
      await this.$nextTick();
      this.$refs.drawer.close();
    },
    openCharacteristicsDrawer() {
      this.$root.$emit('space-templates-characteristics-open', this.spaceTemplate, this.name, this.nameTranslations, this.description, this.descriptionTranslations, this.modified, this.bannerUploadId, this.bannerData);
      this.close();
    },
  },
};
</script>