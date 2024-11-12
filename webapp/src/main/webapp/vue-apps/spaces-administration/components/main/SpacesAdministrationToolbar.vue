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
  <application-toolbar
    id="spacesListToolbar"
    :right-text-filter="{
      minCharacters: 3,
      placeholder: $t('spacesList.label.filterSpaces'),
      tooltip: $t('spacesList.label.filterSpaces')
    }"
    :right-select-box="{
      hide: $root.isMobile,
      selected: selectedTemplateId,
      items: spaceTemplateItems,
    }"
    :filters-count="filtersCount"
    compact
    class="px-1"
    no-text-truncate
    cols-auto
    @filter-text-input-end-typing="$emit('keyword-changed', $event)"
    @filter-select-change="$emit('template-changed', $event)"
    @loading="$emit('loading', $event)">
    <template v-if="!$root.isMobile" #left>
      <div v-if="$root.selectedSpaces.length && !$root.isMobile">
        <component
          v-for="extension in $root.bulkExtensions"
          :key="extension.name"
          :is="extension.componentName"
          class="me-4" />
      </div>
      <v-btn
        v-else
        id="applicationToolbarLeftButton"
        :aria-label="$t('social.spaces.administration.manageSpaces.spaces.add')"
        :class="$root.isMobile && 'px-0'"
        class="btn btn-primary text-truncate"
        @click="$root.$emit('addNewSpace')">
        <v-icon
          size="18">
          fa-plus
        </v-icon>
        <span
          v-if="!$root.isMobile"
          class="text-truncate text-none ms-2">
          {{ $t('social.spaces.administration.manageSpaces.spaces.add') }}
        </span>
      </v-btn>
    </template>
  </application-toolbar>
</template>
<script>
export default {
  props: {
    selectedTemplateId: {
      type: String,
      default: () => '0',
    },
  },
  data: () => ({
    loading: 0,
  }),
  computed: {
    filtersCount() {
      return this.selectedTemplateId === '0' ? 0 : 1;
    },
    spaceTemplateItems() {
      const spaceTemplateItems = [{
        text: this.$t('social.spaces.administration.manageSpaces.spaceTemplates.all'),
        value: '0',
      }];
      if (this.$root.spaceTemplates?.length) {
        spaceTemplateItems.push(...this.$root.spaceTemplates.map(t => ({
          text: t.name,
          value: t.id,
        })));
      }
      return spaceTemplateItems;
    },
  },
  created() {
    this.$root.$on('spaces-list-refresh', this.refresh);
    this.$root.$on('space-list-pending-updated', this.refresh);
  },
  beforeDestroy() {
    this.$root.$off('spaces-list-refresh', this.refresh);
    this.$root.$off('space-list-pending-updated', this.refresh);
  },
};
</script>
