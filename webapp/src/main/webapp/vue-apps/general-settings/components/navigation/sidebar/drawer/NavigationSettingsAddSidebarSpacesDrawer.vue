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
          class="mt-n2 mb-4 mx-0 pa-0 elevation-0 no-border"
          hide-no-data
          hide-selected
          hide-details
          outlined
          dense />
        <div class="font-weight-bold mb-4">
          {{ $t('generalSettings.sidebar.spaces.sidebarDisplay') }}
        </div>
        <div v-if="option === 'SPACES'" class="mb-4">
          <div class="mb-2">
            {{ $t('generalSettings.sidebar.spacesUpdateNameLabel') }}
          </div>
          <translation-text-field
            v-model="names"
            :placeholder="$t('generalSettings.sidebar.spacesNamesPlaceHolder')"
            :maxlength="50"
            drawer-title="generalSettings.sidebar.spacesNamesDrawerTitle" />
        </div>
        <div v-if="option === 'SPACE_TEMPLATE'" class="d-flex align-center mb-4">
          <div>{{ $t('generalSettings.sidebar.spaces.displayOnlyWhenMember') }}</div>
          <v-spacer />
          <v-switch
            v-model="displayOnlyWhenMember"
            true-value="true"
            false-value="false"
            class="ma-0 width-fit-content" />
        </div>
        <div class="d-flex align-center mb-4">
          <div>{{ $t('generalSettings.sidebar.spacesSelectLimit') }}</div>
          <v-spacer />
          <number-input
            v-model="limit"
            :step="1"
            :min="0"
            :max="10" />
        </div>
        <div v-if="limit" class="d-flex align-center mb-2">
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
        <div v-if="limit" class="d-flex align-center mb-4">
          <div>{{ $t('generalSettings.sidebar.displaySpacesInMobile') }}</div>
          <v-spacer />
          <v-switch
            v-model="displayItemsInMobile"
            true-value="true"
            false-value="false"
            class="ma-0 width-fit-content" />
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
    modified: false,
    names: {},
    settings: null,
    option: null,
    item: null,
    spaceTemplates: null,
    spaceTemplateId: null,
    sortBy: 'TITLE',
    limit: 4,
    displayItemsInMobile: 'false',
    displayOnlyWhenMember: 'true',
  }),
  computed: {
    spaceTemplate() {
      return this.spaceTemplateId && this.spaceTemplates?.find?.(t => Number(t.id) === Number(this.spaceTemplateId)) || null;
    },
    disabled() {
      return !this.modified
        || (this.option === 'SPACE_TEMPLATE' && !this.spaceTemplteId)
        || (this.option === 'SPACES' && (!this.names || !this.names[eXo.env.portal.defaultLanguage]?.trim?.()?.length || Object.values(this.names).find(name => name?.length > 50)))
        || !this.sortBy;
    },
  },
  watch: {
    option() {
      if (this.drawer) {
        this.modified = true;
      }
    },
    spaceTemplate() {
      if (this.drawer) {
        this.modified = true;
      }
    },
    limit() {
      if (this.drawer) {
        this.modified = true;
      }
    },
    displayItemsInMobile() {
      if (this.drawer) {
        this.modified = true;
      }
    },
    displayOnlyWhenMember() {
      if (this.drawer) {
        this.modified = true;
      }
    },
    sortBy() {
      if (this.drawer) {
        this.modified = true;
      }
    },
    names: {
      deep: true,
      handler() {
        if (this.drawer) {
          this.modified = true;
        }
      },
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
          displayItemsInMobile: 'false',
          displayOnlyWhenMember: 'true',
        },
      };
      this.isNew = !item;
      this.reset(this.item);
      this.$refs.drawer.open();
      this.refreshSpaceTemplates();
    },
    async refreshSpaceTemplates() {
      if (!this.spaceTemplates) {
        this.spaceTemplates = await this.$spaceTemplateService.getSpaceTemplates();
      }
    },
    reset(item) {
      this.modified = false;
      this.option = this.isNew ? 'SPACE_TEMPLATE' : item.type;
      this.spaceTemplateId = item?.properties?.spaceTemplateId && Number(item.properties.spaceTemplateId) || null;
      this.sortBy = item?.properties?.sortBy || 'TITLE';
      this.limit = item?.properties && Object.hasOwn(item.properties, 'limit') ? Number(item.properties.limit) : 4;
      this.displayItemsInMobile = item?.properties && Object.hasOwn(item.properties, 'displayItemsInMobile') ? item.properties.displayItemsInMobile : 'false';
      this.displayOnlyWhenMember = item?.properties && Object.hasOwn(item.properties, 'displayOnlyWhenMember') ? item.properties.displayOnlyWhenMember : 'false';
      this.names = item?.properties?.names && JSON.parse(item?.properties?.names) || {};
    },
    async apply() {
      this.item.type = this.option;
      if (this.option === 'SPACE_TEMPLATE') {
        this.item.name = this.spaceTemplate.name;
        this.item.icon = this.spaceTemplate.icon;
        this.item.properties = {
          spaceTemplateId: this.spaceTemplateId,
          sortBy: this.sortBy,
          limit: Math.min(this.limit, 10),
          displayItemsInMobile: this.displayItemsInMobile,
          displayOnlyWhenMember: this.displayOnlyWhenMember,
        };
      } else {
        this.item.name = this.names[eXo.env.portal.language] || this.names[eXo.env.portal.defaultLanguage];
        this.item.icon = 'fa-layer-group';
        Object.keys(this.names).forEach(k => {
          if (!this.names[k]?.trim?.()?.length) {
            delete this.names[k];
          }
        });
        this.item.properties = {
          names: JSON.stringify(this.names),
          sortBy: this.sortBy,
          limit: Math.min(this.limit, 10),
          displayItemsInMobile: this.displayItemsInMobile,
          displayOnlyWhenMember: this.displayOnlyWhenMember,
        };
      }
      const data = await this.$spaceService.getSpacesByFilter({
        templateId: this.option === 'SPACE_TEMPLATE' ? this.spaceTemplateId : null,
        offset: 0,
        limit: this.limit,
        filter: this.getSpacesFilterType(this.sortBy),
        sortBy: this.getSpacesSortField(this.sortBy),
        sortDirection: this.getSpacesSortDirection(this.sortBy),
        returnSize: true,
      });
      this.item.items = data?.spaces?.map?.(s => ({
        name: s.displayName,
        avatar: s.avatarUrl,
        url: `/portal/s/${s.id}`,
        type: 'SPACE',
        properties: {
          id: s.id,
        },
      })) || [];
      this.item.properties.notMember = String(data?.size === 0);
      if (this.isNew) {
        this.settings.sidebar.items.push(this.item);
      }
      this.close();
    },
    getSpacesFilterType(sortBy) {
      if (sortBy === 'LAST_ACCESS') {
        return 'lastVisited';
      } else if (sortBy === 'FAVORITE') {
        return 'favorite';
      } else {
        return 'member';
      }
    },
    getSpacesSortField() {
      return 'title';
    },
    getSpacesSortDirection(sortBy) {
      if (sortBy === 'LAST_ACCESS') {
        return 'DESC';
      } else {
        return 'ASC';
      }
    },
    close() {
      this.$refs.drawer.close();
    },
  },
};
</script>