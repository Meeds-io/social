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
  <v-card
    :loading="loading"
    class="d-flex flex-column"
    color="transparent"
    flat>
    <div class="d-flex overflow-hidden full-width mb-2">
      <div class="text-header flex-grow-1 flex-shrink-1 text-truncate">
        {{ $t('SpaceSettings.editCategories.drawer.manageCategories') }}
      </div>
      <v-btn
        v-if="hasCategories"
        :title="$t('SpaceSettings.editCategories.drawer.editCategories')"
        class="flex-grow-0 flex-shrink-0"
        icon
        @click="openCategoriesDrawer">
        <v-icon size="16">fa-edit</v-icon>
      </v-btn>
    </div>
    <v-list
      v-if="hasCategories"
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
    <v-card
      v-else
      class="d-flex justify-center align-center my-4"
      width="100%"
      flat>
      <v-btn
        class="btn btn-primary flex-grow-0 flex-shrink-0"
        @click="openCategoriesDrawer">
        <v-icon size="16">fa-plus</v-icon>
        {{ $t('SpaceSettings.editCategories.drawer.addCategories') }}
      </v-btn>
    </v-card>
    <category-input-drawer
      v-if="drawer"
      ref="drawer"
      v-model="categoryIds"
      :selected-categories="categories" />
  </v-card>
</template>
<script>
export default {
  props: {
    value: {
      type: Array,
      default: null,
    },
  },
  data: () => ({
    drawer: false,
    loading: false,
    categoryIds: [],
    categories: [],
    collator: new Intl.Collator(eXo.env.portal.language, {numeric: true, sensitivity: 'base'}),
  }),
  computed: {
    hasCategories() {
      return !!this.categoryIds?.length;
    },
    sortedCategories() {
      return this.categories?.slice?.()?.sort?.(this.comparator);
    },
  },
  watch: {
    categoryIds: {
      immediate: true,
      handler() {
        this.$emit('input', this.categoryIds);
        this.refreshCategories();
      },
    },
  },
  created() {
    this.categoryIds = this.value?.slice?.() || [];
    this.refreshCategories();
  },
  methods: {
    async openCategoriesDrawer() {
      this.drawer = true;
      await this.$nextTick();
      this.$refs.drawer.openDrawer();
    },
    async refreshCategories() {
      this.loading = true;
      try {
        this.categories = await Promise.all(this.categoryIds.map(id => this.$categoryService.getCategoryTree({
          parentId: id,
        })));
      } finally {
        this.loading = false;
      }
    },
    removeCategory(item) {
      this.categoryIds.splice(this.categoryIds.indexOf(item.id), 1);
      this.refreshCategories();
    },
    comparator(a, b) {
      return this.collator.compare(a.name, b.name);
    },
  },
};
</script>