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
  <div>
    <div class="mt-4 mb-2 text-header">{{ $t('spacesList.settings.filterList') }}</div>
    <div class="ms-n1 d-flex align-center">
      <v-radio-group
        v-model="localSettings.filterType"
        class="pa-0 ma-0 full-width"
        mandatory>
        <v-radio
          :label="$t('spacesList.settings.anySpace')"
          value="any"
          @click="resetFilterType" />
        <v-radio
          :label="$t('spacesList.settings.perTemplate')"
          value="template"
          @click="resetFilterType" />
        <template v-if="localSettings.filterType === 'template'">
          <v-autocomplete
            v-model="templateId"
            :items="enabledTemplates"
            :placeholder="$t('spacesList.settings.searchTemplatePlaceholder')"
            class="mb-2 mx-0 pa-0 elevation-0 no-border"
            item-text="name"
            item-value="id"
            hide-no-data
            hide-selected
            hide-details
            outlined
            dense />
          <div class="d-flex flex-wrap mb-2">
            <v-chip
              v-for="(t, index) in selectedTemplates"
              :key="t.id"
              max-width="150"
              color="primary"
              class="me-2"
              dense>
              <div class="d-flex align-center text-truncate">
                <v-icon class="me-2" size="24">{{ t.icon }}</v-icon>
                <div class="text-truncate">{{ t.name }}</div>
                <v-btn
                  icon
                  class="me-n2"
                  @click="removeItem(index, localSettings.templateIds)">
                  <v-icon size="18" color="white">fa-times</v-icon>
                </v-btn>
              </div>
            </v-chip>
          </div>
        </template>
        <v-radio
          :label="$t('spacesList.settings.perCategory')"
          value="category"
          @click="resetFilterType" />
      </v-radio-group>
    </div>
    <template v-if="localSettings.filterType === 'category'">
      <v-card class="ms-4" flat>
        <!-- Include Categories -->
        <v-checkbox
          v-model="filterPerCategories"
          :label="$t('spacesList.settings.includeCategory')"
          class="mt-0" />
        <div v-if="filterPerCategories" class="mt-4">
          <category-suggester
            v-model="categoryId"
            class="mt-n2 mb-4 mx-0 pa-0"
            label=""
            access-permission />
          <div class="mb-2">{{ $t('spacesList.settings.categoryListSortTitle') }}</div>
          <v-list class="pa-0" dense>
            <v-list-item
              v-for="(c, index) in selectedCategories"
              :key="c.id"
              class="pa-0"
              dense>
              <v-list-item-icon class="me-2 my-auto">
                <v-icon size="24">{{ c.icon }}</v-icon>
              </v-list-item-icon>
              <v-list-item-content class="me-2 pa-0 text-truncate">
                <v-list-item-title class="text-truncate">{{ c.name }}</v-list-item-title>
              </v-list-item-content>
              <v-list-item-action :class="index === selectedCategories.length - 1 && 'invisible'">
                <v-btn icon @click="moveDown(index, localSettings.categoryIds)">
                  <v-icon size="18">fa-arrow-down</v-icon>
                </v-btn>
              </v-list-item-action>
              <v-list-item-action :class="index === 0 && 'invisible'">
                <v-btn icon @click="moveUp(index, localSettings.categoryIds)">
                  <v-icon size="18">fa-arrow-up</v-icon>
                </v-btn>
              </v-list-item-action>
              <v-list-item-action>
                <v-btn icon @click="removeItem(index, localSettings.categoryIds)">
                  <v-icon size="18" color="error">fa-trash</v-icon>
                </v-btn>
              </v-list-item-action>
            </v-list-item>
          </v-list>
        </div>
        <!-- Exclude Categories -->
        <v-checkbox
          v-model="filterPerExcludeCategories"
          :label="$t('spacesList.settings.excludeCategory')"
          class="mt-0" />
        <div v-if="filterPerExcludeCategories" class="mt-4">
          <category-suggester
            v-model="excludedCategoryId"
            class="mt-n2 mb-4 mx-0 pa-0"
            label=""
            access-permission />
          <v-list class="pa-0" dense>
            <v-list-item
              v-for="(c, index) in selectedExcludeCategories"
              :key="c.id"
              class="pa-0"
              dense>
              <v-list-item-icon class="me-2 my-auto">
                <v-icon size="24">{{ c.icon }}</v-icon>
              </v-list-item-icon>
              <v-list-item-content class="me-2 pa-0 text-truncate">
                <v-list-item-title class="text-truncate">{{ c.name }}</v-list-item-title>
              </v-list-item-content>
              <v-list-item-action>
                <v-btn icon @click="removeItem(index, localSettings.excludeCategoryIds)">
                  <v-icon size="18" color="error">fa-trash</v-icon>
                </v-btn>
              </v-list-item-action>
            </v-list-item>
          </v-list>
        </div>
      </v-card>
    </template>
  </div>
</template>

<script>
export default {
  props: {
    value: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      localSettings: {},
      updatingFromParent: false,
      templateId: null,
      categoryId: null,
      excludedCategoryId: null,
      filterPerCategories: false,
      filterPerExcludeCategories: false,
      selectedCategories: [],
      selectedExcludeCategories: [],
      selectedTemplates: []
    };
  },
  computed: {
    enabledTemplates() {
      return this.$root.spaceTemplates?.filter(
        t => t.enabled && !t.deleted && !this.selectedTemplates.find(st => st.id === t.id)
      );
    }
  },
  watch: {
    value: {
      deep: true,
      immediate: true,
      async handler() {
        this.updatingFromParent = true;
        this.localSettings = structuredClone(this.value);
        await this.$nextTick();

        await this.loadCategories();
        await this.loadExcludeCategories();
        await this.refreshSpaceTemplates();
        this.loadTemplates();
        this.filterPerCategories = !!this.localSettings.categoryIds?.length;
        this.filterPerExcludeCategories = !!this.localSettings.excludeCategoryIds?.length;
        this.updatingFromParent = false;
      }
    },
    localSettings: {
      deep: true,
      handler() {
        if (this.updatingFromParent) {
          return;
        }
        this.$emit('input', structuredClone(this.localSettings));
      }
    },
    async categoryId() {
      if (this.categoryId && !this.localSettings.categoryIds.includes(this.categoryId)) {
        this.localSettings.categoryIds.push(this.categoryId);
        this.categoryId = null;
        await this.loadCategories();
      }
    },
    async excludedCategoryId() {
      if (this.excludedCategoryId && !this.localSettings?.excludeCategoryIds?.includes?.(this.excludedCategoryId)) {
        this.localSettings.excludeCategoryIds.push(this.excludedCategoryId);
        this.excludedCategoryId = null;
        await this.loadExcludeCategories();
      }
    },
    templateId() {
      if (this.templateId && !this.localSettings.templateIds.includes(this.templateId)) {
        this.localSettings.templateIds.push(this.templateId);
        this.templateId = null;
        this.loadTemplates();
      }
    },
    'localSettings.categoryIds': 'loadCategories',
    'localSettings.excludeCategoryIds': 'loadExcludeCategories',
    'localSettings.templateIds': 'loadTemplates',
  },
  async mounted() {
    await this.refreshSpaceTemplates();
  },
  methods: {
    resetFilterType() {
      this.localSettings.templateIds = [];
      this.localSettings.categoryIds = [];
    },
    removeItem(index, array) {
      array.splice(index, 1);
    },
    moveUp(index, array) {
      if (index === 0) {return;}
      const item = array.splice(index, 1)[0];
      array.splice(index - 1, 0, item);
    },
    moveDown(index, array) {
      if (index === array.length - 1) {return;}
      const item = array.splice(index, 1)[0];
      array.splice(index + 1, 0, item);
    },
    async loadCategoriesByIds(ids) {
      if (!Array.isArray(ids) || ids.length === 0) {
        return [];
      }
      const categories = await Promise.all(
        ids.map(id => this.$categoryService.getCategory(id).catch(() => null))
      );
      return categories.filter(Boolean);
    },
    async loadCategories() {
      this.selectedCategories = await this.loadCategoriesByIds(this.localSettings?.categoryIds);
    },
    async loadExcludeCategories() {
      this.selectedExcludeCategories = await this.loadCategoriesByIds(this.localSettings?.excludeCategoryIds);
    },
    async refreshSpaceTemplates() {
      if (!this.$root.spaceTemplates) {
        this.$root.spaceTemplates = await this.$spaceTemplateService.getSpaceTemplates(true);
      }
    },
    loadTemplates() {
      if (!Array.isArray(this.localSettings.templateIds)) {
        this.selectedTemplates = [];
        return;
      }
      const templates = this.localSettings.templateIds.map(id =>
        this.$root.spaceTemplates?.find(t => t.id === id)
      );
      this.selectedTemplates = templates.filter(Boolean);
    }
  }
};
</script>
