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
    :loading="saving"
    class="categoryMoveDrawer"
    allow-expand
    right>
    <template #title>
      {{ $t('catagoryManagement.drawer.moveCategory') }}
    </template>
    <template v-if="drawer && category" #content>
      <div class="pa-5">
        <div class="mb-2 text-header">{{ $t('categoryManagement.moveCategoryDrawer.category') }}</div>
        <div class="mb-4 font-weight-bold">{{ category.name }}</div>
        <div class="mb-2 text-header">{{ $t('categoryManagement.moveCategoryDrawer.currentPosition') }}</div>
        <v-card
          class="d-flex flex-wrap mb-4"
          color="transparent"
          min-height="24"
          flat>
          <div
            v-for="(item, index) in currentBreadcrumb"
            :key="item.id"
            class="d-flex flex-row align-center">
            <v-icon
              v-if="index > 0"
              size="16"
              class="mx-2 text--disabled">
              {{ $root.chevonIcon }}
            </v-icon>
            <v-icon v-if="index === 0" size="16">fa-home</v-icon>
            <v-icon v-else size="16">{{ item.icon }}</v-icon>
            <div class="ms-1">{{ item.name }}</div>
          </div>
        </v-card>
        <div class="mb-2 text-header">{{ $t('categoryManagement.moveCategoryDrawer.destinationPosition') }}</div>
        <v-card
          class="d-flex flex-wrap mb-4"
          color="transparent"
          min-height="24"
          flat>
          <div
            v-for="(item, index) in destinationBreadcrumb"
            :key="item.id"
            class="d-flex flex-row align-center">
            <v-icon
              v-if="index > 0"
              size="16"
              class="mx-2 text--disabled">
              {{ $root.chevonIcon }}
            </v-icon>
            <v-icon v-if="index === 0" size="16">fa-home</v-icon>
            <v-icon v-else size="16">{{ item.icon }}</v-icon>
            <div class="ms-1">{{ item.name }}</div>
          </div>
        </v-card>
        <div class="mb-2 text-header">{{ $t('categoryManagement.moveCategoryDrawer.position') }}</div>
        <div class="overflow-hidden">
          <v-treeview
            :active.sync="activeIds"
            :open.sync="openedIds"
            :items="categoryTreeItems"
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
            <template #label="{ item, open, active }">
              <div v-if="item.id !== category.id" class="d-flex align-center">
                <v-btn
                  :disabled="!item.hasSubcategories"
                  icon>
                  <v-icon
                    v-show="item.hasSubcategories"
                    :class="{
                      'fa-rotate-90': open && !$vuetify.rtl,
                      'fa-rotate-270': open && $vuetify.rtl,
                    }"
                    size="12">
                    {{ $root.chevonIcon }}
                  </v-icon>
                </v-btn>
                <v-card
                  class="d-flex align-center flex-grow-1"
                  color="transparent"
                  height="36"
                  flat
                  @keypress.enter="setActive(item)"
                  @click.prevent.stop="setActive(item)">
                  <v-icon size="16" class="me-1">{{ item.id === $root.categoryRootId && 'fa-home' || item.icon }}</v-icon>
                  <div
                    :class="active && 'primary--text font-weight-bold'"
                    class="text-truncate">
                    {{ item.id === $root.categoryRootId && $t('categoryManagement.rootName') || item.name }}
                  </div>
                </v-card>
              </div>
            </template>
          </v-treeview>
        </div>
      </div>
    </template>
    <template #footer>
      <div class="d-flex align-center">
        <v-btn
          :title="$t('categoryManagement.cancel')"
          class="btn ms-auto me-2"
          @click="close()">
          {{ $t('categoryManagement.cancel') }}
        </v-btn>
        <v-btn
          :disabled="disabled"
          :loading="saving"
          class="btn primary"
          @click="move">
          {{ $t('categoryManagement.move') }}
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
    category: null,
    parent: null,
    activeIds: null,
    openedIds: null,
    categoryTree: null,
  }),
  computed: {
    currentBreadcrumb() {
      return this.category && this.getBreadcrumb(this.category);
    },
    destinationBreadcrumb() {
      const destinationCategory = this.category && JSON.parse(JSON.stringify(this.category));
      if (destinationCategory && this.destinationParent) {
        destinationCategory.parentId = this.destinationParent.id;
      }
      return destinationCategory.parentId && this.getBreadcrumb(destinationCategory) || [];
    },
    categoryTreeItems() {
      const categories = this.$root.categoryTree && [this.$root.categoryTree] || [];
      return this.filterTree(JSON.parse(JSON.stringify(categories)));
    },
    destinationParentId() {
      return this.activeIds?.[0];
    },
    destinationParent() {
      return this.activeIds?.[0] && this.$root.getCategory(this.activeIds?.[0]);
    },
    disabled() {
      return !this.destinationParentId || this.destinationParentId === this.category?.parentId;
    },
  },
  created() {
    this.$root.$on('category-move-open', this.openDrawer);
  },
  beforeDestroy() {
    this.$root.$off('category-move-open', this.openDrawer);
  },
  methods: {
    openDrawer(category) {
      this.parent = this.$root.getCategory(category.parentId);
      this.activeIds = [this.parent.id];
      this.category = category;
      this.openedIds = this.currentBreadcrumb.map(cat => cat.id);
      this.$refs.drawer.open();
    },
    async move() {
      this.category.parentId = this.destinationParentId;
      this.saving = true;
      try {
        await this.$categoryService.updateCategory(this.category);
        this.$root.$emit('alert-message', this.$t('categoryManagement.categoryMovedSuccessfully'), 'success');
        this.$root.$emit('category-updated', this.parent);
        this.$root.$emit('category-updated', this.destinationParent);
        this.close();
      } catch (e) {
        this.$root.$emit('alert-message', this.$t('categoryManagement.categoryMovedError'), 'success');
      } finally {
        this.saving = false;
      }
    },
    close() {
      this.$refs.drawer.close();
      this.category = null;
    },
    getBreadcrumb(category) {
      if (!category) {
        return [];
      }
      const breadcrumb = [];
      do {
        const parentCategory = this.$root.getCategory(breadcrumb?.[0]?.parentId || category.parentId);
        breadcrumb.unshift(parentCategory);
      } while (breadcrumb[0].id !== this.$root.categoryRootId);
      return breadcrumb;
    },
    filterTree(categories) {
      return categories
        .filter(cat => cat?.id && cat?.id !== this.category?.id)
        .map(cat => {
          const filteredCategories = this.filterTree(cat.categories);
          if (cat.categories?.length && !filteredCategories?.length) {
            cat.hasSubcategories = false;
          }
          cat.categories = filteredCategories;
          return cat;
        });
    },
    setActive(category) {
      this.activeIds = [category.id];
    },
  },
};
</script>