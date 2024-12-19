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
          <div class="d-flex align-center">
            <div class="d-flex me-2">
              <v-btn
                :disabled="!item.hasSubcategories"
                icon>
                <v-icon
                  v-show="item.hasSubcategories"
                  :class="{
                    'fa-rotate-90': open && !$vuetify.rtl,
                    'fa-rotate-270': open && $vuetify.rtl,
                  }"
                  size="20">
                  {{ $root.chevonIcon }}
                </v-icon>
              </v-btn>
              <v-icon size="28" class="ms-2 me-1">{{ item.icon }}</v-icon>
            </div>
            <div class="text-truncate">{{ item.name }}</div>
            <category-management-item-menu :category="item" />
          </div>
          <v-divider :class="$vuetify.rtl && 'r-0' || 'l-0'" class="position-absolute full-width b-0" />
        </template>
      </v-treeview>
    </div>
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
    this.$root.$on('category-created', this.handleCategoryRefresh);
    this.$root.$on('category-updated', this.handleCategoryRefresh);
    this.$root.$on('category-deleted', this.handleCategoryRefresh);
    this.$root.$on('category-delete', this.deleteCategoryConfirm);
  },
  beforeDestroy() {
    this.$root.$off('category-created', this.handleCategoryRefresh);
    this.$root.$off('category-updated', this.handleCategoryRefresh);
    this.$root.$off('category-deleted', this.handleCategoryRefresh);
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
      return item[textKey].indexOf(search) > -1;
    },
    async handleCategoryRefresh(item) {
      const parent = item.parentId && this.$root.getCategory(item.parentId) || item;
      this.loading = true;
      try {
        await this.$root.refreshTree(parent, this.$root.depth - (parent.depth || 0));
      } finally {
        this.loading = false;
      }
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
