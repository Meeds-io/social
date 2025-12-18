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
    v-if="displaySpaceCreationMenu"
    v-model="menu"
    ref="addNewSpaceButtonMenu"
    content-class="application-menu z-index-modal"
    :left="left"
    offset-y>
    <template #activator="{attrs, on}">
      <v-btn
        id="addNewSpaceButtonMenu"
        :title="$t('menu.spaces.addNewSpaceTooltip')"
        :small="!icon && $root.isMobile"
        :color="color"
        :icon="icon"
        :elevation="elevation"
        v-bind="attrs"
        v-on="on">
        <v-icon :size="iconSize">fa-plus</v-icon>
        <span v-if="displayLabel" class="ms-2 hidden-xs-only">
          {{ $t('spacesList.button.add') }}
        </span>
      </v-btn>
    </template>
    <v-list
      dense
      max-width="auto"
      min-width="auto"
      width="auto">
      <v-list-item @click.prevent="addNewSpace">
        <v-list-item-content class="ms-0 text-body my-auto">
          <v-list-item-title>
            {{ $t('menu.spaces.createMainSpace') }}
          </v-list-item-title>
        </v-list-item-content>
      </v-list-item>

      <v-list-item @click.prevent="addNewSubSpace">
        <v-list-item-content class="ms-0 text-body my-auto">
          <v-list-item-title>
            {{ $t('menu.spaces.createSubSpace') }}
          </v-list-item-title>
        </v-list-item-content>
      </v-list-item>
    </v-list>
  </v-menu>
  <v-btn
    v-else
    id="addNewSpaceButton"
    :title="$t('menu.spaces.addNewSpaceTooltip')"
    :small="!icon && $root.isMobile"
    :color="color"
    :icon="icon"
    :elevation="elevation"
    v-bind="attrs"
    v-on="on"
    @click="addNewSpace">
    <v-icon :size="iconSize">fa-plus</v-icon>
    <span v-if="displayLabel" class="ms-2 hidden-xs-only">
      {{ $t('spacesList.button.add') }}
    </span>
  </v-btn>
</template>

<script>
export default {
  props: {
    color: {
      type: String,
      default: '',
    },
    icon: {
      type: Boolean,
      default: false,
    },
    displayLabel: {
      type: Boolean,
      default: false,
    },
    iconSize: {
      type: Number,
      default: 18,
    },
    elevation: {
      type: Number,
      default: 0,
    },
    requireFormDrawer: {
      type: Boolean,
      default: false
    },
    left: {
      type: Boolean,
      default: false
    },
    setMenuVisibility: {
      type: Boolean,
      default: false
    }
  },
  data: () => ({
    id: Math.random(), // NOSONAR
    menu: false,
    isMemberInParentSpace: false,
    spaceTemplates: [],
    subspaceTemplateIds: [],
    displaySpaceCreationMenu: false
  }),
  computed: {
    filteredSpaceTemplates() {
      return this.spaceTemplates.filter(template => !this.subspaceTemplateIds.includes(template.id));
    },
  },
  watch: {
    menu() {
      if (this.setMenuVisibility) {
        this.processMenuVisibility(this.menu);
      }
      // Workaround to fix closing menu when clicking outside
      if (this.menu) {
        document.addEventListener('mousedown', this.closeMenu);
      } else {
        document.removeEventListener('mousedown', this.closeMenu);
      }
    },
  },
  created() {
    this.init();
  },
  methods: {
    async init() {
      const result = await this.$spaceService.getSpacesByFilter({
        offset: 0,
        limit: 1,
        filter: 'accessible',
        onlyParentSpaces: true,
      });
      this.isMemberInParentSpace = result?.size > 0;
      if (!this.$root.spaceTemplates) {
        this.$root.spaceTemplates = await this.$spaceTemplateService.getSpaceTemplates();
      }
      this.spaceTemplates = this.$root.spaceTemplates;
      if (!this.$root.subspaceTemplateIds) {
        this.$root.subspaceTemplateIds = await this.$spaceTemplateService.getSubspaceTemplateIds();
      }
      this.subspaceTemplateIds = this.$root.subspaceTemplateIds;
      this.displaySpaceCreationMenu = this.isMemberInParentSpace && this.subspaceTemplateIds.length && !this.$root.openedSpaceTemplateId;
    },
    addNewSpace() {
      const spaceTemplate = this.$root.openedSpaceTemplateId;
      if (this.requireFormDrawer) {
        window.require(['SHARED/spaceForm'], drawer => drawer.open(spaceTemplate, !spaceTemplate && this.filteredSpaceTemplates));
      } else {
        this.$root.$emit('addNewSpace', spaceTemplate, !spaceTemplate && this.filteredSpaceTemplates);
      }
    },
    addNewSubSpace() {
      if (this.requireFormDrawer) {
        window.require(['SHARED/spaceForm'], drawer => drawer.open(this.$root.openedSpaceTemplateId, null, null, true));
      } else {
        this.$root.$emit('addNewSpace', this.$root.openedSpaceTemplateId, null, null, true);
      }
    },
    closeMenu(event) {
      if (event !== this.id) {
        if (event?.target) {
          window.setTimeout(() => {
            this.menu = false;
          }, 200);
        } else {
          this.menu = false;
        }
      }
    },
    processMenuVisibility(visible) {
      if (visible) {
        this.$root.$emit('menu-opened');
        this.$root.hoverMenu = visible;
      } else {
        this.$root.$emit('menu-closed');
        this.$root.hoverMenu = visible;
      }
    }
  }
};
</script>
