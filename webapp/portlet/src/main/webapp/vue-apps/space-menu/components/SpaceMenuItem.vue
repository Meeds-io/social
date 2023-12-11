<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2022 Meeds Association
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
  <v-btn
    :value="uri"
    :href="uri"
    class="subtitle-2 spaceButtomNavigationItem">
    <span>{{ label }}</span>
    <i aria-hidden="true" :class="icon"> </i>
  </v-btn>
</template>
<script>
export default {
  props: {
    navigation: {
      type: Object,
      default: null
    }
  },
  computed: {
    uri() {
      return this.navigation?.uri;
    },
    label() {
      return this.navigation?.label;
    },
    icon() {
      return this.navigation.icon || 'fas fa-folder';
    }
  }
};
</script>
