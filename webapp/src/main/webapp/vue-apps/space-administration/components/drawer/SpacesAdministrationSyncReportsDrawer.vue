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
    id="SpaceManagersDrawer"
    ref="drawer"
    v-model="drawer"
    :loading="loading"
    expanded
    no-x-scroll
    right>
    <template v-if="space" #title>
      {{ $t('social.spaces.administration.manageSpaces.bindingReportsOfSpace', {
        0: space.displayName
      }) }}
    </template>
    <template v-if="drawer" #content>
      <div class="pa-4">
        <spaces-administration-binding-report-list
          :operations="operations" />
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    loading: false,
    space: null,
    operations: null,
  }),
  computed: {
    modified() {
      return JSON.stringify(this.groups) !== JSON.stringify(this.originalGroups);
    },
  },
  created() {
    this.$root.$on('space-administration-sync-reports-drawer-open', this.open);
  },
  beforeDestroy() {
    this.$root.$off('space-administration-sync-reports-drawer-open', this.open);
  },
  methods: {
    async open(space) {
      this.space = space;
      this.$refs.drawer.open();
      this.refresh();
      await this.$nextTick();
    },
    async refresh() {
      this.loading = true;
      try {
        const data = await this.$spaceBindingService.getBindingReportOperations(this.space.id);
        this.operations = data.groupSpaceBindingReportOperations;
      } finally {
        this.loading = false;
      }
    },
    close() {
      this.$refs.drawer.close();
    },
  },
};
</script>