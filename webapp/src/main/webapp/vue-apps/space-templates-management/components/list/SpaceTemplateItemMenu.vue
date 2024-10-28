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
  <component
    v-model="menu"
    :is="$root.isMobile && 'v-bottom-sheet' || 'v-menu'"
    :left="!$vuetify.rtl"
    :right="$vuetify.rtl"
    :content-class="menuId"
    offset-y>
    <template #activator="{ on, attrs }">
      <v-btn
        :aria-label="$t('spaceTemplates.menu.open')"
        :loading="menuItemLoading"
        icon
        small
        class="mx-auto"
        v-bind="attrs"
        v-on="on">
        <v-icon size="16" class="icon-default-color">fas fa-ellipsis-v</v-icon>
      </v-btn>
    </template>
    <v-hover v-if="menu" @input="hoverMenu = $event">
      <v-list
        class="pa-0"
        dense
        @mouseout="menu = false"
        @focusout="menu = false">
        <v-subheader v-if="$root.isMobile">
          <div class="d-flex full-width">
            <div class="d-flex flex-grow-1 flex-shrink-1 align-center subtitle-1 text-truncate">
              {{ $t('spaceTemplate.label.templateMenu', {0: name}) }}
            </div>
            <div class="flex-shrink-0">
              <v-btn
                :aria-label="$t('spaceTemplate.label.closeMenu')"
                icon
                @click="menu = false">
                <v-icon>fa-times</v-icon>
              </v-btn>
            </div>
          </div>
        </v-subheader>
        <v-list-item-group v-model="listItem">
          <extension-registry-components
            name="spaceTemplateActions"
            type="space-template-actions"
            :params="{spaceTemplate}" />
          <component
            v-for="extension in sortedExtensions"
            :key="extension.id"
            :is="extension.componentName"
            :space-template="spaceTemplate"
            @loading="menuItemLoading = $event" />
        </v-list-item-group>
      </v-list>
    </v-hover>
  </component>
</template>
<script>
export default {
  props: {
    spaceTemplate: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    menu: false,
    hoverMenu: false,
    listItem: null,
    menuId: `spaceTemplateMenu${parseInt(Math.random() * 10000)}`,
    menuItemLoading: false,
  }),
  computed: {
    spaceTemplateId() {
      return this.spaceTemplate?.id;
    },
    name() {
      return this.$te(this.spaceTemplate?.name) ? this.$t(this.spaceTemplate?.name) : this.spaceTemplate?.name;
    },
    sortedExtensions() {
      return this.$root.menuItemExtensions
        // Remove duplication
        .filter((t, i) => this.$root.menuItemExtensions.findIndex(v => v.name === t.name) === i)
        // Sort results
        .sort((a, b) => a.rank - b.rank);
    },
  },
  watch: {
    listItem() {
      if (this.menu) {
        this.menu = false;
        this.listItem = null;
      }
    },
    menu() {
      if (this.menu) {
        this.$root.$emit('space-management-menu-opened', this.spaceTemplateId);
      } else {
        this.$root.$emit('space-management-menu-closed', this.spaceTemplateId);
      }
    },
    hoverMenu() {
      if (!this.hoverMenu) {
        window.setTimeout(() => {
          if (!this.hoverMenu) {
            this.menu = false;
          }
        }, 200);
      }
    },
  },
  created() {
    this.$root.$on('space-management-menu-opened', this.checkMenuStatus);
    document.addEventListener('click', this.closeMenuOnClick);
  },
  beforeDestroy() {
    this.$root.$off('space-management-menu-opened', this.checkMenuStatus);
    document.removeEventListener('click', this.closeMenuOnClick);
  },
  methods: {
    closeMenuOnClick(e) {
      if (e.target && !e.target.closest(`.${this.menuId}`)) {
        this.menu = false;
      }
    },
    checkMenuStatus(templateId) {
      if (this.menu && templateId !== this.spaceTemplate.id) {
        this.menu = false;
      }
    },
  },
};
</script>