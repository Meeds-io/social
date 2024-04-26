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
    v-if="canView">
    <v-hover v-slot="{ hover }">
      <v-card
        min-width="100%"
        max-width="100%"
        min-height="72"
        class="d-flex flex-column align-center border-box-sizing justify-center pa-5 overflow-hidden position-relative card-border-radius"
        color="white"
        flat>
        <links-header
          v-if="$root.initialized"
          :settings="$root.settings"
          :has-links="$root.hasLinks"
          :can-edit="canEdit"
          :hover="hover"
          class="full-width"
          @edit="editSettings()"
          @add="editSettings(true)" />
        <links-list
          v-if="hasLinks"
          :settings="$root.settings"
          :links="$root.links"
          class="full-width"
          @edit="editSettings()" />
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
    previewMode: false,
  }),
  computed: {
    hasLinks() {
      return this.$root.links?.length;
    },
    canView() {
      return this.canEdit || (this.$root.initialized && this.$root.hasLinks);
    },
    canEdit() {
      return !this.previewMode && this.$root.canEdit;
    },
  },
  watch: {
    canView() {
      this.$root.$updateApplicationVisibility(this.canView);
    }
  },
  created() {
    document.addEventListener('cms-preview-mode', this.switchToPreview);
    document.addEventListener('cms-edit-mode', this.switchToEdit);
    this.$root.$on('links-refresh', this.retrieveSettings);
  },
  beforeDestroy() {
    this.$root.$off('links-refresh', this.retrieveSettings);
    document.removeEventListener('cms-edit-mode', this.switchToEdit);
    document.removeEventListener('cms-preview-mode', this.switchToPreview);
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
    editSettings(openForm) {
      this.edit = false;
      this.$nextTick().then(() => {
        this.edit = true;
        this.$nextTick().then(() => {
          this.$root.$emit('links-settings-drawer', openForm);
        });
      });
    },
    switchToPreview() {
      this.previewMode = true;
    },
    switchToEdit() {
      this.previewMode = false;
    },
  },
};
</script>
