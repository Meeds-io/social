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
    :right="!$vuetify.rtl"
    :left="$vuetify.rtl"
    no-x-scroll>
    <template #title>
      {{ $t(isNew && 'generalSettings.addSideBarItemLink.drawerTitle' || 'generalSettings.updateSideBarItemLink.drawerTitle') }}
    </template>
    <template v-if="drawer" #content>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          class="btn me-2"
          @click="close">
          {{ $t('generalSettings.cancel') }}
        </v-btn>
        <v-btn
          :loading="saving"
          :disabled="!modified"
          class="btn-primary"
          elevation="0"
          @click="apply">
          {{ $t(isNew && 'generalSettings.add' || 'generalSettings.update') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    settings: null,
    item: null,
  }),
  computed: {
    isNew() {
      return this.settings?.sidebar?.items?.indexOf?.(this.item) < 0;
    },
  },
  created() {
    this.$root.$on('sidebar-item-add-link', this.open);
    this.$root.$on('sidebar-item-edit-link', this.open);
  },
  beforeDestroy() {
    this.$root.$off('sidebar-item-add-link', this.open);
    this.$root.$off('sidebar-item-edit-link', this.open);
  },
  methods: {
    open(settings, item) {
      this.settings = settings;
      this.item = item || {
        name: null,
        url: null,
        target: null,
        avatar: null,
        icon: null,
        type: null,
        items: null,
        properties: null,
      };
      this.$refs.drawer.open();
    },
    apply() {
      this.close();
    },
    close() {
      this.$refs.drawer.close();
    },
  },
};
</script>