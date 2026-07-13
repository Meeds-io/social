<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

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
    v-if="breadcrumb && category.categories?.length"
    ref="menu"
    v-model="menu"
    :key="category.id"
    :max-width="350"
    close-delay="500"
    open-on-hover
    close-on-click
    offset-y
    bottom>
    <template #activator="{on, attrs}">
      <v-chip
        ref="chip"
        v-on="$listeners?.select && {
          ...on,
          click: () => openCategory(category),
        } || on"
        v-bind="attrs"
        :outlined="!selected"
        :class="[chipClass, small && 'text-subtitle-font-size' || '', !visible && 'invisible' || '']"
        :color="selected && 'primary'"
        :small="small"
        :tabindex="tabindex"
        class="text-truncate border-box-sizing"
        @focus="menu = true">
        <v-card
          :title="category.name"
          :class="[
            selected && 'white--text' || 'primary--text',
          ]"
          :max-width="maxWidth"
          color="transparent"
          width="auto"
          class="text-truncate"
          flat>
          {{ category.name }}
        </v-card>
        <v-icon
          v-if="category.categories?.length"
          :color="selected && 'white' || 'primary'"
          class="ms-2"
          size="16"
          right
          @click.stop.prevent="menu = true">
          fa-chevron-down
        </v-icon>
      </v-chip>
    </template>
    <v-list class="pa-0" dense>
      <v-list-item
        v-for="subItem in category.categories"
        :key="subItem.id"
        :ref="i === 0 ? 'firstItem' : null"
        :color="selectedId === subItem.id && 'var(--allPagesTertiaryColor) !important'"
        class="text-truncate"
        tabindex="0"
        dense
        @click.prevent.stop="openCategory(subItem)">
        <v-card
          :title="subItem.name"
          :max-width="maxWidth"
          color="transparent"
          class="text-truncate"
          flat>
          {{ subItem.name }}
        </v-card>
      </v-list-item>
    </v-list>
  </v-menu>
  <v-chip
    v-else
    ref="chip"
    :outlined="!selected"
    :class="[chipClass, small && 'text-subtitle-font-size' || '', !visible && 'invisible' || '']"
    :color="selected && 'primary'"
    :small="small"
    :tabindex="tabindex"
    class="text-truncate border-box-sizing"
    @click.prevent.stop="openCategory(category)"
    @keydown.enter.stop.prevent="openCategory(category)">
    <v-card
      :title="category.name"
      :class="[
        selected && 'white--text' || 'primary--text',
      ]"
      :max-width="maxWidth"
      class="text-truncate"
      color="transparent"
      width="auto"
      flat>
      {{ category.name }}
    </v-card>
  </v-chip>
</template>
<script>
export default {
  props: {
    category: {
      type: Object,
      default: null,
    },
    selectedId: {
      type: Boolean,
      default: false,
    },
    selected: {
      type: Boolean,
      default: false,
    },
    breadcrumb: {
      type: Boolean,
      default: false,
    },
    maxWidth: {
      type: Number,
      default: () => 150,
    },
    chipClass: {
      type: String,
      default: null,
    },
    parentWidth: {
      type: Number,
      default: null,
    },
    small: {
      type: Boolean,
      default: false,
    },
    tabindex: {
      type: [String, Number],
      default: 0,
    },
  },
  data: () => ({
    menu: false,
    width: null,
  }),
  computed: {
    isMobile() {
      return this.$vuetify.breakpoint.mobile;
    },
    visible() {
      return this.isMobile || !this.parentWidth || !this.width || this.width < this.parentWidth;
    },
  },
  watch: {
    visible: {
      immediate: true,
      handler() {
        if (this.parentWidth && this.width) {
          this.$emit('initialized', this.visible);
        }
      }
    },
  },
  created() {
    document.addEventListener('click', this.closeMenuImmediatly);
  },
  mounted() {
    window.setTimeout(() => {
      if (this.$refs?.chip?.$el) {
        this.width = this.$refs.chip.$el.offsetLeft + this.$refs.chip.$el.offsetWidth;
      }
    }, 10);
  },
  beforeDestroy() {
    document.removeEventListener('click', this.closeMenuImmediatly);
  },
  methods: {
    closeMenuImmediatly() {
      this.menu = false;
    },
    openCategory(category) {
      this.$emit('select', category);
      if (this.menu) {
        this.closeMenu();
      }
    },
    closeMenu() {
      window.setTimeout(() => this.menu = false, 50);
    },
  },
};
</script>