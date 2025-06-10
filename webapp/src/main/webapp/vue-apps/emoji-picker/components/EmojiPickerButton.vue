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
  <div
    v-if="useQuickEmojis"
    class="d-flex my-auto ">
    <emoji-picker-quick-emojis
      @select-emoji="selectEmoji" />
    <v-btn
      ref="launcher"
      width="28"
      height="28"
      min-width="28"
      class="pa-0"
      icon
      @click="showEmojiPicker">
      <v-icon
        size="16"
        class="icon-default-color">
        fas fa-plus
      </v-icon>
    </v-btn>
  </div>
  <v-btn
    v-else
    ref="launcher"
    class="pa-0"
    icon
    @click="showEmojiPicker">
    <v-icon
      :size="iconSize"
      class="icon-default-color">
      fas fa-smile
    </v-icon>
  </v-btn>
</template>

<script>

export default {
  props: {
    useQuickEmojis: {
      type: Boolean,
      default: false
    },
    iconSize: {
      type: Number,
      default: 16
    }
  },
  methods: {
    selectEmoji(emoji) {
      this.$emit('select-emoji', emoji);
    },
    showEmojiPicker() {
      const button = this.$refs.launcher?.$el;
      if (button) {
        const rect = button.getBoundingClientRect();
        document.dispatchEvent(new CustomEvent('show-emoji-picker', {
          detail: {
            top: `${rect.bottom + 8}px`,
            left: `${rect.left}px`,
            launcherInstance: this
          }
        }));
      }
    }
  }
};
</script>
