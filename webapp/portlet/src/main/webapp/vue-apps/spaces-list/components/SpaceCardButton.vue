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
  <v-card
    :width="iconOnly && 36 || 'auto'"
    flat>
    <v-tooltip :disabled="extension.title && ($root.isMobile || !iconOnly)" bottom>
      <template #activator="{on, attrs}">
        <v-btn
          v-on="on"
          v-bind="extension.href && {
            ...(attrs || {}),
            'href': extension.href,
          } || attrs"
          :aria-label="extension.title"
          :icon="iconOnly"
          :color="!iconOnly && 'primary'"
          :outlined="!iconOnly"
          :loading="extension.loading"
          class="absolute-vertical-align z-index-one"
          ripple
          @touchstart.stop="0"
          @touchend.stop="0"
          @mousedown.stop="0"
          @mouseup.stop="0"
          @click.stop.prevent="() => extension?.click?.(space)">
          <div class="d-flex align-center justify-center">
            <v-icon
              v-if="extension.icon"
              :size="extension.iconSize || 20">
              {{ extension.icon }}
            </v-icon>
            <span
              v-if="!iconOnly"
              :class="extension.icon && 'ms-2'"
              class="text-body-font-size">
              {{ extension.title }}
            </span>
          </div>
        </v-btn>
      </template>
      <span>{{ extension.title }}</span>
    </v-tooltip>
  </v-card>
</template>
<script>
export default {
  props: {
    extension: {
      type: Object,
      default: () => ({
        icon: null,
        iconSize: null,
        title: null,
        loading: false,
        click: null,
      }),
    },
    space: {
      type: Object,
      default: null,
    },
    icon: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    iconOnly() {
      return this.icon || this.extension.iconOnly;
    },
  }
};
</script>

