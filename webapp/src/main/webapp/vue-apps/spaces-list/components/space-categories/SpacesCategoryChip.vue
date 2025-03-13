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
    :key="category.id"
    ref="menu"
    v-model="menu"
    bottom
    close-delay="500"
    close-on-click
    :max-width="350"
    offset-y
    open-on-hover>
    <template #activator="{on, attrs}">
      <v-chip
        ref="chip"
        v-bind="attrs"
        class="text-truncate border-box-sizing"
        :class="[chipClass, !visible && 'invisible' || '']"
        color="primary"
        :outlined="!selected"
        v-on="$attrs?.select && {
          ...on,
          click: () => openCategory(category),
        } || on">
        <v-card
          class="text-truncate"
          :class="[
            selected && 'white--text' || 'primary--text',
          ]"
          color="transparent"
          flat
          :max-width="maxWidth"
          :title="category.name"
          width="auto">
          {{ category.name }}
        </v-card>
        <v-icon
          v-if="category.categories?.length"
          class="ms-2"
          :color="selected && 'white' || 'primary'"
          right
          size="16"
          @click.stop.prevent="menu = true">
          fa-chevron-down
        </v-icon>
      </v-chip>
    </template>
    <v-list
      class="pa-0"
      dense>
      <v-list-item
        v-for="subItem in category.categories"
        :key="subItem.id"
        class="text-truncate"
        :color="$root.selectedCategoryId === subItem.id && 'var(--allPagesTertiaryColor) !important'"
        dense
        @click.prevent.stop="openCategory(subItem)">
        <v-card
          class="text-truncate"
          color="transparent"
          flat
          :max-width="maxWidth"
          :title="subItem.name">
          {{ subItem.name }}
        </v-card>
      </v-list-item>
    </v-list>
  </v-menu>
  <v-chip
    v-else
    ref="chip"
    class="text-truncate border-box-sizing"
    :class="[chipClass, !visible && 'invisible' || '']"
    color="primary"
    :outlined="!selected"
    @click.prevent.stop="openCategory(category)">
    <v-card
      class="text-truncate"
      :class="[
        selected && 'white--text' || 'primary--text',
      ]"
      color="transparent"
      flat
      :max-width="maxWidth"
      :title="category.name"
      width="auto">
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
    },
    data: () => ({
      menu: false,
      width: null,
    }),
    computed: {
      visible () {
        return !this.parentWidth || !this.width || this.width < this.parentWidth;
      },
    },
    watch: {
      visible: {
        immediate: true,
        handler () {
          if (this.parentWidth && this.width) {
            this.$emit('initialized', this.visible);
          }
        },
      },
    },
    created () {
      document.addEventListener('click', this.closeMenuImmediatly);
    },
    mounted () {
      window.setTimeout(() => {
        if (this.$refs?.chip?.$el) {
          this.width = this.$refs.chip.$el.offsetLeft + this.$refs.chip.$el.offsetWidth;
        }
      }, 10);
    },
    beforeUnmount () {
      document.removeEventListener('click', this.closeMenuImmediatly);
    },
    methods: {
      closeMenuImmediatly () {
        this.menu = false;
      },
      openCategory (category) {
        this.$emit('select', category);
        if (this.menu) {
          this.closeMenu();
        }
      },
      closeMenu () {
        window.setTimeout(() => this.menu = false, 50);
      },
    },
  };
</script>