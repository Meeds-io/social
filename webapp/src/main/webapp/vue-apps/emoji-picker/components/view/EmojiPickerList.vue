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
    class="border-box-sizing"
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
      ref="emojiScroll"
      :items="emojiRows"
      :item-height="itemHeight"
      height="350"
      class="overflow-x-hidden specific-scrollbar">
      <template #default="{ item }">
        <div
          v-if="item.type === 'category'"
          :data-category-name="item.name"
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
      itemHeight: 40,
      isProgrammaticScroll: false,
    };
  },
  props: {
    emojis: {
      type: Object,
      default: null
    }
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
        const flatList = [];
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
    this.$refs.emojiScroll?.$el?.removeEventListener('scroll', this.handleScroll);
  },
  mounted() {
    this.prepareSearchIndex();
    this.updateFilteredEmojis();
    this.$refs.emojiScroll?.$el?.addEventListener('scroll', this.handleScroll, {passive: true});
  },
  methods: {
    getCategoryIndex(categoryName) {
      return this.emojiCategories.findIndex(category => category.name === categoryName);
    },
    selectCategory(index) {
      this.selectedCategoryIndex = index;
      this.selectedCategory = this.emojiCategories?.[index];
      const categoryName = this.selectedCategory?.name;
      const targetIndex = this.emojiRows.findIndex(item => item.type === 'category' && item.name === categoryName);

      if (targetIndex === -1) {
        return;
      }
      const scrollEl = this.$refs?.emojiScroll?.$el;
      if (!scrollEl) {
        return;
      }
      this.isProgrammaticScroll = true;
      scrollEl.scrollTo({
        top: targetIndex * this.itemHeight,
        behavior: 'smooth'
      });
      let scrollTimeout;
      const onScroll = () => {
        clearTimeout(scrollTimeout);
        scrollTimeout = setTimeout(() => {
          this.isProgrammaticScroll = false;
          scrollEl.removeEventListener('scroll', onScroll);
        }, 150);
      };
      scrollEl.addEventListener('scroll', onScroll);
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
    getVisibleCategoryIndex(scrollTop) {
      let offset = 0;
      let lastCategoryName = null;
      for (let i = 0; i < this.emojiRows.length; i++) {
        const item = this.emojiRows[i];
        if (item.type === 'category') {
          lastCategoryName = item.name;
        }
        if (offset >= scrollTop) {
          break;
        }
        offset += this.itemHeight;
      }
      if (lastCategoryName) {
        const categoryIndex = this.getCategoryIndex(lastCategoryName);
        return categoryIndex !== -1 ? categoryIndex : 0;
      }
      return 0;
    },
    handleScroll(event) {
      if (this.isProgrammaticScroll) {
        return;
      }
      const scrollTop = event.target.scrollTop;
      const categoryIndex = this.getVisibleCategoryIndex(scrollTop);
      if (categoryIndex !== -1 && categoryIndex !== this.selectedCategoryIndex) {
        this.selectedCategoryIndex = categoryIndex;
      }
    }
  }
};
</script>
