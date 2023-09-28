<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

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
  <v-app
    v-if="$root.initialized">
    <v-hover v-slot="{ hover }">
      <v-card
        min-height="72"
        class="d-flex align-center border-box-sizing justify-center position-relative"
        color="white"
        flat>
        <links-list
          v-if="hasLinks"
          :settings="$root.settings"
          :links="$root.links"
          :hover="hover"
          min-width="100%"
          class="mx-auto"
          @edit="editSettings" />
        <div v-else-if="canEdit" class="d-flex align-center justify-center">
          <v-btn
            class="primary"
            elevation="0"
            outlined
            border
            @click="editSettings">
            {{ $t('links.label.addLinksButton') }}
          </v-btn>
        </div>
      </v-card>
    </v-hover>
    <links-settings-drawer
      v-if="canEdit && edit" />
  </v-app>
</template>
<script>
export default {
  data: () => ({
    loading: true,
    edit: false,
  }),
  computed: {
    hasLinks() {
      return this.$root.links?.length;
    },
    canEdit() {
      return this.$root.canEdit;
    },
  },
  created() {
    this.$root.$on('links-refresh', this.retrieveSettings);
  },
  beforeDestroy() {
    this.$root.$off('links-refresh', this.retrieveSettings);
  },
  methods: {
    retrieveSettings(settings) {
      if (settings) {
        this.$root.settings = settings;
        return;
      }
      this.loading = true;
      this.$linkService.getSettings(this.$root.name, this.$root.language)
        .then(settings => this.$root.settings = settings)
        .then(() => this.loading = false);
    },
    editSettings() {
      this.edit = false;
      this.$nextTick().then(() => {
        this.edit = true;
        this.$nextTick().then(() => {
          this.$root.$emit('links-settings-drawer');
        });
      });
    },
  },
};
</script>
