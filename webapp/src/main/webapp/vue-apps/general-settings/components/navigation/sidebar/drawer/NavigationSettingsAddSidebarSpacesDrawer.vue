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
    :right="!$vuetify.rtl"
    :left="$vuetify.rtl"
    no-x-scroll>
    <template #title>
      {{ $t(isNew && 'generalSettings.addSideBarItemSpaces.drawerTitle' || 'generalSettings.updateSideBarItemSpaces.drawerTitle') }}
    </template>
    <template v-if="drawer" #content>
      <div class="pa-4">
        <v-radio-group
          v-model="option"
          class="mt-0 mb-4 ms-n1 text-no-wrap flex-grow-1 flex-shrink-0"
          mandatory>
          <v-radio
            value="SPACE_TEMPLATE"
            class="mx-0 mt-0 mb-1">
            <template #label>
              <span class="text-body">{{ $t('generalSettings.sidebar.spaceTemplateOption') }}</span>
            </template>
          </v-radio>
          <v-radio
            value="SPACES"
            class="mx-0 mt-0 mb-1">
            <template #label>
              <span class="text-body">{{ $t('generalSettings.sidebar.spacesOption') }}</span>
            </template>
          </v-radio>
        </v-radio-group>
        <v-autocomplete
          v-if="option === 'SPACE_TEMPLATE' && spaceTemplates"
          v-model="spaceTemplateId"
          :items="spaceTemplates"
          :placeholder="$t('generalSettings.sidebar.searchTemplatePlaceholder')"
          item-text="name"
          item-value="id"
          class="mt-0 mb-4 mx-0 pa-0 elevation-0 no-border"
          hide-no-data
          hide-selected
          hide-details
          outlined
          dense />
        <div v-if="option === 'SPACES'" class="mb-4">
          <div class="mb-2">
            {{ $t('generalSettings.sidebar.spacesUpdateNameLabel') }}
          </div>
          <translation-text-field
            v-model="names"
            :placeholder="$t('generalSettings.sidebar.spacesNamesPlaceHolder')"
            drawer-title="generalSettings.sidebar.spacesNamesDrawerTitle" />
        </div>
        <div class="d-flex align-center mb-2">
          <div>{{ $t('generalSettings.sidebar.spacesSortItemsBy') }}</div>
          <v-spacer />
          <select
            v-model="sortBy"
            :aria-label="$t('generalSettings.sidebar.spacesSortItemsBy')"
            class="ignore-vuetify-classes py-2 height-auto width-auto text-truncate ma-0">
            <option value="TITLE">
              {{ $t('generalSettings.sidebar.spacesSortItemsByTitle') }}
            </option>
            <option value="FAVORITE">
              {{ $t('generalSettings.sidebar.spacesSortItemsByFavorite') }}
            </option>
            <option value="LAST_ACCESS">
              {{ $t('generalSettings.sidebar.spacesSortItemsByRecentlyVisited') }}
            </option>
          </select>
        </div>
        <div class="d-flex align-center mb-4">
          <div>{{ $t('generalSettings.sidebar.spacesSelectLimit') }}</div>
          <v-spacer />
          <number-input
            v-model="limit"
            step="1"
            min="1"
            max="10" />
        </div>
      </div>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          class="btn me-2"
          @click="close">
          {{ $t('generalSettings.cancel') }}
        </v-btn>
        <v-btn
          :loading="saving"
          :disabled="!modified"
          class="btn-primary"
          elevation="0"
          @click="apply">
          {{ $t(isNew && 'generalSettings.add' || 'generalSettings.update') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    isNew: false,
    names: {},
    settings: null,
    option: null,
    item: null,
    spaceTemplates: null,
    spaceTemplateId: null,
    icon: 'fa-layers-group',
    sortBy: 'TITLE',
    limit: 4,
  }),
  computed: {
    spaceTemplate() {
      return this.spaceTemplateId && this.spaceTemplates?.find?.(t => Number(t.id) === Number(this.spaceTemplateId)) || null;
    },
  },
  watch: {
    option() {
      if (this.drawer) {
        this.reset();
      }
    },
  },
  created() {
    this.$root.$on('sidebar-item-add-spaces', this.open);
    this.$root.$on('sidebar-item-edit-spaces', this.open);
  },
  beforeDestroy() {
    this.$root.$off('sidebar-item-add-spaces', this.open);
    this.$root.$off('sidebar-item-edit-spaces', this.open);
  },
  methods: {
    open(settings, item) {
      this.settings = settings;
      this.item = item || {
        name: null,
        url: null,
        target: null,
        avatar: null,
        icon: null,
        type: null,
        items: null,
        properties: {
          sortBy: 'TITLE',
          limit: 4,
        },
      };
      this.isNew = !item;
      this.reset(item);
      this.$refs.drawer.open();
      this.refreshSpaceTemplates();
    },
    async refreshSpaceTemplates() {
      if (!this.spaceTemplates) {
        this.spaceTemplates = await this.$spaceTemplateService.getSpaceTemplates();
      }
    },
    reset(item) {
      if (item) {
        this.option = this.isNew ? 'SPACE_TEMPLATE' : item.type;
      }
      this.spaceTemplateId = this.option === 'SPACE_TEMPLATE' && item?.properties?.spaceTemplateId || null;
      this.icon = this.option === 'SPACE_TEMPLATE' && (this.spaceTemplate?.icon || item?.icon) || 'fa-layers-group';
      this.sortBy = item?.properties?.sortBy || this.sortBy || 'TITLE';
      this.limit = item?.properties?.limit || this.limit || 4;
    },
    apply() {
      this.close();
    },
    close() {
      this.$refs.drawer.close();
    },
  },
};
</script>