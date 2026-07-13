<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

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
  <v-hover v-slot="{ hover }">
    <a
      :class="{'background-grey-primary': hover}"
      :href="item.url"
      class="entry-list-item-link position-relative d-flex full-width py-2 text-decoration-none"
      target="_self">
      <div
        style="position: relative; overflow: hidden; height: 80px; min-width: 80px;"
        class="flex-shrink-0 application-border-radius background-grey-primary d-flex align-center justify-center">
        <img
          v-if="item.illustrationUrl"
          :src="item.illustrationUrl"
          :alt="item.title"
          style="position: absolute; top: 0; left: 0;"
          width="80"
          height="80"
          class="application-border-radius object-fit-cover full-width full-height d-block">
        <v-icon
          v-else-if="item.icon"
          size="32"
          color="tertiary">
          {{ item.icon }}
        </v-icon>
      </div>
      <div
        style="margin-inline-start: 15px;"
        class="d-flex flex-column align-stretch flex-grow-1 no-min-width overflow-hidden">
        <div class="d-flex align-center text-subtitle mb-1">
          <date-format
            v-if="item.date"
            :value="new Date(item.date)"
            :format="dateFormat" />
          <v-spacer v-if="firstCategory" />
          <category-chip
            v-if="firstCategory"
            :category="firstCategory"
            tabindex="-1"
            small />
        </div>
        <span class="text-body text-truncate-2">{{ item.title }}</span>
        <span
          v-if="item.summary"
          class="text-subtitle text-truncate-2">
          {{ item.summary }}
        </span>
        <div class="d-flex align-center text-subtitle mt-1 mt-auto">
          <v-img
            v-if="item.spaceAvatarUrl"
            class="my-auto rounded flex-grow-0"
            :src="item.spaceAvatarUrl"
            width="20"
            height="20"
            alt="" />
          <v-icon
            v-if="item.spaceAvatarUrl && item.authorDisplayName"
            class="mx-1"
            small>
            mdi-chevron-right
          </v-icon>
          <v-avatar
            v-if="!item.spaceAvatarUrl && item.authorAvatarUrl"
            size="20"
            class="flex-shrink-0 me-1">
            <img :src="item.authorAvatarUrl" :alt="item.authorDisplayName">
          </v-avatar>
          <span
            v-if="item.authorDisplayName"
            class="text-truncate">
            {{ item.authorDisplayName }}
          </span>
        </div>
      </div>
      <v-icon
        v-if="item.icon"
        :title="item.objectType"
        size="16"
        class="entry-list-item-type-icon icon-default-color position-absolute t-0 r-0 mt-1 me-1">
        {{ item.icon }}
      </v-icon>
    </a>
  </v-hover>
</template>
<script>
export default {
  props: {
    item: {
      type: Object,
      required: true,
    },
  },
  data: () => ({
    dateFormat: {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    },
    firstCategory: null,
  }),
  watch: {
    item: {
      immediate: true,
      handler() {
        const categoryId = this.item?.categoryIds?.[0];
        if (categoryId) {
          this.$categoryService.getCategory(categoryId)
            .then(category => this.firstCategory = category)
            .catch(() => this.firstCategory = null);
        } else {
          this.firstCategory = null;
        }
      },
    },
  },
};
</script>
