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
  <div v-if="$root.isAllSections">
    <space-setting-row
      :title="$t('SpaceSettings.categories')"
      :summary="summary">
      <template #action>
        <v-btn
          :title="$t('SpaceSettings.categories.button.tooltip')"
          small
          icon
          @click="openDrawer">
          <v-icon size="18" class="icon-default-color">fa-edit</v-icon>
        </v-btn>
      </template>
    </space-setting-row>
    <categories-drawer ref="categoriesDrawer" />
  </div>
</template>
<script>
export default {
  computed: {
    /**
     * @returns {Number} the number of categories the space is tagged with
     */
    categoriesCount() {
      return this.$root.space.categoryIds?.length || 0;
    },
    /**
     * @returns {String} the number of categories, as one line
     */
    summary() {
      if (!this.categoriesCount) {
        return this.$t('SpaceSettings.categories.summary.none');
      } else if (this.categoriesCount === 1) {
        return this.$t('SpaceSettings.categories.summary.one');
      } else {
        return this.$t('SpaceSettings.categories.summary.many', {0: this.categoriesCount});
      }
    },
  },
  methods: {
    /**
     * Opens the drawer editing the categories of the space.
     *
     * @returns {void}
     */
    openDrawer() {
      this.$refs.categoriesDrawer.open(this.$root.space.id, this.$root.space.categoryIds);
    },
  },
};
</script>
