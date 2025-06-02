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
  <v-card
    max-width="380"
    class="specific-scrollbar border-box-sizing"
    flat>
    <emoji-picker-list-category
      :categories="emojiCategories"
      :selected-category="selectedCategory"
      :is-searching="hasSearchTerm"
      @select="selectCategory" />
    <v-text-field
      v-model="search"
      prepend-inner-icon="fas fa-search"
      clear-icon="fas fa-times text-font-size"
      :placeholder="$t('emojiPicker.search.label')"
      class="ma-1 pt-0"
      dense
      clearable
      hide-details
      outlined />
    <v-virtual-scroll
      :items="emojiRows"
      item-height="42"
      height="400">
      <template #default="{ item: row }">
        <div class="d-flex pa-1 flex-nowrap">
          <v-btn
            v-for="emoji in row"
            :key="emoji.unicode"
            :title="emoji.name"
            class="pa-1 btn btn-default no-border"
            icon
            @click="selectEmoji(emoji)">
            <v-icon
              size="18"
              class="mb-1">
              {{ getEmojiChar(emoji.unicode) }}
            </v-icon>
          </v-btn>
        </div>
      </template>
    </v-virtual-scroll>
  </v-card>
</template>

<script>

const MAX_RECENT_EMOJIS = 30;

export default {
  props: {
    emojis: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      search: '',
      debouncedSearch: '',
      selectedCategory: null,
      cachedFilteredEmojis: [],
      recentEmojis: [],
      debounceTimer: null
    };
  },
  computed: {
    emojiCategories() {
      const base = this.emojis?.categories || [];
      const recent = this.recentEmojis.length
        ? [{
          name: this.$t('emojiPicker.search.label'),
          iconClass: 'fas fa-clock',
          emojis: this.recentEmojis
        }] : [];
      return [...recent, ...base];
    },
    currentCategory() {
      return this.selectedCategory || this.emojiCategories?.[0];
    },
    emojiRows() {
      const emojis = this.cachedFilteredEmojis;
      const perRow = 10;
      const rows = [];
      for (let i = 0; i < emojis.length; i += perRow) {
        rows.push(emojis.slice(i, i + perRow));
      }
      return rows;
    },
    hasSearchTerm() {
      return this.search?.trim()?.length > 0;
    }
  },
  watch: {
    search() {
      this.debounceSearch();
    },
    selectedCategory() {
      this.updateFilteredEmojis();
    }
  },
  mounted() {
    this.loadRecentEmojis();
    this.prepareSearchIndex();
    this.updateFilteredEmojis();
  },
  methods: {
    selectCategory(index) {
      this.selectedCategory = this.emojiCategories?.[index];
    },
    selectEmoji(emoji) {
      this.addToRecentEmojis(emoji);
      this.$emit('select-emoji', this.getEmojiChar(emoji.unicode));
    },
    getEmojiChar(unicode) {
      return unicode.split('-')
        .map(hex => String.fromCodePoint(parseInt(hex, 16)))
        .join('');
    },
    prepareSearchIndex() {
      for (const category of this.emojiCategories || []) {
        this.createSearchIndex(category.emojis);
      }
    },
    updateFilteredEmojis() {
      const all = this.emojis.categories?.flatMap(cat => cat.emojis) || [];
      const term = this.debouncedSearch?.toLowerCase()?.trim();
      if (!term) {
        this.cachedFilteredEmojis = this.currentCategory?.emojis || [];
        return;
      }
      this.cachedFilteredEmojis = all.filter(emoji =>
        emoji._searchIndex?.includes(term)
      );
    },
    debounceSearch() {
      clearTimeout(this.debounceTimer);
      this.debounceTimer = setTimeout(() => {
        this.debouncedSearch = this.search;
        this.updateFilteredEmojis();
      }, 100);
    },
    addToRecentEmojis(emoji) {
      this.recentEmojis = [
        emoji,
        ...this.recentEmojis.filter(e => e.unicode !== emoji.unicode)
      ].slice(0, MAX_RECENT_EMOJIS);
      localStorage.setItem('recentEmojis', JSON.stringify(this.recentEmojis));
    },
    loadRecentEmojis() {
      const json = localStorage.getItem('recentEmojis');
      if (json) {
        this.recentEmojis = JSON.parse(json);
        this.createSearchIndex(this.recentEmojis);
      }
    },
    createSearchIndex(emojis) {
      emojis.forEach(emoji => {
        emoji._searchIndex = [emoji.name || '', ...(emoji.shortcodes || []), ...(emoji.tags || [])
        ].join('|').toLowerCase();
      });
    }
  }
};
</script>
