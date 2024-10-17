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
    class="spaceTemplateNameFormDrawer"
    right>
    <template #title>
      {{ isNew && $t('spaceTemplate.add.drawer.newTemplate') || $t('spaceTemplate.add.drawer.editTemplate') }}
    </template>
    <template v-if="drawer" #content>
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
            required>
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
            rich-editor>
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
    spaceTemplate: null,
    name: null,
    description: null,
    maxNameLength: 50,
    maxDescriptionLength: 100,
    nameTranslations: {},
    descriptionTranslations: {},
  }),
  computed: {
    rules() {
      return {
        name: [
          v => !!v?.length || ' ',
          v => !v?.length || v.length < this.maxNameLength || this.$t('spaceTemplate.nameExceedsMaxLength', {
            0: this.maxNameLength,
          }),
        ],
        description: [
          v => !v?.length || v.length < this.maxDescriptionLength || this.$t('spaceTemplate.descriptionExceedsMaxLength', {
            0: this.maxDescriptionLength,
          }),
        ],
      };
    },
    disabled() {
      return !this.name?.length || this.name.length > this.maxNameLength || (this.description?.length && this.description.length > this.maxDescriptionLength);
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
    open(spaceTemplate) {
      this.isNew = !spaceTemplate?.id;
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
      if (!spaceTemplate) {
        this.name = null;
        this.nameTranslations = null;
        this.description = null;
        this.descriptionTranslations = null;
      }
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    openCharacteristicsDrawer() {
      this.$root.$emit('space-templates-characteristics-open', this.spaceTemplate, this.name, this.nameTranslations, this.description, this.descriptionTranslations);
      this.close();
    },
  },
};
</script>