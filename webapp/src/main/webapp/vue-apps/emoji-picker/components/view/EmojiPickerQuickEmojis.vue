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
  <div>
    <v-btn
      v-for="emoji in emojis"
      :key="emoji.unicode"
      width="28"
      min-width="28"
      height="28"
      class="pa-0 me-1 btn btn-default no-border"
      icon
      @click="selectEmoji(emoji)">
      <icon
        size="16"
        v-sanitized-html="emoji.data" />
    </v-btn>
  </div>
</template>

<script>

export default {
  props: {
    // each consumer injects its own list; defaults to the historical chat quick list
    emojis: {
      type: Array,
      default: () => [
        {unicode: '1F44D', data: '&#x1F44D;'},
        {unicode: '2764-FE0F', data: '&#x2764;&#xFE0F;'},
        {unicode: '1F605', data: '&#x1F605;'},
        {unicode: '1F62E', data: '&#x1F62E;'},
        {unicode: '1F622', data: '&#x1F622;'}
      ]
    },
  },
  methods: {
    getEmojiChar(unicode) {
      return unicode.split('-')
        .map(hex => String.fromCodePoint(parseInt(hex, 16)))
        .join('');
    },
    selectEmoji(emoji) {
      this.$emit('select-emoji', this.getEmojiChar(emoji.unicode));
      document.dispatchEvent(new CustomEvent('quick-emoji-selected', {detail: {emoji}}));
    }
  }
};
</script>
