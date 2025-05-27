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
      {{ $t('categoryInput.drawer') }}
    </template>
    <template v-if="drawer" #content>
      <div class="d-flex flex-column ma-4">
        <template v-if="objectType === 'space'">
          <div class="mb-2">
            {{ $t('categoryInput.drawer.summary1') }}
          </div>
          <div class="mb-2">
            {{ $t('categoryInput.drawer.summary2') }}
          </div>
          <div class="mb-4">
            {{ $t('categoryInput.drawer.summary3') }}
          </div>
        </template>
        <slot></slot>
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
          {{ $t('categoryInput.cancel') }}
        </v-btn>
        <v-btn
          :disabled="!modified"
          :loading="saving"
          class="btn btn-primary"
          @click.prevent.stop="save">
          {{ $t('categoryInput.update') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    formModified: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    drawer: false,
    loading: false,
    saving: false,
    objectType: null,
    objectId: null,
    spaceId: null,
    categoryIds: null,
    selectedCategoryIds: null,
  }),
  computed: {
    modified() {
      return this.formModified || JSON.stringify(this.selectedCategoryIds) !== JSON.stringify(this.categoryIds);
    },
  },
  created() {
    document.addEventListener('category-form-drawer-open', this.openByEvent);
  },
  beforeDestroy() {
    document.removeEventListener('category-form-drawer-open', this.openByEvent);
  },
  methods: {
    openByEvent(event) {
      this.open(event?.detail);
    },
    open({objectType, objectId, spaceId, categoryIds}) {
      this.objectType = objectType;
      this.objectId = objectId;
      this.spaceId = spaceId;
      this.categoryIds = categoryIds || [];
      this.selectedCategoryIds = this.categoryIds.slice();
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    async save() {
      this.saving = true;
      try {
        await this.$categoryLinkService.updateCategories({
          objectType: this.objectType,
          objectId: this.objectId,
          spaceId: this.spaceId,
          oldCategories: this.categoryIds,
          newCategories: this.selectedCategoryIds,
          dropExisting: true
        });
        this.categoryIds = this.selectedCategoryIds;
        this.$root.$emit('categories-updated', this.objectType, this.objectId, this.categoryIds);
        this.$root.$emit('alert-message', this.$t('categoryInput.updated.success'), 'success');
        this.close();
      } catch (e) {
        console.error(e);
        this.$root.$emit('alert-message', this.$t('categoryInput.updated.error'), 'error');
      } finally {
        this.saving = false;
      }
    },
  },
};
</script>