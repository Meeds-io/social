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
    id="SpaceSettingsCategoriesDrawer"
    ref="drawer"
    v-model="drawer"
    :loading="loading || saving"
    allow-expand
    right>
    <template #title>
      {{ $t('SpaceSettings.editCategories.drawer') }}
    </template>
    <template v-if="drawer" #content>
      <div class="d-flex flex-column ma-4">
        <div class="mb-2">
          {{ $t('SpaceSettings.editCategories.drawer.summary1') }}
        </div>
        <div class="mb-4">
          {{ $t('SpaceSettings.editCategories.drawer.summary2') }}
        </div>
        <div class="text-header mb-2">
          {{ $t('SpaceSettings.editCategories.drawer.manageCategories') }}
        </div>
        <v-autocomplete
          ref="autocomplete"
          v-model="category"
          :items="filteredCategories"
          :placeholder="$t('SpaceSettings.editCategories.drawer.searchCategories')"
          item-text="name"
          item-value="id"
          class="mx-0 mt-0 mb-4 pa-0 elevation-0 no-border"
          hide-no-data
          hide-details
          return-object
          outlined
          dense
          @update:search-input="keyword = $event">
          <template #item="{item}">
            <v-card
              color="transparent"
              max-width="350"
              flat>
              <v-list-item :title="item.name" class="pa-0">
                <v-list-item-icon class="me-3">
                  <v-card
                    color="transparent"
                    min-width="20"
                    flat>
                    <v-icon size="20">{{ item.icon }}</v-icon>
                  </v-card>
                </v-list-item-icon>
                <v-list-item-content>
                  <v-list-item-title class="text-truncate">
                    {{ item.name }}
                  </v-list-item-title>
                </v-list-item-content>
              </v-list-item>
            </v-card>
          </template>
        </v-autocomplete>
        <v-list
          v-if="selectedCategories"
          class="pa-0 mb-4 full-width overflow-hidden"
          dense>
          <v-list-item
            v-for="item in sortedCategories"
            :key="item.id"
            :title="item.name"
            class="pa-0">
            <v-list-item-icon class="ps-0 pe-3 mx-0 my-auto">
              <v-card
                color="transparent"
                min-width="20"
                flat>
                <v-icon size="20">{{ item.icon }}</v-icon>
              </v-card>
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title class="text-truncate my-auto">
                {{ item.name }}
              </v-list-item-title>
            </v-list-item-content>
            <v-list-item-action class="ps-3 mx-0 my-auto">
              <v-btn
                :title="$t('SpaceSettings.editCategories.deleteCategory')"
                min-width="16"
                color="error"
                icon
                small
                @click="removeCategory(item)">
                <v-icon size="16">fa-trash</v-icon>
              </v-btn>
            </v-list-item-action>
          </v-list-item>
        </v-list>
      </div>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :disabled="saving"
          class="btn me-2"
          @click="close">
          {{ $t('SpaceSettings.button.cancel') }}
        </v-btn>
        <v-btn
          :disabled="!modified"
          :loading="saving"
          class="btn btn-primary"
          @click.prevent.stop="save">
          {{ $t('SpaceSettings.button.updateSpace') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    loading: false,
    saving: false,
    categoryIds: null,
    selectedCategories: null,
    categories: null,
    category: null,
    keyword: null,
    searchTimeout: null,
    limit: 10,
  }),
  computed: {
    selectedCategoryIds() {
      return this.selectedCategories?.map?.(cat => cat.id) || [];
    },
    modified() {
      return JSON.stringify(this.selectedCategoryIds) !== JSON.stringify(this.categoryIds);
    },
    filteredCategories() {
      return this.categories?.filter?.(cat => this.selectedCategoryIds.indexOf(cat.id) < 0);
    },
    sortedCategories() {
      return this.selectedCategories?.slice?.()?.sort?.(this.comparator);
    },
  },
  watch: {
    keyword() {
      this.searchCategories();
    },
    category() {
      if (this.category) {
        if (!this.selectedCategories) {
          this.selectedCategories = [this.category] || [];
        } else if (this.selectedCategoryIds.indexOf(this.category.id) < 0) {
          this.selectedCategories.push(this.category);
        }
        window.setTimeout(() => this.category = null, 10);
      } else {
        this.$refs.autocomplete.isFocused = false;
      }
    },
  },
  methods: {
    open() {
      this.categoryIds = this.$root.space?.categoryIds || [];
      this.categories = null;
      this.$refs.drawer.open();
      this.init();
    },
    async init() {
      this.loading = true;
      try {
        this.selectedCategories = await Promise.all(this.categoryIds.map(id => this.$categoryService.getCategoryTree({
          parentId: id,
        })));
      } finally {
        this.loading = false;
      }
    },
    close() {
      this.$refs.drawer.close();
    },
    async searchCategories() {
      if (this.keyword?.trim?.()?.length) {
        this.categories = await this.$categoryService.findCategories({
          query: this.keyword,
          limit: this.limit + this.selectedCategoryIds.length,
          linkPermission: true,
          sortByName: true,
        });
      } else {
        this.categories = [];
      }
    },
    removeCategory(item) {
      this.selectedCategories.splice(this.selectedCategories.indexOf(item), 1);
    },
    async save() {
      this.saving = true;
      try {
        if (this.categoryIds?.length) {
          const unlinkIds = this.categoryIds.filter(id => this.selectedCategoryIds.indexOf(id) < 0);
          if (unlinkIds?.length) {
            await Promise.all(unlinkIds.map(id => this.$categoryLinkService.unlink(id, {
              type: 'space',
              id: this.$root.space.id,
              spaceId: this.$root.space.id,
            })));
          }
        }
        if (this.selectedCategoryIds.length) {
          const linkIds = this.selectedCategoryIds.filter(id => this.categoryIds.indexOf(id) < 0);
          if (linkIds?.length) {
            await Promise.all(linkIds.map(id => this.$categoryLinkService.link(id, {
              type: 'space',
              id: this.$root.space.id,
              spaceId: this.$root.space.id,
            })));
          }
        }
        this.$root.space.categoryIds = this.selectedCategoryIds;
        this.$root.$emit('alert-message', this.$t('SpaceSettings.editCategories.success'), 'success');
        this.close();
      } catch (e) {
        this.$root.$emit('alert-message', this.$t('SpaceSettings.editCategories.error'), 'error');
      } finally {
        this.saving = false;
      }
    },
    comparator(a, b) {
      return this.$root.collator.compare(a.name, b.name);
    },
  },
};
</script>