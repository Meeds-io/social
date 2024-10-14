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
    v-model="initialized"
    allow-expand
    right
    disable-pull-to-refresh
    @closed="$emit('closed')">
    <template #title>
      <div class="d-flex flex-no-wrap align-center overflow-hidden">
        <v-icon size="18" class="me-2">fa-question-circle</v-icon>
        <div :title="title" class="text-truncate">{{ title }}</div>
      </div>
    </template>
    <template #content>
      <v-card class="pa-4" flat>
        <template v-if="$slots.content">
          <slot name="content"></slot>
        </template>
        <template v-else>
          {{ content }}
        </template>
      </v-card>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    titleKey: {
      type: String,
      default: null,
    },
    contentKey: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    initialized: false,
  }),
  computed: {
    title() {
      return this.$t(this.titleKey);
    },
    content() {
      return this.contentKey && this.$t(this.contentKey) || '';
    },
  },
  mounted() {
    window.setTimeout(() => this.initialized = true, 50);
  },
};
</script>
