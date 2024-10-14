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
  <v-list-item
    v-on="this.$listeners.click && {
      click: this.$listeners.click
    }"
    v-bind="this.$attrs.href && {
      href: this.$attrs.href
    }"
    :aria-label="$t(label)"
    class="ps-0 py-0"
    link>
    <v-list-item-icon class="mx-2">
      <v-icon
        class="ma-auto"
        size="18"
        v-bind="iconColor && {
          color: iconColor,
        }">
        {{ icon }}
      </v-icon>
    </v-list-item-icon>
    <v-list-item-content class="d-inline">
      <v-list-item-title :class="labelColor && `${labelColor}--text`" class="text-body">{{ $t(label) }}</v-list-item-title>
    </v-list-item-content>
  </v-list-item>
</template>
<script>
export default {
  props: {
    label: {
      type: String,
      default: null,
    },
    labelColor: {
      type: String,
      default: null,
    },
    icon: {
      type: String,
      default: null,
    },
    iconColor: {
      type: String,
      default: null,
    },
  },
};
</script>

