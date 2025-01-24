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
    id="SpacesListSettingsDrawer"
    ref="drawer"
    v-model="drawer"
    :loading="saving"
    allow-expand
    right>
    <template #title>
      {{ $t('spacesList.settings.drawer.title') }}
    </template>
    <template v-if="drawer" #content>
      <div class="pa-5" flat>
        <div class="mb-2 text-header">{{ $t('spacesList.settings.displayOptions') }}</div>
        <v-radio-group
          v-model="settings.hideQuickActions"
          class="pa-0 mb-2 mt-0 ms-n1"
          mandatory>
          <v-radio :value="false">
            <template #label>
              <v-list-item class="pa-0" dense>
                <v-list-item-content class="pa-0">
                  <v-list-item-title>
                    {{ $t('spacesList.settings.displayOptions.option1.title') }}
                  </v-list-item-title>
                  <v-list-item-subtitle>
                    {{ $t('spacesList.settings.displayOptions.option1.subtitle') }}
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
            </template>
          </v-radio>
          <v-radio :value="true">
            <template #label>
              <v-list-item class="pa-0" dense>
                <v-list-item-content class="pa-0">
                  <v-list-item-title>
                    {{ $t('spacesList.settings.displayOptions.option2.title') }}
                  </v-list-item-title>
                  <v-list-item-subtitle>
                    {{ $t('spacesList.settings.displayOptions.option2.subtitle') }}
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
            </template>
          </v-radio>
        </v-radio-group>
        <div v-if="settings.hideQuickActions" class="d-flex mb-4">
          <translation-text-field
            v-model="settings.nameTranslations"
            :placeholder="$t('spacesList.settings.namePlaceholder')"
            :maxlength="maxNameLength"
            :rules="rules.name"
            :required="false"
            drawer-title="spacesList.settings.nameTranslationTitle"
            class="width-auto flex-grow-1 pb-1"
            back-icon />
        </div>
        <div class="mb-2 text-header">{{ $t('spacesList.settings.sortingAndFilterOptions') }}</div>
        <div class="mb-2 d-flex align-center text-left">
          <div>{{ $t('spacesList.settings.pageSizeLabel') }}</div>
          <v-spacer />
          <number-input
            v-model="settings.pageSize"
            :step="1"
            :min="0"
            :max="50" />
        </div>
        <div class="mb-2 d-flex align-center text-left">
          <div>{{ $t('spacesList.settings.SortItemsBy') }}</div>
          <v-spacer />
          <select
            v-model="settings.sortBy"
            :aria-label="$t('generalSettings.sidebar.spacesSortItemsBy')"
            class="ignore-vuetify-classes py-2 height-auto width-auto text-truncate ma-0">
            <option value="lastVisited">
              {{ $t('spacesList.settings.spacesSortItemsByRecentlyVisited') }}
            </option>
            <option value="title">
              {{ $t('spacesList.settings.spacesSortItemsByTitle') }}
            </option>
          </select>
        </div>
        <div class="d-flex align-center text-left">
          <div>{{ $t('spacesList.settings.allowFilteringPerCategory') }}</div>
          <v-spacer />
          <v-switch
            v-model="settings.allowFilteringPerCategory"
            class="ma-0 width-fit-content" />
        </div>
        <div v-if="settings.allowFilteringPerCategory" class="mt-2 d-flex align-center text-left">
          <div>{{ $t('spacesList.settings.maximumSubcategoryDepth') }}</div>
          <v-spacer />
          <number-input
            v-model="settings.categoryDepth"
            :step="1"
            :min="0"
            :max="50" />
        </div>
        <div class="mt-4 mb-2 text-header">{{ $t('spacesList.settings.filterList') }}</div>
        <div class="mb-2 ms-n1 d-flex align-center">
          <v-radio-group
            v-model="settings.filterType"
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
            <template v-if="settings.filterType === 'template'">
              <v-autocomplete
                v-model="templateId"
                :items="enabledSpaceTemplates"
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
                  v-for="(t, index) in selectedSpaceTemplates"
                  :key="t.id"
                  max-width="150"
                  color="primary"
                  class="me-2"
                  dense>
                  <div class="d-flex align-center text-truncate">
                    <v-icon class="me-2" size="24">{{ t.icon }}</v-icon>
                    <div class="text-truncate">{{ t.name }}</div>
                    <v-btn
                      :title="$t('spacesList.settings.delete')"
                      class="me-n2"
                      icon
                      @click="removeItem(index, settings.templateIds)">
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
        <template v-if="settings.filterType === 'category'">
          <category-suggester
            v-model="categoryId"
            class="mt-n2 mb-4 mx-0 pa-0"
            label=""
            access-permission />
          <div class="mb-2">{{ $t('spacesList.settings.categoryListSortTitle') }}</div>
          <v-list class="pa-0" dense>
            <v-list-item
              v-for="(c, index) in selectedSpaceCategories"
              :key="c.id"
              class="pa-0"
              dense>
              <v-list-item-icon class="me-2 my-auto">
                <v-icon size="24">{{ c.icon }}</v-icon>
              </v-list-item-icon>
              <v-list-item-content class="me-2 pa-0 text-truncate">
                <v-list-item-title class="text-truncate">
                  {{ c.name }}
                </v-list-item-title>
              </v-list-item-content>
              <v-list-item-action :class="index == (selectedSpaceCategories.length -1) && 'invisible'" class="ms-2 my-auto">
                <v-btn
                  :title="$t('spacesList.settings.moveDown')"
                  icon
                  @click="moveDown(index, settings.categoryIds)">
                  <v-icon size="18">fa-arrow-down</v-icon>
                </v-btn>
              </v-list-item-action>
              <v-list-item-action :class="index == 0 && 'invisible'" class="mx-0 my-auto">
                <v-btn
                  :title="$t('spacesList.settings.moveUp')"
                  icon
                  @click="moveUp(index, settings.categoryIds)">
                  <v-icon size="18">fa-arrow-up</v-icon>
                </v-btn>
              </v-list-item-action>
              <v-list-item-action class="mx-0 my-auto">
                <v-btn
                  :title="$t('spacesList.settings.delete')"
                  icon
                  @click="removeItem(index, settings.categoryIds)">
                  <v-icon size="18" color="error">fa-trash</v-icon>
                </v-btn>
              </v-list-item-action>
            </v-list-item>
          </v-list>
        </template>
      </div>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-btn
          :disabled="saving"
          class="btn ms-auto me-2"
          @click="close">
          {{ $t('spacesList.label.cancel') }}
        </v-btn>
        <v-btn
          :disabled="disabled"
          :loading="saving"
          class="btn btn-primary"
          elevation="0"
          @click="save">
          {{ $t('spacesList.label.apply') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    saving: false,
    settings: {},
    originalSettings: {},
    maxNameLength: 150,
    categoryId: null,
    templateId: null,
    selectedSpaceCategories: [],
  }),
  computed: {
    rules() {
      return {
        name: [
          v => !!v?.length || ' ',
          v => !v?.length || v.length <= this.maxNameLength || this.$t('spacesList.settings.nameExceedsMaxLength', {
            0: this.maxNameLength,
          }),
        ],
      };
    },
    modified() {
      return JSON.stringify(this.settings) !== JSON.stringify(this.originalSettings);
    },
    disabled() {
      return !this.modified || Object.keys(this.settings.nameTranslations).some(k => this.settings.nameTranslations[k]?.length > this.maxNameLength);
    },
    enabledSpaceTemplates() {
      return this.$root.spaceTemplates?.filter?.(t => t.enabled && !t.deleted && !this.selectedSpaceTemplates.find(st => st.id === t.id));
    },
    selectedSpaceTemplates() {
      return this.settings.templateIds?.map?.(id => this.$root.spaceTemplates?.find?.(t => t.id === id)).filter(t => t) || [];
    },
    categoryIds() {
      return this.settings.categoryIds;
    },
  },
  watch: {
    async categoryId() {
      if (this.categoryId) {
        if (this.settings.categoryIds.indexOf(this.categoryId) < 0) {
          this.settings.categoryIds.push(this.categoryId);
        }
        await this.$nextTick();
        this.categoryId = null;
      }
    },
    async templateId() {
      if (this.templateId) {
        if (this.settings.templateIds.indexOf(this.templateId) < 0) {
          this.settings.templateIds.push(this.templateId);
        }
        await this.$nextTick();
        this.templateId = null;
      }
    },
    async categoryIds() {
      if (!this.categoryIds?.length) {
        this.selectedSpaceCategories = [];
      } else {
        this.selectedSpaceCategories = await Promise.all(this.categoryIds.map(id => this.$categoryService.getCategory(id)));
      }
    },
  },
  async created() {
    this.$root.$on('spaces-list-settings-open', this.open);
    document.addEventListener('spaces-list-settings-open', this.open);
    if (!this.$root.spaceTemplates) {
      this.$root.spaceTemplates = await this.$spaceTemplateService.getSpaceTemplates(true);
    }
  },
  beforeDestroy() {
    this.$root.$off('spaces-list-settings-open', this.open);
    document.removeEventListener('spaces-list-settings-open', this.open);
  },
  methods: {
    open(event) {
      if (event?.detail && this.$root.id && this.$root.id !== event?.detail) {
        return;
      }
      this.settings = JSON.parse(JSON.stringify(this.$root.settings));
      this.originalSettings = JSON.parse(JSON.stringify(this.$root.settings));
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    resetFilterType() {
      this.settings.categoryIds = null;
      this.settings.templateIds = null;
      if (this.settings.filterType === 'template') {
        this.settings.templateIds = [];
      } else if (this.settings.filterType === 'category') {
        this.settings.categoryIds = [];
      }
    },
    async save() {
      this.saving = true;
      try {
        const formData = new FormData();
        formData.append('settings', JSON.stringify(this.settings));
        const urlParams = new URLSearchParams(formData).toString();
        const response = await fetch(this.$root.settingsSaveUrl, {
          method: 'POST',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          body: urlParams,
        });
        if (response?.ok) {
          this.$root.$emit('alert-message', this.$t('spacesList.settings.savedSuccessfully'), 'success');
          this.$root.settings = this.settings;
          this.$root.$emit('spaces-list-settings-updated');
          this.close();
        } else {
          this.$root.$emit('alert-message', this.$t('spacesList.settings.saveError'), 'error');
        }
      } finally {
        this.saving = false;
      }
    },
    removeItem(index, array) {
      array.splice(index, 1);
      this.settings = JSON.parse(JSON.stringify(this.settings));
    },
    moveUp(index, array) {
      const item = array[index];
      array.splice(index, 1);
      array.splice(index - 1, 0, item);
      this.settings = JSON.parse(JSON.stringify(this.settings));
    },
    moveDown(index, array) {
      const item = array[index];
      array.splice(index, 1);
      array.splice(index + 1, 0, item);
      this.settings = JSON.parse(JSON.stringify(this.settings));
    },
  },
};
</script>