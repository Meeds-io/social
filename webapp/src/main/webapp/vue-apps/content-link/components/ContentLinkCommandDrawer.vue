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
    ref="drawer"
    v-model="drawer"
    :right="!$vuetify.rtl"
    :loading="loading"
    class="z-index-snackbar">
    <template #title>
      {{ $t('contentLink.drawer.title') }}
    </template>
    <template v-if="drawer && plugins.length" #content>
      <div class="d-flex flex-wrap py-5 px-3">
        <content-link-plugin
          v-for="p in plugins"
          :key="p.command"
          :plugin="p"
          class="mx-2 mb-4"
          @close="close" />
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
  }),
  computed: {
    plugins() {
      return this.$root.plugins?.filter?.(p => p && !p.hidden) || [];
    },
  },
  created() {
    this.$utils.includeExtensions('ContentLinkExtension');
  },
  methods: {
    open() {
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
  },
};
</script>