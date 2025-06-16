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
    v-model="visible"
    :position-x="position.x"
    :position-y="position.y"
    :close-on-content-click="false"
    :min-width="minWidth"
    :max-height="400"
    :content-class="`border-radius-8 specific-scrollbar ${menuContentClass}`"
    absolute
    top
    offset-x
    offset-y>
    <emoji-suggester-list
      v-if="filteredEmojis.length"
      ref="emojiSuggesterList"
      :focused-index.sync="focusedIndex"
      :suggestions="filteredEmojis"
      @select="selectEmoji" />
  </v-menu>
</template>
<script>

export default {
  data() {
    return {
      visible: false,
      query: '',
      currentRange: null,
      position: {x: 0, y: 0},
      composerElement: null,
      shortCodeRegex: /:[a-zA-Z0-9:_-]{2,32}$/,
      focusedIndex: -1,
    };
  },
  props: {
    composerId: {
      type: String,
      default: null
    },
    minWidth: {
      type: Number,
      default: null
    }
  },
  computed: {
    menuContentClass() {
      return !this.filteredEmojis?.length && 'elevation-0';
    },
    emojis() {
      return this.$emojiBank;
    },
    filteredEmojis() {
      if (!this.query.startsWith(':') || this.query.length < 3) {
        return [];
      }
      const lcQuery = this.query.toLowerCase();
      const allEmojis = this.emojis.categories.flatMap(cat => cat.emojis);

      return allEmojis
        .filter(e => e.shortcodes?.some(sc => sc.toLowerCase().startsWith(lcQuery)));
    }
  },
  mounted() {
    const composer = document.getElementById(this.composerId);
    if (!composer) {
      return;
    }
    this.composerElement = composer;
    composer.addEventListener('input', this.onInput);
    composer.addEventListener('keyup', this.onInput);
    composer.addEventListener('keydown', this.onKeyDown, {capture: true});
    composer.addEventListener('click', this.onInput);
  },
  beforeDestroy() {
    const composer = document.getElementById(this.composerId);
    if (!composer) {
      return;
    }
    composer.removeEventListener('input', this.onInput);
    composer.removeEventListener('keyup', this.onInput);
    composer.removeEventListener('keydown', this.onKeyDown, {capture: true});
    composer.removeEventListener('click', this.onInput);
  },
  methods: {
    onInput() {
      const selection = window.getSelection();
      if (!selection || !selection.rangeCount) {
        return;
      }

      const range = selection.getRangeAt(0);
      this.currentRange = range.cloneRange();

      const word = this.getCurrentShortcode(range);
      this.query = word;

      if (word.startsWith(':') && word.length >= 3) {
        const rect = range.getBoundingClientRect();
        this.position = {
          x: rect.left + window.scrollX,
          y: (rect.bottom + window.scrollY) - 20
        };
        this.visible = true;
      } else {
        this.hide();
      }
    },
    onKeyDown(e) {
      if (!this.visible || !this.filteredEmojis.length) {
        return;
      }

      const handledKeys = ['ArrowDown', 'ArrowUp', 'Enter', 'Escape'];
      if (!handledKeys.includes(e.key)) {
        return;
      }
      e.preventDefault();
      e.stopImmediatePropagation();

      if (e.key === 'ArrowDown') {
        this.focusedIndex = (this.focusedIndex + 1) % this.filteredEmojis.length;
        this.$refs.emojiSuggesterList?.moveToItem(1);
      } else if (e.key === 'ArrowUp') {
        this.focusedIndex = (this.focusedIndex - 1 + this.filteredEmojis.length) % this.filteredEmojis.length;
        this.$refs.emojiSuggesterList?.moveToItem(-1);
      } else if (e.key === 'Enter') {
        const emoji = this.filteredEmojis[this.focusedIndex];
        if (emoji) {
          this.selectEmoji(emoji);
        }
      } else if (e.key === 'Escape') {
        this.hide();
      }
    },
    getShortcodeRange(range) {
      if (!range || !range.startContainer || range.startContainer.nodeType !== Node.TEXT_NODE) {
        return null;
      }

      const text = range.startContainer.textContent.slice(0, range.startOffset);
      const match = text.match(this.shortCodeRegex);

      if (!match) {
        return null;
      }

      const start = range.startOffset - match[0].length;
      const newRange = document.createRange();
      newRange.setStart(range.startContainer, start);
      newRange.setEnd(range.startContainer, range.startOffset);
      return newRange;
    },
    getCurrentShortcode(range) {
      const container = range.startContainer;
      if (!container || container.nodeType !== Node.TEXT_NODE) {
        return '';
      }

      const text = container.textContent.slice(0, range.startOffset);
      const match = text.match(this.shortCodeRegex);
      return match ? match[0] : '';
    },
    selectEmoji(item) {
      this.hide();
      const range = this.getShortcodeRange(this.currentRange);
      if (!range) {
        return;
      }
      this.$emit('select-emoji', item.emoji, range);
    },
    hide() {
      this.visible = false;
      this.focusedIndex = -1;
    }
  },
};
</script>
