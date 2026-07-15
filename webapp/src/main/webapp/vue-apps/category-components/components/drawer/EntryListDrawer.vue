<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

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
    id="EntryListDrawer"
    ref="drawer"
    v-model="drawer"
    right>
    <template #title>
      {{ $t('categoryEntry.drawer.title') }}
    </template>
    <template v-if="drawer" #content>
      <div class="d-flex flex-column">
        <categories-breadcrumb
          v-if="breadcrumb"
          :breadcrumb="breadcrumb"
          :selected-id="categoryId"
          class="pa-4"
          @select="selectBreadcrumb" />
        <div class="d-flex flex-column px-4">
          <category-entry-list-item
            v-for="item in items"
            :key="`${item.objectType}-${item.id}`"
            :item="item" />
        </div>
        <div
          v-if="hasMore"
          class="d-flex justify-center pa-4">
          <v-btn
            :loading="loadingMore"
            text
            @click="loadMore">
            {{ $t('categoryEntry.drawer.loadMore') }}
          </v-btn>
        </div>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    categoryId: null,
    objectTypes: [],
    breadcrumb: null,
    items: [],
    offset: 0,
    limit: 20,
    hasMore: false,
    loading: false,
    loadingMore: false,
  }),
  methods: {
    async open(categoryId, objectTypes) {
      this.categoryId = categoryId;
      this.objectTypes = objectTypes?.length && objectTypes || ['news', 'notes'];
      this.items = [];
      this.offset = 0;
      this.hasMore = false;
      this.breadcrumb = null;
      this.$refs.drawer.open();
      await Promise.all([
        this.loadBreadcrumb(),
        this.loadEntries(),
      ]);
    },
    close() {
      this.$refs.drawer.close();
    },
    async loadBreadcrumb() {
      const [category, ancestorIds] = await Promise.all([
        this.$categoryService.getCategory(this.categoryId),
        this.$categoryService.getAncestorIds(this.categoryId),
      ]);
      const ancestors = await Promise.all((ancestorIds || []).map(id => this.$categoryService.getCategory(id)));
      // The tree root category has no meaningful display name (the Home icon already represents it)
      this.breadcrumb = [...ancestors.reverse(), category].filter(cat => cat && cat.parentId !== 0);
    },
    async loadEntries() {
      this.loading = true;
      try {
        const data = await this.$categoryService.getCategoryEntries({
          categoryId: this.categoryId,
          types: this.objectTypes,
          offset: this.offset,
          limit: this.limit,
        });
        this.items = [...this.items, ...(data?.items || [])];
        this.hasMore = !!data?.hasMore;
      } finally {
        this.loading = false;
      }
    },
    async loadMore() {
      this.loadingMore = true;
      this.offset += this.limit;
      try {
        await this.loadEntries();
      } finally {
        this.loadingMore = false;
      }
    },
    selectBreadcrumb(category) {
      if (category) {
        this.open(category.id, this.objectTypes);
      } else {
        this.close();
      }
    },
  },
};
</script>
