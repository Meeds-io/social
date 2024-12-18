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
  <v-treeview
    v-if="hasItems"
    :items="categoryTreeItems"
    :open.sync="openItems"
    :search="keyword"
    :filter="filter"
    :load-children="loadChildren"
    item-children="categories"
    item-key="id"
    item-text="name">
    <template #prepend="{ item }">
      <v-icon
        v-if="item.children"
        v-text="item.icon || 'fa-folder'" />
    </template>
  </v-treeview>
  <div v-else class="d-flex justify-center text-header full-width my-8 mx-5">
    {{ $t('catagoryManagement.noData') }}
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
    categoryTree: {},
    openItems: null,
    loading: true,
  }),
  computed: {
    categoryTreeItems() {
      return this.categoryTree?.categories || [];
    },
    hasItems() {
      return this.categoryTreeItems?.length;
    },
  },
  created() {
    this.refreshTree(this.categoryTree, 4);
  },
  methods: {
    filter(item, search, textKey) {
      return item[textKey].indexOf(search) > -1;
    },
    async loadChildren(item) {
      this.refreshTree(item, 1);
    },
    async refreshTree(item, depth) {
      const categoryTree = await this.$categoryService.getCategoryTree({
        parentId,
        depth,
      });
      Object.assign(item, categoryTree);
    },
  },
};
</script>
