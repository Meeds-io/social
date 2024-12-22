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
    :loading="loading"
    go-back-button
    allow-expand
    right
    @closed="$emit('closed')">
    <template #title>
      {{ $t('categoryInput.drawer') }}
    </template>
    <template v-if="drawer" #content>
      <div v-if="hasItems" class="d-flex flex-column">
        <application-toolbar
          :right-text-filter="{
            minCharacters: 1,
            placeholder: $t('categoryInput.filter.placeholder'),
            tooltip: $t('categoryInput.noData'),
          }"
          class="px-1"
          compact
          @filter-text-input="keyword = $event"
          @filter-text-input-end-typing="search">
          <template #left>
            <div class="text-header">
              {{ $t('categoryInput.drawer.selectCategories') }}
            </div>
          </template>
        </application-toolbar>
        <v-treeview
          v-model="categoryIds"
          :items="categoryTreeItems"
          :open.sync="openedIds"
          :search="keyword"
          :filter="filter"
          :load-children="loadChildren"
          :disabled="loading"
          class="hide-disabled-selection ms-n5 me-4"
          expand-icon=""
          item-children="categories"
          item-key="id"
          item-text="name"
          item-disabled="disabled"
          selection-type="independent"
          disable-per-node
          open-on-click
          selectable
          transition
          hoverable
          dense>
          <template #label="{ item, open }">
            <div v-if="!item.loadMore" class="d-flex align-center">
              <v-card
                color="transparent"
                min-width="24"
                flat>
                <v-icon
                  v-show="item.size"
                  :class="{
                    'fa-rotate-90': open && !$vuetify.rtl,
                    'fa-rotate-270': open && $vuetify.rtl,
                  }"
                  size="16">
                  {{ chevonIcon }}
                </v-icon>
              </v-card>
              <v-card
                class="d-flex align-center justify-center ms-1 me-2"
                color="transparent"
                min-width="16"
                flat>
                <v-icon size="16">{{ item.icon }}</v-icon>
              </v-card>
              <div class="text-truncate">{{ item.name }}</div>
            </div>
            <div v-else class="d-flex align-center">
              <v-btn
                :title="$t('categoryInput.loadMore')"
                :loading="item.loading"
                color="transparent"
                class="ms-7 px-0"
                elevation="0"
                link
                @click.prevent.stop="loadMore(item.parentId)">
                <span class="text-link">{{ $t('categoryInput.loadMore') }}</span>
              </v-btn>
            </div>
          </template>
        </v-treeview>
      </div>
      <div v-else-if="categoryTree?.owner" class="d-flex flex-column align-center justify-center ma-5">
        <v-icon size="60" color="tertiary">fa-th-large</v-icon>
        <div class="mt-4 mb-2">{{ $t('categoryInput.admin.placeholder1') }}</div>
        <div
          v-sanitized-html="$t('categoryInput.admin.placeholder2', {
            0: `<a href='/portal/administration/home/development/categories'>`,
            1: '</a>',
          })"></div>
      </div>
      <div v-else-if="!loading" class="d-flex flex-column align-center justify-center ma-5">
        <v-icon size="60" color="tertiary">fa-th-large</v-icon>
        <div class="mt-4 mb-2">{{ $t('categoryInput.user.placeholder1') }}</div>
        <div>{{ $t('categoryInput.user.placeholder2') }}</div>
      </div>
    </template>
    <template v-if="hasItems" #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :disabled="loading"
          class="btn me-2"
          @click="close">
          {{ $t('SpaceSettings.button.cancel') }}
        </v-btn>
        <v-btn
          :disabled="loading"
          class="btn btn-primary"
          @click.prevent.stop="apply">
          {{ $t('SpaceSettings.button.apply') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    value: {
      type: Array,
      default: null,
    },
    selectedCategories: {
      type: Array,
      default: null,
    },
  },
  data: () => ({
    drawer: false,
    loading: false,
    keyword: null,
    openedIds: [],
    categoryIds: [],
    categoryTree: null,
    categoryOwnerId: null,
    categoryRootId: null,
    foundCategories: null,
    depth: 10,
    pageSize: 10,
    refresh: 1,
  }),
  computed: {
    isMobile() {
      return this.$vuetify.breakpoint.mobile;
    },
    chevonIcon() {
      return this.$vuetify.rtl && 'fa-chevron-left' || 'fa-chevron-right';
    },
    categories() {
      const categories = [];
      if (this.categoryTree && this.refresh > 0) {
        this.addSubcategories(this.categoryTree, categories);
      }
      return categories;
    },
    categoryTreeItems() {
      return this.categoryTree?.categories || [];
    },
    hasItems() {
      return this.categoryTreeItems?.length;
    },
  },
  watch: {
    keyword() {
      if (this.keyword) {
        this.loading = true;
      }
    },
    categories() {
      this.categories?.forEach?.(cat => {
        if (!cat.categories) {
          cat.categories = [];
        }
      });
    },
    async foundCategories() {
      if (this.foundCategories?.length) {
        if (this.openedIdsInterval) {
          window.clearTimeout(this.openedIdsInterval);
          this.openedIdsInterval = null;
        }
        await this.$nextTick();
        this.openedIdsInterval = window.setTimeout(() => {
          const openedIds = this.openedIds.slice();
          this.foundCategories.forEach(cat => {
            const category = cat?.id && this.getCategory(cat.id);
            if (category && !openedIds.find(id => id === cat.id)) {
              openedIds.push(cat.id);
              cat.ancestorIds.forEach(ancestorId => {
                if (!openedIds.find(id => id === ancestorId)) { // NOSONAR
                  openedIds.push(ancestorId);
                }
              });
            }
          });
          if (this.openedIds.length !== openedIds) {
            this.openedIds = openedIds;
          }
        }, 50);
      }
    },
  },
  methods: {
    openDrawer() {
      this.$refs.drawer.open();
      this.init();
    },
    close() {
      this.$refs.drawer.close();
    },
    async init() {
      this.loading = true;
      await this.refreshTree(this.categoryTree, this.depth);
      window.setTimeout(async () => {
        this.categoryOwnerId = this.categoryTree?.ownerId;
        this.categoryRootId = this.categoryTree?.id;
        if (this.selectedCategories?.length) {
          await Promise.all(this.selectedCategories.map(cat => this.loadParent(cat.id, cat.parentId)));
        }
        const categoryIds = this.value?.slice?.() || [];
        this.initOpenedElements(categoryIds);
        // Wait until opened ids are set
        await this.$nextTick();
        // Set selected elements once opened
        this.categoryIds = categoryIds;
        this.loading = false;
      }, 200);
    },
    initOpenedElements(categoryIds) {
      this.openedIds = [];
      categoryIds.forEach(id => this.addOpenedIdWithAncestor(id));
    },
    addOpenedIdWithAncestor(id) {
      const category = this.getCategory(id);
      if (category?.parentId && this.openedIds.indexOf(category.parentId) < 0) {
        this.openedIds.push(category.parentId);
        this.addOpenedIdWithAncestor(category.parentId);
      }
    },
    filter(item, search, textKey) {
      return (item[textKey] && item[textKey].indexOf(search) > -1) || this.foundCategories?.find?.(cat => cat.id === item.id);
    },
    async search() {
      if (this.keyword?.trim?.()?.length) {
        this.loading = true;
        try {
          await this.searchCategories(this.keyword.trim());
        } finally {
          this.loading = false;
        }
      } else {
        this.resetSearch();
      }
    },
    async loadChildren(item) {
      if (item?.loadMore) {
        return;
      }
      const category = this.getCategory(item.id);
      if (category.limit) {
        return category.categories;
      } else {
        return await this.refreshTree(item, 1);
      }
    },
    async refreshTree(item, depth, offset, limit) {
      if (item?.loadMore) {
        return;
      }
      const categoryIds = this.categoryIds;
      try {
        const parentId = item?.id || this.categoryRootId || 0;
        const ownerId = item?.ownerId || this.categoryOwnerId || 0;
        const categoryTree = await this.$categoryService.getCategoryTree({
          parentId,
          ownerId,
          depth,
          offset: offset || 0,
          limit: limit || this.pageSize,
          linkPermission: true,
        });
        if (!parentId) {
          this.categoryTree = categoryTree;
          return categoryTree;
        } else {
          Object.keys(categoryTree).forEach(key => item[key] = categoryTree[key]);
          return item;
        }
      } finally {
        this.refresh++;
        await this.$nextTick();
        this.categoryIds = categoryIds;
      }
    },
    async searchCategories(query) {
      this.foundCategories = await this.$categoryService.findCategories({
        query,
        ownerId: this.categoryOwnerId,
        offset: 0,
        limit: this.pageSize,
      });
      await Promise.all(this.foundCategories.map(cat => this.loadAncestors(cat)));
    },
    resetSearch() {
      this.foundCategories = null;
    },
    async loadAncestors(category) {
      let limit = 0;
      while (!this.getCategory(category.id)) {
        limit += this.pageSize;
        const index = category.ancestorIds.findIndex(id => this.getCategory(id));
        const length = category.ancestorIds.length;
        const ancestorId = category.ancestorIds[index];
        let lastLoadedParent = this.getCategory(ancestorId);
        // Can't be parallelized so disable Sonar and ESLint recommandations
        // eslint-disable-next-line no-await-in-loop
        await this.refreshTree(lastLoadedParent, length - index, 0, limit); // NOSONAR
        lastLoadedParent = this.getCategory(ancestorId);
        if (lastLoadedParent.id === category.parentId
            && lastLoadedParent.size <= limit) {
          break;
        }
      }
    },
    async loadParent(id, parentId) {
      let parentCategory = this.getCategory(parentId);
      if (!parentCategory) {
        return;
      }
      let limit = 0;
      while (!this.getCategory(id) && parentCategory.size > (parentCategory.limit || limit)) {
        limit += this.pageSize;
        // Can't be parallelized so disable Sonar and ESLint recommandations
        // eslint-disable-next-line no-await-in-loop
        await this.refreshTree(parentCategory, this.depth, 0, limit); // NOSONAR
        parentCategory = this.getCategory(parentId);
      }
    },
    async loadMore(id) {
      const category = this.getCategory(id);
      const loadMoreButtonItem = category.categories.find(i => i.loadMore);
      loadMoreButtonItem.loading = true;
      category.limit += this.pageSize;
      try {
        await this.refreshTree(category, Math.max(this.depth - category.depth, 1), 0, category.limit);
      } finally {
        window.setTimeout(() => {
          loadMoreButtonItem.loading = false;
        }, 50);
      }
    },
    getCategory(id) {
      if (id === this.categoryTree?.id) {
        return this.categoryTree;
      }
      return this.categories.find(cat => cat.id === id);
    },
    addSubcategories(item, result, depth, itemIndex) {
      if (!item) {
        return;
      }
      item.index = itemIndex || 0;
      result.push(item);
      item.depth = depth || 0;
      if (item?.categories) {
        item.categories.forEach((cat, index) => this.addSubcategories(cat, result, item.depth + 1, index));
        if (item.limit && item.size > item.limit && !item.categories.find(i => i.loadMore)) {
          item.categories.push({
            id: item.id + 100000,
            parentId: item.id,
            loading: false,
            loadMore: true,
            disabled: true,
          });
        }
      }
    },
    apply() {
      this.$emit('input', this.categoryIds);
      this.close();
    },
  },
};
</script>