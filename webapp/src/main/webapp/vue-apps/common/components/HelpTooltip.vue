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
  <v-tooltip
    :value="true"
    :position-x="positionX"
    :position-y="positionY"
    bottom>
    <div class="d-flex flex-no-wrap align-center">
      <v-icon
        size="18"
        class="me-2"
        dark>
        fa-question-circle
      </v-icon>
      {{ text }}
    </div>
  </v-tooltip>
</template>
<script>
export default {
  props: {
    label: {
      type: String,
      default: null,
    },
    attach: {
      type: Object,
      default: null,
    },
  },
  computed: {
    text() {
      return this.$t(this.label);
    },
    position() {
      return this.attach?.getBoundingClientRect();
    },
    positionX() {
      return (this.position?.left || 0) + 100;
    },
    positionY() {
      return (this.position?.top || 0) + 20;
    },
  },
};
</script>