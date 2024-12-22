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
  <v-btn
    :disabled="$root.isBulkProcessing"
    color="primary"
    elevation="0"
    outlined
    @click="$root.$emit('space-administration-edit-categories-drawer-open', $root.selectedSpaces, $root.allSpacesSelected ? $root.spacesSize : $root.selectedSpaces.length, saveCategories)">
    <v-icon size="16" class="me-2">fa-th-large</v-icon>
    {{ $t('social.spaces.administration.manageSpaces.editCategories') }}
  </v-btn>
</template>
<script>
export default {
  methods: {
    saveCategories(params) {
      this.$root.applyOperationInBulk(
        async (space) => {
          const oldCategoryIds = space.categoryIds || [];
          const newCategoryIds = params.categoryIds?.slice?.() || [];
          if (!params.dropExisting) {
            oldCategoryIds.forEach(id => {
              if (newCategoryIds.indexOf(id) < 0) {
                newCategoryIds.push(id);
              }
            });
          }
          await this.$spaceCategoryService.updateCategories(space.id, oldCategoryIds, newCategoryIds);
          space.categoryIds = newCategoryIds;
        },
        null,
        () => {
          this.$root.$emit('alert-message', this.$root.$t('social.spaces.administration.manageSpaces.categoriesAppliedOnSpaces'), 'success');
        });
    },
  },
};
</script>