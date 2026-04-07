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
    id="CategoriesDrawer"
    ref="drawer"
    v-model="drawer"
    :loading="loading || saving"
    allow-expand
    right
    @closed="closed">
    <template #title>
      {{ $t('categoryInput.drawer') }}
    </template>
    <template v-if="drawer" #content>
      <div class="d-flex flex-column ma-4">
        <div class="mb-2">
          {{ summary1 || $t('categoryInput.drawer.summary1') }}
        </div>
        <div class="mb-2">
          {{ summary2 || $t('categoryInput.drawer.summary2') }}
        </div>
        <div class="mb-4">
          {{ summary3 || $t('categoryInput.drawer.summary3') }}
        </div>
        <slot></slot>
        <category-input v-model="selectedCategoryIds" />
      </div>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :disabled="saving"
          class="btn me-2"
          @click="close">
          {{ $t('categoryInput.cancel') }}
        </v-btn>
        <v-btn
          :disabled="!modified"
          :loading="saving"
          class="btn btn-primary"
          @click.prevent.stop="save">
          {{ $t('categoryInput.update') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    formModified: {
      type: Boolean,
      default: false,
    },
    summary1: {
      type: String,
      default: null,
    },
    summary2: {
      type: String,
      default: null,
    },
    summary3: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    drawer: false,
    loading: false,
    saving: false,
    spaceId: null,
    categoryIds: null,
    selectedCategoryIds: null,
  }),
  computed: {
    modified() {
      return this.formModified || JSON.stringify(this.selectedCategoryIds) !== JSON.stringify(this.categoryIds);
    },
  },
  methods: {
    open(spaceId, categoryIds) {
      this.spaceId = spaceId;
      this.categoryIds = categoryIds || [];
      this.selectedCategoryIds = this.categoryIds.slice();
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    closed() {
      document.dispatchEvent(new CustomEvent('categories-drawer-closed'));
    },
    async save() {
      if (!this.spaceId) {
        this.$emit('save', this.selectedCategoryIds);
        return;
      } else {
        this.saving = true;
        try {
          await this.$categoryLinkService.updateCategories({
            objectType: 'space',
            objectId: this.spaceId,
            spaceId: this.spaceId,
            oldCategories: this.categoryIds,
            newCategories: this.selectedCategoryIds,
            dropExisting: true
          });
          this.categoryIds = this.selectedCategoryIds;
          this.$root.$emit('space-categories-updated', this.spaceId, this.categoryIds);
          this.$root.$emit('alert-message', this.$t('categoryInput.updated.success'), 'success');
          this.close();
        } catch (e) {
          this.$root.$emit('alert-message', this.$t('categoryInput.updated.error'), 'error');
        } finally {
          this.saving = false;
        }
      }
    },
  },
};
</script>