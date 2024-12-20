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
    id="SpaceSettingsCategoriesDrawer"
    ref="drawer"
    v-model="drawer"
    :loading="loading || saving"
    allow-expand
    right>
    <template #title>
      {{ $t('SpaceSettings.editCategories.drawer') }}
    </template>
    <template v-if="drawer" #content>
      <div class="d-flex flex-column ma-4">
        <div class="mb-2">
          {{ $t('SpaceSettings.editCategories.drawer.summary1') }}
        </div>
        <div class="mb-2">
          {{ $t('SpaceSettings.editCategories.drawer.summary2') }}
        </div>
        <div class="mb-4">
          {{ $t('SpaceSettings.editCategories.drawer.summary3') }}
        </div>
        <category-input v-model="selectedCategoryIds" />
      </div>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :disabled="saving"
          class="btn me-2"
          @click="close">
          {{ $t('SpaceSettings.button.cancel') }}
        </v-btn>
        <v-btn
          :disabled="!modified"
          :loading="saving"
          class="btn btn-primary"
          @click.prevent.stop="save">
          {{ $t('SpaceSettings.button.updateSpace') }}
        </v-btn>
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
    categoryIds: null,
    selectedCategoryIds: null,
  }),
  computed: {
    modified() {
      return JSON.stringify(this.selectedCategoryIds) !== JSON.stringify(this.categoryIds);
    },
  },
  methods: {
    open() {
      this.categoryIds = this.$root.space?.categoryIds || [];
      this.selectedCategoryIds = this.categoryIds.slice();
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    async save() {
      this.saving = true;
      try {
        if (this.categoryIds?.length) {
          const unlinkIds = this.categoryIds.filter(id => this.selectedCategoryIds.indexOf(id) < 0);
          if (unlinkIds?.length) {
            await Promise.all(unlinkIds.map(id => this.$categoryLinkService.unlink(id, {
              type: 'space',
              id: this.$root.space.id,
              spaceId: this.$root.space.id,
            })));
          }
        }
        if (this.selectedCategoryIds.length) {
          const linkIds = this.selectedCategoryIds.filter(id => this.categoryIds.indexOf(id) < 0);
          if (linkIds?.length) {
            await Promise.all(linkIds.map(id => this.$categoryLinkService.link(id, {
              type: 'space',
              id: this.$root.space.id,
              spaceId: this.$root.space.id,
            })));
          }
        }
        this.$root.space.categoryIds = this.selectedCategoryIds;
        this.$root.$emit('alert-message', this.$t('SpaceSettings.editCategories.success'), 'success');
        this.close();
      } catch (e) {
        this.$root.$emit('alert-message', this.$t('SpaceSettings.editCategories.error'), 'error');
      } finally {
        this.saving = false;
      }
    },
  },
};
</script>