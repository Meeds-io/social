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
  <space-categories-drawer
    id="SpacesAdministrationCategoriesDrawer"
    ref="drawer"
    :form-modified="dropExisting"
    @save="saveSpaces">
    <space-avatar
      v-if="space"
      :space="space"
      class="mb-4" />
    <div
      v-else-if="spaces"
      class="mb-4">
      <v-chip
        class="mb-4 light-grey-color"
        height="40">
        <span>
          {{ $t('social.spaces.administration.manageSpaces.selectedSpacesCount', {
            0: selectionCount,
          }) }}
        </span>
      </v-chip>
      <div class="d-flex align-center">
        <div>{{ $t('social.spaces.administration.manageSpaces.dropExistingCategories') }}</div>
        <v-switch v-model="dropExisting" class="ms-auto my-0 me-n2" />
      </div>
    </div>
  </space-categories-drawer>
</template>
<script>
export default {
  data: () => ({
    saving: false,
    space: null,
    spaces: null,
    categoryIds: [],
    selectionCount: null,
    callback: null,
    dropExisting: false,
  }),
  created() {
    this.$root.$on('space-administration-edit-categories-drawer-open', this.open);
    this.$root.$on('space-categories-updated', this.handleSpaceUpdated);
  },
  beforeDestroy() {
    this.$root.$off('space-administration-edit-categories-drawer-open', this.open);
    this.$root.$off('space-categories-updated', this.handleSpaceUpdated);
  },
  methods: {
    open(obj, selectionCount, callback) {
      if (obj?.id) {
        this.space = obj;
        this.spaces = null;
        this.categoryIds = this.space.categoryIds || [];
        this.selectionCount = 0;
        this.callback = null;
      } else {
        this.space = null;
        this.spaces = obj;
        this.categoryIds = [];
        this.selectionCount = selectionCount;
        this.callback = callback;
      }
      this.dropExisting = false;
      this.$refs.drawer.open(this.space?.id, this.categoryIds);
    },
    handleSpaceUpdated(_, categoryIds) {
      this.space.categoryIds = categoryIds;
    },
    saveSpaces(categoryIds) {
      this.callback({
        categoryIds,
        dropExisting: this.dropExisting,
      });
      this.$refs.drawer.close();
    },
  },
};
</script>