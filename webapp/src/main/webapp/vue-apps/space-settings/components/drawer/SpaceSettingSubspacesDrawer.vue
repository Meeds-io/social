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
  <exo-drawer
    id="SubspacesDrawer"
    ref="drawer"
    v-model="drawer"
    :loading="loading || saving"
    right>
    <template #title>
      <div class="d-flex flex-no-wrap align-center overflow-hidden">
        {{ $t('spaceSetting.subspaces') }}
        <v-btn
          id="subspacesAddButton"
          :aria-label="$t('spaceSetting.subspaces.add')"
          class="btn btn-primary text-truncate ms-auto"
          @click="addSubspace">
          <span>
            {{ $t('spaceSetting.subspaces.add') }}
          </span>
        </v-btn>
      </div>
    </template>
    <template v-if="drawer" #content>
      <div v-if="displayedSubspaces">
        <space-setting-subspaces-item
          v-for="s in subspaces"
          :key="s.id"
          :space="s"
          @loading="loading = $event" />
      </div>
      <div v-else class="d-flex flex-column flex-grow-1 full-height">
        <div class="d-flex flex-column align-center justify-center my-auto">
          <v-icon class="tertiary--text" size="60">fas fa-layer-group</v-icon>
          <span class="ma-5">{{ $t('spaceSetting.noSubspaceYetCreated') }}</span>
        </div>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    loading: false,
    saving: false,
    spaceId: null,
    subspaces: [],
    dialog: false,
  }),
  computed: {
    displayedSubspaces() {
      return !!this.subspaces?.length;
    },
    templateId() {
      return this.$root.space?.templateId;
    },
  },
  created() {
    this.$root.$on('subspaces-list-refresh', this.retrieveSubspaces);
  },
  beforeDestroy() {
    this.$root.$off('subspaces-list-refresh', this.retrieveSubspaces);
  },
  methods: {
    open(spaceId) {
      this.spaceId = spaceId;
      this.retrieveSubspaces();
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    async retrieveSubspaces() {
      this.loading = true;
      try {
        const data = await this.$spaceService.getSpacesByFilter({
          filter: 'all',
          expand: 'managers,groupBinding',
          parentSpaceId: this.spaceId,
          offset: 0,
          limit: 25,
          sortBy: 'title',
          sortDirection: 'desc',
        });
        this.subspaces = data?.spaces || [];
      } finally {
        this.loading = false;
      }
    },
    async addSubspace() {
      const spaceTemplates = await this.$spaceTemplateService.getSpaceTemplates();
      const selectedTemplate = spaceTemplates.find(t => String(t.id) === String(this.templateId));
      const allowedSubspaceTemplatesIds = selectedTemplate.allowedSubspaceTemplates
        ?.map(item => item.split(':')[0]) || [];
      const allowedSubspaceTemplates = spaceTemplates.filter(t =>
        allowedSubspaceTemplatesIds.includes(String(t.id))
      );
      window.require(['SHARED/spaceForm'], drawer => drawer.open(null, allowedSubspaceTemplates, eXo.env.portal.spaceId));
    },
  },
};
</script>