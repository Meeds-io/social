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
    id="ActivityStreamSettingsDrawer"
    ref="drawer"
    v-model="drawer"
    :loading="saving"
    allow-expand
    right>
    <template #title>
      {{ $root.spaceId && $t('activityStream.settings.space.title') || $t('activityStream.settings.title') }}
    </template>
    <template v-if="drawer" #content>
      <div class="pa-5" flat>
        <template v-if="$root.spaceId">
          <div class="mb-2 text-header">{{ $t('activityStream.settings.space.displayOptions') }}</div>
          <div class="mb-2">{{ $t('activityStream.settings.space.displayOptions.title') }}</div>
          <translation-text-field
            v-model="settings.nameTranslations"
            :placeholder="$t('activityStream.settings.space.displayOptions.placeholder')"
            :maxlength="maxNameLength"
            :rules="rules.name"
            :required="false"
            drawer-title="activityStream.settings.space.displayOptions.nameTranslationTitle"
            class="width-auto flex-grow-1 pb-1"
            back-icon />
        </template>
        <template v-else>
          <div class="mb-2 text-header">{{ $t('activityStream.settings.postingOptions') }}</div>
          <v-tooltip
            :disabled="$root.canPost"
            bottom>
            <template #activator="{on, attrs}">
              <div
                v-on="on"
                v-bind="attrs"
                class="d-flex full-width align-start text-start">
                <v-list-item
                  class="pa-0 me-4"
                  dense>
                  <v-list-item-content class="pa-0">
                    <v-list-item-title :class="!$root.canPost && 'text--disabled'">
                      {{ $t('activityStream.settings.postingOptions.title') }}
                    </v-list-item-title>
                    <v-list-item-subtitle class="text-wrap">
                      {{ $t('activityStream.settings.postingOptions.subtitle') }}
                    </v-list-item-subtitle>
                  </v-list-item-content>
                </v-list-item>
                <v-spacer />
                <v-switch
                  v-model="settings.allowPostToNetwork"
                  :disabled="!$root.canPost"
                  class="mx-0 mt-0 pa-0 width-fit-content" />
              </div>
            </template>
            <span>{{ $t('activityStream.settings.postingOptions.disabled') }}</span>
          </v-tooltip>
        </template>
        <div class="mt-4 mb-2 text-header">{{ $t('activityStream.settings.filterOptions') }}</div>
        <div class="d-flex full-width align-center text-start">
          <div>{{ $t('activityStream.settings.filterOptions.title') }}</div>
          <v-spacer />
          <v-switch
            v-model="settings.allowFilteringPerCategory"
            class="ma-0 pt-2 width-fit-content" />
        </div>
        <div v-if="settings.allowFilteringPerCategory" class="d-flex full-width align-center text-start">
          <div>{{ $t('activityStream.settings.setMaximumSubcategoryDepth') }}</div>
          <v-spacer />
          <number-input
            v-model="settings.categoryDepth"
            :step="1"
            :min="0"
            :max="50" />
        </div>
        <div class="mt-4 mb-2 text-header">{{ $t('activityStream.settings.filterList') }}</div>
        <div class="d-flex align-center text-start">
          <div>{{ $t('activityStream.settings.filterListPerCategory') }}</div>
          <v-spacer />
          <v-switch
            v-model="filterPerCategories"
            class="ma-0 width-fit-content" />
        </div>
        <div v-if="filterPerCategories" class="mt-4">
          <category-suggester
            v-model="categoryId"
            class="mt-n2 mb-4 mx-0 pa-0"
            label=""
            access-permission />
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
                <v-list-item-title class="text-truncate">
                  {{ c.name }}
                </v-list-item-title>
              </v-list-item-content>
              <v-list-item-action :class="index == (selectedCategories.length -1) && 'invisible'" class="ms-2 my-auto">
                <v-btn
                  :title="$t('activityStream.settings.moveDown')"
                  icon
                  @click="moveDown(index, settings.categoryIds)">
                  <v-icon size="18">fa-arrow-down</v-icon>
                </v-btn>
              </v-list-item-action>
              <v-list-item-action :class="index == 0 && 'invisible'" class="mx-0 my-auto">
                <v-btn
                  :title="$t('activityStream.settings.moveUp')"
                  icon
                  @click="moveUp(index, settings.categoryIds)">
                  <v-icon size="18">fa-arrow-up</v-icon>
                </v-btn>
              </v-list-item-action>
              <v-list-item-action class="mx-0 my-auto">
                <v-btn
                  :title="$t('activityStream.settings.deleteCategory')"
                  icon
                  @click="removeItem(index, settings.categoryIds)">
                  <v-icon size="18" color="error">fa-trash</v-icon>
                </v-btn>
              </v-list-item-action>
            </v-list-item>
          </v-list>
        </div>
      </div>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-btn
          :disabled="saving"
          class="btn ms-auto me-2"
          @click="close">
          {{ $t('activityStream.label.cancel') }}
        </v-btn>
        <v-btn
          :disabled="disabled"
          :loading="saving"
          class="btn btn-primary"
          elevation="0"
          @click="save">
          {{ $t('activityStream.label.apply') }}
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
    selectedCategories: [],
    filterPerCategories: false,
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
    async categoryIds() {
      if (!this.categoryIds?.length) {
        this.selectedCategories = [];
      } else {
        this.selectedCategories = await Promise.all(this.categoryIds.map(id => this.$categoryService.getCategory(id)));
      }
    },
    filterPerCategories() {
      if (this.drawer && !this.filterPerCategories) {
        this.settings.categoryIds = [];
      }
    },
  },
  created() {
    this.$root.$on('activity-stream-settings-open', this.open);
  },
  beforeDestroy() {
    this.$root.$off('activity-stream-settings-open', this.open);
  },
  methods: {
    open() {
      this.settings = JSON.parse(JSON.stringify(this.$root.settings));
      this.originalSettings = JSON.parse(JSON.stringify(this.$root.settings));
      this.filterPerCategories = !!this.categoryIds?.length;
      if (!this.$root.canPost) {
        this.settings.allowPostToNetwork = false;
      }
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    resetFilterType() {
      this.settings.categoryIds = [];
    },
    async save() {
      this.saving = true;
      try {
        const formData = new FormData();
        formData.append('settings', JSON.stringify(this.settings));
        const urlParams = new URLSearchParams(formData).toString();
        const response = await fetch(this.$root.saveSettingsUrl, {
          method: 'POST',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          body: urlParams,
        });
        if (response?.ok) {
          this.$root.$emit('alert-message', this.$t('activityStream.settings.savedSuccessfully'), 'success');
          this.$root.settings = this.settings;
          this.$root.$emit('activity-stream-settings-updated', this.settings);
          this.close();
        } else {
          this.$root.$emit('alert-message', this.$t('activityStream.settings.saveError'), 'error');
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