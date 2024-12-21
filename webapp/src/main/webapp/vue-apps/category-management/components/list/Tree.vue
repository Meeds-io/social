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
  <div class="overflow-hidden">
    <div v-if="loading" class="position-relative z-index-two">
      <v-progress-linear
        indeterminate
        color="primary"
        class="position-absolute" />
    </div>
    <div v-if="hasItems" class="overflow-hidden">
      <div class="d-flex align-center text-header my-2">
        <div>{{ $t('categoryManagement.table.column.name') }}</div>
        <v-spacer />
        <div>{{ $t('categoryManagement.table.column.actions') }}</div>
      </div>
      <v-divider class="full-width" />
      <v-treeview
        :items="categoryTreeItems"
        :open.sync="openItems"
        :search="keyword"
        :filter="filter"
        :load-children="$root.loadChildren"
        class="ms-n9"
        expand-icon=""
        item-children="categories"
        item-key="id"
        item-text="name"
        hoverable
        activatable
        open-on-click
        transition
        dense>
        <template #label="{ item, open }">
          <div v-if="!item.loadMore" class="d-flex align-center">
            <v-card
              color="transparent"
              min-width="36"
              flat>
              <v-icon
                v-show="item.size"
                :class="{
                  'fa-rotate-90': open && !$vuetify.rtl,
                  'fa-rotate-270': open && $vuetify.rtl,
                }"
                size="20">
                {{ $root.chevonIcon }}
              </v-icon>
            </v-card>
            <v-card
              class="d-flex align-center justify-center ms-1 me-2"
              color="transparent"
              min-width="28"
              flat>
              <v-icon size="28">{{ item.icon }}</v-icon>
            </v-card>
            <div class="text-truncate">{{ item.name }}</div>
            <category-management-item-menu :category="item" />
          </div>
          <div v-else class="d-flex align-center">
            <v-btn
              :title="$t('categoryInput.loadMore')"
              :loading="item.loading"
              color="transparent"
              class="ms-10 px-0"
              elevation="0"
              link
              @click.prevent.stop="$root.loadMore(item.parentId)">
              <span class="text-link">{{ $t('categoryInput.loadMore') }}</span>
            </v-btn>
          </div>
          <v-divider :class="$vuetify.rtl && 'r-0' || 'l-0'" class="position-absolute full-width b-0" />
        </template>
      </v-treeview>
    </div>
    <div v-else class="d-flex justify-center text-header full-width my-8 mx-5">
      {{ $t('categoryManagement.noData') }}
    </div>
    <exo-confirm-dialog
      ref="deleteConfirmDialog"
      :title="$t('categoryManagement.label.confirmDeleteTitle')"
      :message="$t('categoryManagement.label.confirmDeleteMessage', {0: `<br><strong>${nameToDelete}</strong>`})"
      :ok-label="$t('categoryManagement.label.confirm')"
      :cancel-label="$t('categoryManagement.label.cancel')"
      @ok="deleteCategory(categoryToDelete)"
      @closed="categoryToDelete = null" />
  </div>
</template>
<script>
export default {
  props: {
    keyword: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    openItems: [],
    loading: true,
    categoryToDelete: null,
  }),
  computed: {
    categoryTree() {
      return this.$root.categoryTree;
    },
    categoryTreeItems() {
      return this.categoryTree?.categories || [];
    },
    foundCategories() {
      return this.loading ? (this.$root.foundCategories || []) : this.$root.foundCategories;
    },
    hasItems() {
      return this.categoryTreeItems?.length;
    },
    nameToDelete() {
      return this.categoryToDelete && this.$te(this.categoryToDelete?.name) ? this.$t(this.categoryToDelete?.name) : this.categoryToDelete?.name || '';
    },
  },
  watch: {
    keyword() {
      if (this.keyword) {
        this.loading = true;
      }
    },
    async foundCategories() {
      if (this.foundCategories?.length) {
        if (this.openItemsInterval) {
          window.clearTimeout(this.openItemsInterval);
          this.openItemsInterval = null;
        }
        await this.$nextTick();
        this.openItemsInterval = window.setTimeout(() => {
          const openItems = this.openItems.slice();
          this.foundCategories.forEach(cat => {
            const category = cat?.id && this.$root.getCategory(cat.id);
            if (category && !openItems.find(id => id === cat.id)) {
              openItems.push(cat.id);
              cat.ancestorIds.forEach(ancestorId => {
                if (!openItems.find(id => id === ancestorId)) { // NOSONAR
                  openItems.push(ancestorId);
                }
              });
            }
          });
          if (this.openItems.length !== openItems) {
            this.openItems = openItems;
          }
        }, 50);
      }
    },
  },
  created() {
    this.init();
    this.$root.$on('category-created', this.handleCategoryCreated);
    this.$root.$on('category-updated', this.handleCategoryUpdated);
    this.$root.$on('category-deleted', this.handleCategoryDeleted);
    this.$root.$on('category-moved', this.handleCategoryMoved);
    this.$root.$on('category-delete', this.deleteCategoryConfirm);
  },
  beforeDestroy() {
    this.$root.$off('category-created', this.handleCategoryCreated);
    this.$root.$off('category-updated', this.handleCategoryUpdated);
    this.$root.$off('category-deleted', this.handleCategoryDeleted);
    this.$root.$off('category-moved', this.handleCategoryMoved);
    this.$root.$off('category-delete', this.deleteCategoryConfirm);
  },
  methods: {
    async init() {
      this.loading = true;
      try {
        await this.$root.refreshTree(this.categoryTree, this.$root.depth);
      } finally {
        this.loading = false;
      }
      this.$root.categoryOwnerId = this.categoryTree?.ownerId;
      this.$root.categoryRootId = this.categoryTree?.id;
      this.openItems = this.$root.categories.map(c => c.id);
    },
    filter(item, search, textKey) {
      return (item[textKey] && item[textKey].indexOf(search) > -1) || this.$root.foundCategories?.find?.(cat => cat.id === item.id);
    },
    async handleCategoryCreated(item) {
      const parentCategory = this.$root.getCategory(item.parentId);
      if (parentCategory) {
        if (parentCategory.limit) {
          const category = await this.$categoryService.getCategory(item.id);
          if (!parentCategory.categories?.length) {
            parentCategory.categories = [category];
          } else {
            const index = parentCategory.categories.findIndex(cat => category.name.localeCompare(cat.name) <= 0);
            if (index >= 0) {
              parentCategory.categories.splice(index, 0, category);
            } else {
              parentCategory.categories.push(category);
            }
            parentCategory.limit++;
          }
        }
        parentCategory.size++;
      }
    },
    async handleCategoryUpdated(item) {
      item = await this.$categoryService.getCategory(item.id);
      const category = this.$root.getCategory(item.id);
      category.name = item.name;
      category.icon = item.icon;
      category.linkPermissionIds = item.linkPermissionIds;
      category.accessPermissionIds = item.accessPermissionIds;
    },
    handleCategoryDeleted(item) {
      const parentCategory = this.$root.getCategory(item.parentId);
      if (parentCategory.limit && parentCategory.categories?.length) {
        const index = parentCategory.categories.findIndex(cat => cat.id === item.id);
        parentCategory.categories.splice(index, 1);
        parentCategory.limit--;
        parentCategory.size--;
      }
    },
    handleCategoryMoved(item, fromCategory) {
      this.handleCategoryDeleted({
        id: item.id,
        parentId: fromCategory.id,
      });
      this.handleCategoryCreated(item);
    },
    deleteCategoryConfirm(category) {
      this.categoryToDelete = category;
      if (this.categoryToDelete) {
        this.$refs.deleteConfirmDialog.open();
      }
    },
    // Called from parent
    startLoading() {
      this.loading = true;
    },
    // Called from parent
    endLoading() {
      this.loading = false;
    },
    async deleteCategory(category) {
      this.loading = true;
      try {
        await this.$categoryService.deleteCategory(category.id);
        this.$root.$emit('category-deleted', category);
        this.$root.$emit('alert-message', this.$t('categoryManagement.categoryDeletedSuccessfully'), 'success');
      } catch (e) {
        this.$root.$emit('alert-message', this.$t('categoryManagement.categoryDeleteError'), 'error');
      } finally {
        this.loading = false;
      }
    },
  },
};
</script>
