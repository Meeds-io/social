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
    class="d-flex flex-column"
    color="transparent"
    flat>
    <div class="d-flex align-start overflow-hidden full-width mb-2">
      <slot
        v-if="$scopedSlots.label"
        name="label"
        :open-categories-drawer="openCategoriesDrawer"
        :has-categories="hasCategories"
        :categories="sortedCategories"></slot>
      <div
        v-else
        :class="labelClass"
        class="flex-grow-1 flex-shrink-1 text-truncate">
        {{ $t(label) }}
      </div>
      <v-btn
        v-if="!hideAddButton && initialized"
        :title="actionLabel"
        :aria-label="actionLabel"
        class="category-input-add-button flex-grow-0 flex-shrink-0 ms-2"
        small
        icon
        @click="openCategoriesDrawer">
        <v-icon size="16">{{ hasCategories && 'fa-edit' || 'fa-plus' }}</v-icon>
      </v-btn>
    </div>
    <v-progress-linear
      v-if="loading"
      color="primary"
      indeterminate />
    <v-list
      v-if="hasCategories && !hideCategoriesList"
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
            v-if="item.canLink"
            :title="$t('categoryInput.deleteCategory')"
            min-width="16"
            color="error"
            icon
            small
            @click="removeCategory(item)">
            <v-icon size="16">fa-trash</v-icon>
          </v-btn>
          <v-tooltip v-else bottom>
            <template #activator="{on, attrs}">
              <v-btn
                v-on="on"
                v-bind="attrs"
                :ripple="false"
                min-width="16"
                tag="div"
                plain
                icon
                small>
                <v-icon size="16">fa-lock</v-icon>
              </v-btn>
            </template>
            <span>{{ $t('categoryInput.restrictedCategoryLink') }}</span>
          </v-tooltip>
        </v-list-item-action>
      </v-list-item>
    </v-list>
    <category-input-drawer
      v-if="drawer"
      ref="drawer"
      v-model="categoryIds"
      :selected-categories="categories"
      :access-permission="accessPermission"
      @closed="drawer = false" />
  </v-card>
</template>
<script>
export default {
  props: {
    value: {
      type: Array,
      default: null,
    },
    label: {
      type: String,
      default: () => 'categoryInput.drawer.manageCategories',
    },
    labelClass: {
      type: String,
      default: () => 'text-header',
    },
    accessPermission: {
      type: Boolean,
      default: false,
    },
    filterPreselection: {
      type: Boolean,
      default: false,
    },
    single: {
      type: Boolean,
      default: false,
    },
    hideAddButton: {
      type: Boolean,
      default: false,
    },
    hideCategoriesList: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    drawer: false,
    loading: true,
    initialized: false,
    refreshIndex: 0,
    lockedIds: [],
    categoryIds: [],
    categories: [],
    collator: new Intl.Collator(eXo.env.portal.language, {numeric: true, sensitivity: 'base'}),
  }),
  computed: {
    filteredCategories() {
      return this.categories?.filter?.(cat => cat && cat.name && !cat.locked) || [];
    },
    sortedCategories() {
      return this.filteredCategories.slice().sort(this.comparator);
    },
    hasCategories() {
      return !!this.filteredCategories.length;
    },
    /**
     * The title and accessible name of the label line's button: it adds
     * categories while none is chosen, and edits them once some are.
     *
     * @returns {string} the translated « Add categories » or « Edit associated categories »
     */
    actionLabel() {
      return this.hasCategories
        && this.$t('categoryInput.drawer.editCategories')
        || this.$t('categoryInput.addCategories');
    },
  },
  watch: {
    categoryIds: {
      immediate: true,
      handler() {
        const categoryIds = this.categoryIds.slice();
        this.lockedIds.forEach(id => {
          if (categoryIds.indexOf(id) < 0) {
            categoryIds.push(id);
          }
        });
        this.$emit('input', categoryIds);
        if (categoryIds.length !== this.categoryIds.length) {
          this.categoryIds = categoryIds;
        }
        this.refreshCategories();
      },
    },
  },
  created() {
    this.categoryIds = this.value?.slice?.() || [];
  },
  methods: {
    async openCategoriesDrawer() {
      this.drawer = true;
      await this.$nextTick();
      this.$refs.drawer.openDrawer();
    },
    /**
     * Loads the categories of the current ids. Several refreshes can overlap
     * (the immediate watcher runs on the empty default before `created` sets
     * the stored ids), so only the latest one applies its result, ends the
     * loading state and marks the input initialized.
     *
     * @returns {Promise<void>} resolved once the categories are loaded
     */
    async refreshCategories() {
      const refreshIndex = ++this.refreshIndex;
      this.loading = true;
      try {
        const categories = await Promise.all(this.categoryIds.map(id => this.$categoryService.getCategory(id)
          .then(c => {
            if (c
              && !c.canLink
              && !this.accessPermission
              && this.filterPreselection) {
              return null;
            } else {
              return c;
            }
          })
          .catch(() => {
            this.lockedIds.push(id);
            return this.filterPreselection ? null : {
              id,
              locked: true,
            };
          })));
        await this.$nextTick();
        if (refreshIndex !== this.refreshIndex) {
          return;
        }
        this.categories = categories.filter(c => c);
        if (this.filterPreselection
            && this.categories?.length !== this.categoryIds?.length) {
          this.categoryIds = this.categories.map(c => c.id);
        }
      } finally {
        if (refreshIndex === this.refreshIndex) {
          this.loading = false;
          this.initialized = true;
        }
      }
    },
    removeCategory(item) {
      if (item.canLink && !item.locked) {
        this.categoryIds.splice(this.categoryIds.indexOf(item.id), 1);
        this.refreshCategories();
      }
    },
    comparator(a, b) {
      return this.collator.compare(a.name, b.name);
    },
  },
};
</script>