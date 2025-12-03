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
        <v-tooltip
          v-if="$root.isPublicPage && $root.registrationType !== 'OPEN'"
          :disabled="$root.isAdministrator"
          bottom>
          <template #activator="{on, attrs}">
            <div
              v-on="on"
              v-bind="attrs"
              class="mb-4 d-flex full-width align-start text-start">
              <v-list-item
                class="pa-0 me-4"
                dense>
                <v-list-item-content class="pa-0">
                  <v-list-item-title :class="!$root.isAdministrator && 'text--disabled'">
                    {{ $t('spacesList.settings.anonymousAccess') }}
                  </v-list-item-title>
                  <v-list-item-subtitle class="text-wrap">
                    {{ $t('spacesList.settings.anonymousAccessSubtitle') }}
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <v-spacer />
              <v-switch
                v-model="publicAccess"
                :disabled="!$root.isAdministrator"
                class="mx-0 mt-n1 pa-0 width-fit-content" />
            </div>
          </template>
          <span>{{ $t('publicWidgetHidden.tooltip.onlyAdminCanChangeValue') }}</span>
        </v-tooltip>
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
        <div class="mb-2 d-flex align-center text-start">
          <div>{{ $t('spacesList.settings.pageSizeLabel') }}</div>
          <v-spacer />
          <number-input
            v-model="settings.pageSize"
            :step="1"
            :min="0"
            :max="50" />
        </div>
        <div class="mb-2 d-flex align-center text-start">
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
        <div class="d-flex align-center text-start">
          <div>{{ $t('spacesList.settings.allowFilteringPerCategory') }}</div>
          <v-spacer />
          <v-switch
            v-model="settings.allowFilteringPerCategory"
            class="ma-0 width-fit-content" />
        </div>
        <div v-if="settings.allowFilteringPerCategory" class="mt-2 d-flex align-center text-start">
          <div>{{ $t('spacesList.settings.maximumSubcategoryDepth') }}</div>
          <v-spacer />
          <number-input
            v-model="settings.categoryDepth"
            :step="1"
            :min="0"
            :max="50" />
        </div>
        <spaces-filter-settings
          v-model="settings" />
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
    settings: {
      filterType: 'any',
      categoryIds: [],
      excludeCategoryIds: [],
      templateIds: []
    },
    originalSettings: {},
    maxNameLength: 150,
    publicAccess: false,
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
      return JSON.stringify(this.settings) !== JSON.stringify(this.originalSettings)
        || (this.$root.isAdministrator && this.$root.isPublicPage && this.publicAccess !== !!this.$root.settingName);
    },
    disabled() {
      return !this.modified || Object.keys(this.settings.nameTranslations).some(k => this.settings.nameTranslations[k]?.length > this.maxNameLength);
    }
  },
  created() {
    this.$root.$on('spaces-list-settings-open', this.open);
    document.addEventListener('spaces-list-settings-open', this.open);
    this.publicAccess = !!this.$root.settingName;
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
      this.settings = structuredClone(this.$root.settings);
      this.originalSettings = structuredClone(this.$root.settings);
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    async save() {
      this.saving = true;
      try {
        const formData = new FormData();
        formData.append('settings', JSON.stringify(this.settings));
        if (this.publicAccess !== !!this.$root.settingName) {
          formData.append('publicAccess', this.publicAccess);
        }
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
          if (this.publicAccess !== !!this.$root.settingName) {
            if (!this.publicAccess) {
              this.$root.settingName = null;
            } else {
              this.$root.settingName = await fetch(this.$root.settingNameUrl, {
                method: 'GET',
                credentials: 'include',
              }).then(resp => resp?.ok && resp.text());
            }
          }
        } else {
          this.$root.$emit('alert-message', this.$t('spacesList.settings.saveError'), 'error');
        }
      } finally {
        this.saving = false;
      }
    }
  },
};
</script>
