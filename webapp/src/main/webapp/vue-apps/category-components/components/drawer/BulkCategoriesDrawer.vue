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
  <categories-drawer
    id="BulkCategoriesDrawer"
    ref="drawer"
    :form-modified="dropExisting"
    :summary1="summary1"
    :summary2="summary2"
    :summary3="summary3"
    @save="saveAll">
    <div
      class="mb-4">
      <v-chip
        class="mb-4 light-grey-color"
        height="40">
        <span>
          {{ $t('categoryInput.drawer.selectedObjectsCount', {
            0: selectionCount,
          }) }}
        </span>
      </v-chip>
      <div class="d-flex align-center">
        <div>{{ $t('categoryInput.drawer.dropExistingCategories') }}</div>
        <v-switch v-model="dropExisting" class="ms-auto my-0 me-n2" />
      </div>
    </div>
  </categories-drawer>
</template>
<script>
export default {
  data: () => ({
    saving: false,
    spaceId: null,
    objectType: null,
    objectIds: [],
    categoryIds: [],
    selectedCategoryIds: [],
    selectionCount: null,
    dropExisting: false,
    summary1: null,
    summary2: null,
    summary3: null,
  }),
  created() {
    document.addEventListener('bulk-edit-categories-drawer-open', this.openByEvent);
  },
  beforeDestroy() {
    document.removeEventListener('bulk-edit-categories-drawer-open', this.openByEvent);
  },
  methods: {
    openByEvent(event) {
      this.open(event?.detail);
    },
    open(params) {
      this.spaceId = params.spaceId;
      this.objectType = params.objectType;
      this.objectIds = params.objectIds || [];
      this.selectionCount = this.objectIds.length;
      this.categoryIds = params.categoryIds || [];
      this.dropExisting = false;
      this.summary1 = params.summary1;
      this.summary2 = params.summary2;
      this.summary3 = params.summary3;
      this.$refs.drawer.open(null, []);
    },

    async saveAll(categoriesIds) {
      this.selectedCategoryIds = categoriesIds;
      await Promise.all(this.objectIds.map(id => this.save(id)));
      this.$root.$emit('alert-message', this.$t('categoryInput.drawer.bulk.updated.success'), 'success');
      this.$refs.drawer.close();
      document.dispatchEvent(new CustomEvent('categories-updated', {detail: {
        objectType: this.objectType,
        objectIds: this.objectIds,
        spaceId: this.spaceId,
        categoryIds: this.selectedCategoryIds,
      }}));
    },

    async save(objectId) {
      this.saving = true;
      try {
        await this.$categoryLinkService.updateCategories({
          objectType: this.objectType,
          objectId: objectId,
          spaceId: this.spaceId,
          oldCategories: this.categoryIds,
          newCategories: this.selectedCategoryIds,
          dropExisting: this.dropExisting
        });
      } catch (e) {
        console.error(e);
        this.$root.$emit('alert-message', this.$t('categoryInputDrawer.updated.error'), 'error');
      } finally {
        this.saving = false;
      }
    },
  }
};
</script>