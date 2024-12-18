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
    <div v-if="loading" class="position-relative z-index-two">
      <v-progress-linear
        indeterminate
        color="primary"
        class="position-absolute" />
    </div>
    <v-treeview
      v-if="hasItems"
      :items="categoryTreeItems"
      :open.sync="openItems"
      :search="keyword"
      :filter="filter"
      item-children="categories"
      item-key="id"
      item-text="name"
      hoverable
      activatable
      open-on-click
      transition
      dense>
      <template #prepend="{ item }">
        <v-icon size="28" class="me-1">{{ item.icon }}</v-icon>
      </template>
      <template #label="{ item }">
        <div class="d-flex align-center">
          <div class="me-auto">{{ item.name }}</div>
          <category-management-item-menu
            :category="item"
            class="ms-auto" />
        </div>
      </template>
    </v-treeview>
    <div v-else class="d-flex justify-center text-header full-width my-8 mx-5">
      {{ $t('catagoryManagement.noData') }}
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
    depth: 10,
  }),
  computed: {
    categoryTree() {
      return this.$root.categoryTree;
    },
    categoryTreeItems() {
      return this.categoryTree?.categories || [];
    },
    hasItems() {
      return this.categoryTreeItems?.length;
    },
    nameToDelete() {
      return this.categoryToDelete && this.$te(this.categoryToDelete?.name) ? this.$t(this.categoryToDelete?.name) : this.categoryToDelete?.name || '';
    },
  },
  created() {
    this.init();
    this.$root.$on('category-created', this.handleCategoryCreated);
    this.$root.$on('category-updated', this.handleCategoryUpdated);
    this.$root.$on('category-deleted', this.handleCategoryDeleted);
    this.$root.$on('category-delete', this.deleteCategoryConfirm);
  },
  beforeDestroy() {
    this.$root.$off('category-created', this.handleCategoryCreated);
    this.$root.$off('category-updated', this.handleCategoryUpdated);
    this.$root.$off('category-delete', this.deleteCategoryConfirm);
  },
  methods: {
    async init() {
      await this.refreshTree(this.categoryTree, this.depth);
      this.$root.categoryOwnerId = this.categoryTree?.ownerId;
      this.$root.categoryRootId = this.categoryTree?.id;
    },
    filter(item, search, textKey) {
      return item[textKey].indexOf(search) > -1;
    },
    async loadChildren(item) {
      await this.refreshTree(item, 1);
    },
    async refreshTree(item, depth) {
      const parentId = item?.id || this.$root.categoryRootId || 0;
      const ownerId = item?.ownerId || this.$root.categoryOwnerId || 0;
      this.loading = true;
      try {
        const categoryTree = await this.$categoryService.getCategoryTree({
          parentId,
          ownerId,
          depth,
        });
        if (!parentId) {
          this.$root.categoryTree = categoryTree;
        } else {
          Object.keys(categoryTree).forEach(key => item[key] = categoryTree[key]);
        }
      } finally {
        this.loading = false;
      }
    },
    handleCategoryCreated(item) {
      const parent = this.$root.getCategory(item.parentId);
      this.refreshTree(parent, this.depth - (parent.depth || 0));
    },
    handleCategoryUpdated(item) {
      const parent = this.$root.getCategory(item.parentId);
      this.refreshTree(parent, this.depth - (parent.depth || 0));
    },
    handleCategoryDeleted(item) {
      const parent = this.$root.getCategory(item.parentId);
      this.refreshTree(parent, this.depth - (parent.depth || 0));
    },
    deleteCategoryConfirm(category) {
      this.categoryToDelete = category;
      if (this.categoryToDelete) {
        this.$refs.deleteConfirmDialog.open();
      }
    },
    deleteCategory(category) {
      this.loading = true;
      this.$categoryService.deleteCategory(category.id)
        .then(() => {
          this.$root.$emit('category-deleted', category);
          this.$root.$emit('alert-message', this.$t('categoryManagement.categoryDeletedSuccessfully'), 'success');
        })
        .catch(() => this.$root.$emit('alert-message', this.$t('categoryManagement.categoryDeleteError'), 'error'))
        .finally(() => this.loading = false);
    },
  },
};
</script>
