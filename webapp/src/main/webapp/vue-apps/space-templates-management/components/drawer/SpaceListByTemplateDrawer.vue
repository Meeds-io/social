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
    ref="drawer"
    v-model="drawer"
    :loading="loading"
    class="spacesListByTemplateDrawer"
    right
    allow-expand>
    <template #title>
      {{ $t('spaceTemplate.templateSpacesDrawer.title', {
        0: templateName
      }) }}
    </template>
    <template #titleIcons>
      <div class="full-height d-flex align-center">
        <v-btn
          v-if="role !== 'pending'"
          :title="$t('spaceTemplate.addSpaceTooltip')"
          color="primary"
          elevation="0"
          small
          @click="$root.$emit('addNewSpace', templateId)">
          <v-icon
            color="while"
            class="me-2"
            size="18">
            fa-plus
          </v-icon>
          {{ $t('spaceTemplate.addSpaceButtonLabel') }}
        </v-btn>
      </div>
    </template>
    <template #content>
      <div class="pa-5">
        <div class="mb-5">
          {{ $t('spaceTemplate.templateSpacesDrawer.description') }}
        </div>
        <space-avatar
          v-for="s in spaces"
          :key="s.id"
          :space="s"
          class="mb-5"
          list-style />
      </div>
    </template>
    <template v-if="hasMore" #footer>
      <v-btn
        :loading="loading"
        block
        class="btn pa-0"
        @click="loadMore">
        {{ $t('spaceTemplate.templateSpacesDrawer.loadMore') }}
      </v-btn>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    loading: false,
    offset: 0,
    pageSize: 10,
    spaces: null,
    size: 0,
    templateId: null,
    templateName: null,
  }),
  computed: {
    hasMore() {
      return this.spaces?.length && this.spaces?.length < this.size;
    },
  },
  created() {
    this.$root.$on('space-list-by-template-open', this.open);
  },
  beforeDestroy() {
    this.$root.$off('space-list-by-template-open', this.open);
  },
  methods: {
    open(templateId, templateName) {
      this.templateId = templateId;
      this.templateName = templateName;
      this.retrieveList();
      this.$refs.drawer.open();
    },
    async retrieveList(append) {
      if (append) {
        this.offset += this.pageSize;
      } else {
        this.spaces = null;
        this.size = 0;
      }
      this.loading = true;
      try {
        const data = await this.$spaceService.getSpaces(null, this.offset, this.pageSize, null, null, this.templateId);
        if (append) {
          this.spaces.push(...data.spaces);
        } else {
          this.spaces = data.spaces || [];
        }
        this.size = data.size;
      } finally {
        this.loading = false;
      }
    },
    loadMore() {
      this.retrieveList(true);
    },
  },
};
</script>