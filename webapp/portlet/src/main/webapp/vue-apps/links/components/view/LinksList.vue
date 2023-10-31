<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

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
  <component
    v-if="links?.length"
    :is="isColumn && 'v-list' || 'card-carousel'"
    :class="isColumn && 'pa-0' || 'mt-n2 mb-n4'"
    v-bind="isColumn && {
      dense: !largeIcon
    }">
    <component
      v-for="link in links"
      :key="link.id"
      :is="componentName"
      :link="link"
      :type="type"
      :large-icon="largeIcon"
      :show-name="showName"
      :show-description="showDescription" />
  </component>
</template>
<script>
export default {
  props: {
    settings: {
      type: Object,
      default: null,
    },
    links: {
      type: Array,
      default: null,
    },
  },
  computed: {
    type() {
      return this.settings?.type || 'CARD';
    },
    showName() {
      return this.settings?.showName || false;
    },
    showDescription() {
      return this.settings?.showDescription || false;
    },
    largeIcon() {
      return this.settings?.largeIcon || false;
    },
    header() {
      return this.settings?.header?.[this.$root.language] || this.settings?.header?.[this.$root.defaultLanguage];
    },
    seeMoreUrl() {
      return this.$linkService.toLinkUrl(this.settings?.seeMore);
    },
    isColumn() {
      return this.type === 'COLUMN';
    },
    componentName() {
      return this.isColumn && 'links-column' || 'links-card';
    },
  },
};
</script>