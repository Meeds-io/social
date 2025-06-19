<!--
 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2025 Meeds Association contact@meeds.io

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
  <v-list
    class="pa-0"
    dense>
    <v-list-item
      ref="emojiItems"
      v-for="(item, index) in suggestions"
      :key="item.unicode"
      :class="{ 'v-list-item--active': index === focusedIndex }"
      @click="$emit('select', item)">
      <v-list-item-title>
        {{ item.emoji }} {{ item.shortcodes[0] }}
      </v-list-item-title>
    </v-list-item>
  </v-list>
</template>

<script>

export default {
  props: {
    suggestions: {
      type: Array,
      default: () => []
    },
    focusedIndex: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      localFocusedIndex: this.focusedIndex
    };
  },
  watch: {
    focusedIndex() {
      this.localFocusedIndex = this.focusedIndex;
    }
  },
  methods: {
    moveToItem(offset) {
      const max = this.suggestions.length - 1;
      this.localFocusedIndex = Math.max(0, Math.min(max, this.localFocusedIndex + offset));
      this.$emit('update:focusedIndex', this.localFocusedIndex);
      this.$nextTick(() => {
        const item = this.$refs.emojiItems?.[this.localFocusedIndex];
        if (item?.$el?.scrollIntoView) {
          item.$el.scrollIntoView({block: 'nearest'});
        }
      });
    }
  }
};
</script>
