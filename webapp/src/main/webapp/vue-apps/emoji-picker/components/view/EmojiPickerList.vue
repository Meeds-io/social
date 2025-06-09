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
    class="specific-scrollbar border-box-sizing"
    flat>
    <emoji-picker-list-category
      :categories="emojiCategories"
      :selected-category-index="selectedCategoryIndex"
      :is-searching="hasSearchTerm"
      :has-recents="hasRecents"
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
      ref="emojiScroll"
      :item-height="itemHeight"
      height="350">
      <template #default="{ item }">
        <div
          v-if="item.type === 'category'"
          v-intersect="(isVisible) => onCategoryVisibleByName(item.name, isVisible)"
          class="font-weight-bold ms-2 mb-n1 mt-3">
          {{ $t(`emojiPicker.category.${item.name}.label`) }}
        </div>
        <div
          v-else-if="item.type === 'row'"
          class="d-flex pa-1 flex-nowrap">
          <v-btn
            v-for="emoji in item.emojis"
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

const MAX_RECENT_EMOJIS = 24;

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
      selectedCategoryIndex: 0,
      cachedFilteredEmojis: [],
      recentEmojis: this.loadRecentEmojisFromStorage(),
      debounceTimer: null,
      categoryRefs: {},
      observer: null,
      perRows: 8,
      itemHeight: 40
    };
  },
  computed: {
    emojiCategories() {
      const base = this.emojis?.categories || [];
      const recent = this.recentEmojis.length
        ? [{
          name: 'recent',
          iconClass: 'fas fa-clock',
          emojis: this.recentEmojis
        }] : [];
      return [...recent, ...base];
    },
    hasRecents() {
      return this.recentEmojis?.length > 0;
    },
    currentCategory() {
      return this.selectedCategory || this.emojiCategories?.[0];
    },
    emojiRows() {
      const flatList = [];
      if (this.hasSearchTerm) {
        const rows = [];
        for (let i = 0; i < this.cachedFilteredEmojis.length; i += this.perRows) {
          rows.push({
            type: 'row',
            emojis: this.cachedFilteredEmojis.slice(i, i + this.perRows)
          });
        }
        return rows;
      } else {
        for (const category of this.emojiCategories) {
          flatList.push({type: 'category', name: category.name});
          for (let i = 0; i < category.emojis.length; i += this.perRows) {
            flatList.push({
              type: 'row',
              emojis: category.emojis.slice(i, i + this.perRows)
            });
          }
        }
        return flatList;
      }
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
  created() {
    document.addEventListener('quick-emoji-selected', this.handleQuickEmojisSelect);
  },
  beforeDestroy() {
    document.removeEventListener('quick-emoji-selected', this.handleQuickEmojisSelect);
  },
  mounted() {
    this.prepareSearchIndex();
    this.updateFilteredEmojis();
  },
  methods: {
    onCategoryVisibleByName(categoryName, isVisible) {
      if (!isVisible) {
        return;
      }
      const catIndex = this.emojiCategories.findIndex(c => c.name === categoryName);
      if (catIndex !== -1 && this.selectedCategoryIndex !== catIndex) {
        this.selectedCategoryIndex = catIndex;
      }
    },
    selectCategory(index) {
      this.selectedCategoryIndex = index;
      this.selectedCategory = this.emojiCategories?.[index];
      const categoryName = this.selectedCategory?.name;
      const targetIndex = this.emojiRows.findIndex(item => item.type === 'category' && item.name === categoryName);
      if (targetIndex !== -1) {
        this.$nextTick(() => {
          this.$refs?.emojiScroll?.$el.scrollTo({
            top: targetIndex * this.itemHeight,
            behavior: 'smooth'
          });
        });
      }
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
      if (!emoji) {
        return;
      }
      this.recentEmojis = [
        emoji,
        ...this.recentEmojis.filter(e => e.unicode !== emoji.unicode)
      ].slice(0, MAX_RECENT_EMOJIS);
      localStorage.setItem('recentEmojis', JSON.stringify(this.recentEmojis));
    },
    loadRecentEmojisFromStorage() {
      const json = localStorage.getItem('recentEmojis');
      if (json) {
        const recent = JSON.parse(json);
        this.createSearchIndex(recent);
        return recent;
      }
      return [];
    },
    createSearchIndex(emojis) {
      emojis.forEach(emoji => {
        emoji._searchIndex = [emoji.name || '', ...(emoji.shortcodes || []), ...(emoji.tags || [])
        ].join('|').toLowerCase();
      });
    },
    handleQuickEmojisSelect(event) {
      const targetEmoji = event.detail?.emoji;
      const emoji = this.emojis.quickEmojis.find(emoji => emoji.unicode === targetEmoji.unicode);
      this.addToRecentEmojis(emoji);
    },
  }
};
</script>
