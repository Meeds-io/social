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
  <v-menu
    v-model="showPicker"
    :close-on-content-click="false"
    :position-x="menuX"
    :position-y="menuY"
    content-class="z-index-modal bord pickerMenu border-radius-8 overflow-hidden"
    :max-height="maxHeight"
    :max-width="maxWidth"
    absolute
    offset-x
    offset-y>
    <emoji-picker-list
      :emojis="emojis"
      @select-emoji="selectEmoji" />
  </v-menu>
</template>

<script>

export default {
  data() {
    return {
      emojiPickerPosition: {
        top: '40px',
        left: '0px'
      },
      showPicker: false,
      search: '',
      launcherInstance: null,
      maxHeight: 450,
      maxWidth: 308
    };
  },
  created() {
    document.addEventListener('show-emoji-picker', this.showEmojiPicker);
    document.addEventListener('mousedown', this.handleClickOutside);
  },
  beforeDestroy() {
    document.removeEventListener('show-emoji-picker', this.showEmojiPicker);
    document.removeEventListener('mousedown', this.handleClickOutside);
  },
  computed: {
    emojis() {
      return this.$root.emojiBank;
    },
    menuX() {
      return this.emojiPickerPosition?.left;
    },
    menuY() {
      return this.emojiPickerPosition?.top;
    }
  },
  methods: {
    handleClickOutside(event) {
      const menuEl = document.querySelector('.pickerMenu');
      if (!menuEl?.contains(event.target)) {
        this.showPicker = false;
      }
    },
    showEmojiPicker(event) {
      const data = event.detail;
      this.launcherInstance = data.launcherInstance;
      const launcherTop = parseFloat(data.top);
      const launcherLeft = parseFloat(data.left);
      const menuHeight = this.maxHeight;

      const spaceBelow = window.innerHeight - launcherTop;
      const showAbove = spaceBelow < menuHeight;

      this.emojiPickerPosition = {
        top: showAbove ? launcherTop - menuHeight - 45 : launcherTop,
        left: launcherLeft - (this.maxWidth + 32) / 2
      };
      this.showPicker = true;
    },
    selectEmoji(emoji) {
      this.launcherInstance.$emit('select-emoji', emoji);
      this.$nextTick(() => {
        this.showPicker = false;
      });
    }
  }
};
</script>
