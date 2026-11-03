<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

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
      :title="$t('spaceSetting.subspaces')"
      :summary="summary">
      <template #action>
        <v-btn
          small
          icon
          @click="openDrawer">
          <v-icon size="18" class="icon-default-color">fa-edit</v-icon>
        </v-btn>
      </template>
    </space-setting-row>
    <space-setting-subspaces-drawer ref="subspacesDrawer" />
  </div>
</template>
<script>
export default {
  data: () => ({
    subspacesCount: null,
  }),
  computed: {
    /**
     * @returns {String} the number of subspaces, once counted
     */
    summary() {
      if (this.subspacesCount === null) {
        return null;
      } else if (!this.subspacesCount) {
        return this.$t('spaceSetting.subspaces.summary.none');
      } else if (this.subspacesCount === 1) {
        return this.$t('spaceSetting.subspaces.summary.one');
      } else {
        return this.$t('spaceSetting.subspaces.summary.many', {0: this.subspacesCount});
      }
    },
  },
  created() {
    this.$root.$on('subspaces-list-refresh', this.countSubspaces);
    this.countSubspaces();
  },
  beforeDestroy() {
    this.$root.$off('subspaces-list-refresh', this.countSubspaces);
  },
  methods: {
    /**
     * Counts the subspaces with the query the subspaces drawer lists them with,
     * reading only the size of its answer.
     *
     * @returns {Promise} resolved once counted
     */
    async countSubspaces() {
      const data = await this.$spaceService.getSpacesByFilter({
        filter: 'all',
        parentSpaceId: this.$root.space.id,
        offset: 0,
        limit: 1,
      });
      this.subspacesCount = data?.size || 0;
    },
    /**
     * Opens the drawer listing the subspaces.
     *
     * @returns {void}
     */
    openDrawer() {
      this.$refs.subspacesDrawer.open(this.$root.space.id);
    },
  },
};
</script>
