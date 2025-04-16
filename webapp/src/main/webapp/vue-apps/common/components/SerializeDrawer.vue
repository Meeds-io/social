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
    ref="serializeDrawer"
    :loading="loading"
    body-classes="hide-scroll decrease-z-index-more"
    allow-expand
    right>
    <template #title>
      <slot name="title"></slot>
    </template>
    <template #content>
      <slot name="content"></slot>
      <div class="d-flex justify-center pt-5">
        <v-btn
          class="btn btn-primary"
          :disabled="loading"
          @click="startExport">
          {{ $t('databind.export') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data: () => ({
    loading: false,
    type: '',
    id: '',
  }),
  created() {
    this.$root.$on('serialize-drawer-open', this.open);
  },
  methods: {
    open(type, id) {
      this.type = type;
      this.id = id;
      this.$refs.serializeDrawer.open();
    },
    close() {
      this.$refs.serializeDrawer.close();
    },
    serialize() {
      const id = this.id && [this.id];
      const formData = new FormData();
      formData.append('objectType', this.type);
      if (id?.length) {
        id.forEach(id => formData.append('objectId', id));
      }
      const params = new URLSearchParams(formData).toString();
      return `/social/rest/databind/serialize?${params}`;
    },
    async startExport() {
      try {
        this.loading = true;
        const url = this.serialize();
        const response = await fetch(url);
        const blob = await response.blob();
        const downloadUrl = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = downloadUrl;
        link.download = `${this.type}.zip`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(downloadUrl);
      } catch (e) {
        this.$root.$emit('alert-message', this.$te(e.message) ? this.$t(e.message) : this.$t('databind.exportError'), 'error');
      } finally {
        this.loading = false;
        this.$emit('export-start');
      }
    },
  },
};
</script>
