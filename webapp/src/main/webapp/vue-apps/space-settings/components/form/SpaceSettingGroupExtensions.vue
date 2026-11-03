<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2026 Meeds Association contact@meeds.io

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
  <extension-registry-components
    :params="params"
    :filter="filter"
    name="SpaceSettings"
    type="space-settings-components"
    parent-element="div"
    element="div">
    <template v-if="title" #header>
      <space-setting-group-title
        :id="titleId"
        :title="title"
        :danger="danger" />
    </template>
    <template v-else-if="dividerFirst" #header>
      <v-divider class="mx-4" />
    </template>
    <template #separator>
      <v-divider class="mx-4" />
    </template>
    <template v-if="dividerLast" #footer>
      <v-divider class="mx-4" />
    </template>
  </extension-registry-components>
</template>
<script>
/**
 * The rows that applications add to one group of the Space settings page,
 * with a divider between two rows. Nothing is drawn when no row of the group
 * is enabled: a group made of these rows only (`title` set) carries its title
 * with them, and rows following or preceding social's rows get the divider
 * that separates them (`divider-first`, `divider-last`).
 */
export default {
  props: {
    params: {
      type: Object,
      default: null,
    },
    filter: {
      type: Function,
      default: null,
    },
    title: {
      type: String,
      default: null,
    },
    titleId: {
      type: String,
      default: null,
    },
    danger: {
      type: Boolean,
      default: false,
    },
    dividerFirst: {
      type: Boolean,
      default: false,
    },
    dividerLast: {
      type: Boolean,
      default: false,
    },
  },
};
</script>
