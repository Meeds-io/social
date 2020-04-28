<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 Meeds Association
contact@meeds.io
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
  <div
    :id="id"
    :class="clickable"
    class="spacesOverviewCard d-flex flex-column"
    @click="count && $emit('click')">
    <v-flex class="ma-auto" flat>
      <div class="spacesOverviewCount text-center pb-1">
        {{ count }}
      </div>
      <div class="spacesOverviewTitle text-center text-truncate pt-1">
        {{ title }}
      </div>
    </v-flex>
  </div>
</template>

<script>
export default {
  props: {
    id: {
      type: String,
      default: () => null,
    },
    title: {
      type: String,
      default: () => null,
    },
    count: {
      type: Number,
      default: () => 0,
    },
  },
  computed: {
    clickable() {
      return this.count && this.count !== '-' && 'clickable';
    },
  },
};
</script>

