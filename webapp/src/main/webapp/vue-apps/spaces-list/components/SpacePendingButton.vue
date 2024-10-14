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
    :disabled="$root.isMobile"
    transition="scale-transition"
    bottom>
    <template #activator="{on, attrs}">
      <v-btn
        :id="`${filter}SpacesButton`"
        v-on="on"
        v-bind="attrs"
        :aria-label="$t(labelKey, {0: count})"
        :class="{
          'primary-border-color' : isSelected,
          'border-color-transparent' : !isSelected,
        }"
        class="ms-2"
        height="36"
        width="36"
        icon
        @click="apply">
        <v-icon
          :class="iconClass"
          size="20"
          dark>
          {{ icon }}
        </v-icon>
        <v-card
          :class="badgeColor"
          class="d-flex align-center justify-center aspect-ratio-1 border-radius-circle position-absolute t-0 r-0 mt-n2 me-n2 text-subtitle-font-size line-height-normal"
          height="24"
          width="24"
          dark
          flat>
          {{ count > 9 ? '+9' : count }}
        </v-card>
      </v-btn>
    </template>
    <span>{{ $t(labelKey, {0: count}) }}</span>
  </v-tooltip>
</template>
<script>
export default {
  props: {
    count: {
      type: Number,
      default: () => 0,
    },
    filter: {
      type: String,
      default: null,
    },
    icon: {
      type: String,
      default: null,
    },
    iconClass: {
      type: String,
      default: null,
    },
    labelKey: {
      type: String,
      default: null,
    },
    badgeColor: {
      type: String,
      default: null,
    },
  },
  computed: {
    isSelected() {
      return this.$root.filter === this.filter;
    }
  },
  methods: {
    apply() {
      if (this.filter === 'requests') {
        this.$root.$emit('space-list-pending-open', this.count);
      } else {
        this.$root.$emit('spaces-list-filter-update', this.isSelected ? 'all' : this.filter);
      }
    },
  },
};
</script>