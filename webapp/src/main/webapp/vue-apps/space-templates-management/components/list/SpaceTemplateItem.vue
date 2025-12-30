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
  <tr>
    <td align="center">
      <v-checkbox
        :value="selected || $root.allSpaceTemplatesSelected"
        on-icon="fas fa-check-square fa-lg primary--text"
        off-icon="far fa-square fa-lg"
        class="my-auto pt-2"
        @change="changeCheckboxStatus" />
    </td>
    <!-- name -->
    <td
      :width="$root.isMobile && '100%' || 'auto'"
      colspan="2"
      class="pe-0">
      <div class="d-flex align-center">
        <v-card
          class="d-flex align-center justify-center me-4"
          min-width="35"
          flat>
          <v-icon size="28">{{ icon }}</v-icon>
        </v-card>
        <div v-sanitized-html="name" class="text-truncate"></div>
      </div>
    </td>
    <!-- description -->
    <td v-if="!$vuetify.breakpoint.lgAndDown">
      <div
        v-sanitized-html="description"
        class="text-truncate-3 text-break">
      </div>
    </td>
    <td v-if="!$vuetify.breakpoint.lgAndDown" class="text-center">
      <span>{{ spaceTemplateType }}</span>
    </td>
    <td
      v-if="!$vuetify.breakpoint.lgAndDown"
      class="text-center"
      width="120px">
      <space-templates-management-item-permission
        v-for="expression in spaceTemplate.permissions"
        :key="expression"
        :expression="expression"
        class="ma-1" />
    </td>
    <td
      v-if="!$vuetify.breakpoint.lgAndDown"
      class="text-truncate text-center"
      width="120px">
      <v-chip
        v-if="spacesCount"
        :title="spacesCount"
        elevation="0"
        @click="openSpacesList">
        {{ spacesCountLabel }}
      </v-chip>
      <v-btn
        v-else
        :title="$t('spaceTemplate.addSpaceTooltip')"
        elevation="0"
        class="btn"
        icon
        @click="openSpaceForm">
        <v-icon class="icon-default-color" size="20">fa-plus</v-icon>
      </v-btn>
    </td>
    <td
      v-if="!$root.isMobile"
      width="50px">
      <v-switch
        v-model="enabled"
        :loading="loading"
        :title="$t('spaceTemplate.changeStatusTooltip')"
        class="mt-0 mx-auto ps-4"
        @change="changeStatus" />
    </td>
    <td
      class="text-center"
      width="50px">
      <space-templates-management-item-menu :space-template="spaceTemplate" />
    </td>
  </tr>
</template>
<script>

export default {
  props: {
    spaceTemplate: {
      type: Object,
      default: null,
    },
    selected: {
      type: Boolean,
      default: false,
    },
    select: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    menu: false,
    hoverMenu: false,
  }),
  computed: {
    spaceTemplateId() {
      return this.spaceTemplate?.id;
    },
    enabled() {
      return this.spaceTemplate?.enabled;
    },
    name() {
      return this.$te(this.spaceTemplate?.name) ? this.$t(this.spaceTemplate?.name) : this.spaceTemplate?.name;
    },
    description() {
      return this.$te(this.spaceTemplate?.description) ? this.$t(this.spaceTemplate?.description) : this.spaceTemplate?.description;
    },
    icon() {
      return this.spaceTemplate?.icon;
    },
    spacesCount() {
      return this.spaceTemplate?.spacesCount;
    },
    spacesCountLabel() {
      return this.spacesCount > 9 ? '9+' : this.spacesCount;
    },
    spaceTemplateType() {
      return this.spaceTemplate?.type;
    }
  },
  watch: {
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
  methods: {
    openSpacesList() {
      this.$root.$emit('space-list-by-template-open', this.spaceTemplate.id, this.spaceTemplate.name);
    },
    openSpaceForm() {
      this.$root.$emit('addNewSpace', this.spaceTemplate.id);
    },
    changeStatus(enabled) {
      this.$root.$emit('close-alert-message');
      this.loading = true;
      this.$spaceTemplateService.getSpaceTemplate(this.spaceTemplate.id)
        .then(spaceTemplate => {
          spaceTemplate.enabled = enabled;
          return this.$spaceTemplateService.updateSpaceTemplate(spaceTemplate)
            .then(() => {
              this.$root.$emit(`space-templates-${enabled && 'enabled' || 'disabled'}`, spaceTemplate);
            });
        })
        .then(() => {
          this.$root.$emit('alert-message', enabled ? this.$t('spaceTemplate.status.enabled.success') : this.$t('spaceTemplate.status.disabled.success'), 'success');
        })
        .catch(() => this.$root.$emit('alert-message', this.$t('spaceTemplate.status.update.error'), 'error'))
        .finally(() => this.loading = false);
    },
    changeCheckboxStatus(status) {
      this.select(status);
    }
  },
};
</script>