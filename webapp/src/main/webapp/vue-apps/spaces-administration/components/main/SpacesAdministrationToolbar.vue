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
    :compact="compactDisplay || $root.isMobile"
    class="px-1"
    no-text-truncate
    @filter-text-input-end-typing="$emit('keyword-changed', $event)"
    @filter-button-click="$root.$emit('spaces-list-filter-open', filter)"
    @loading="$emit('loading', $event)" />
</template>
<script>
export default {
  props: {
    filter: {
      type: String,
      default: null,
    },
    compactDisplay: {
      type: Boolean,
      default: false
    },
  },
  data: () => ({
    loading: 0,
  }),
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
