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
    <!-- name -->
    <td
      class="pe-0"
      colspan="2"
      :width="$root.isMobile && '100%' || 'auto'">
      <div class="d-flex align-center">
        <v-card
          class="d-flex align-center justify-center me-4"
          flat
          min-width="35">
          <v-icon size="28">
            {{ icon }}
          </v-icon>
        </v-card>
        <div
          v-sanitized-html="name"
          class="text-truncate"></div>
      </div>
    </td>
    <!-- description -->
    <td v-if="!$vuetify.display.lgAndDown.value">
      <div
        v-sanitized-html="description"
        class="text-truncate-3 text-break"></div>
    </td>
    <td
      v-if="!$vuetify.display.lgAndDown.value"
      class="text-center"
      width="120px">
      <space-templates-management-item-permission
        v-for="expression in spaceTemplate.permissions"
        :key="expression"
        class="ma-1"
        :expression="expression" />
    </td>
    <td
      v-if="!$vuetify.display.lgAndDown.value"
      class="text-truncate text-center"
      width="120px">
      <v-chip
        v-if="spacesCount"
        elevation="0"
        :title="spacesCount"
        @click="openSpacesList">
        {{ spacesCountLabel }}
      </v-chip>
      <v-btn
        v-else
        class="btn"
        elevation="0"
        icon
        :title="$t('spaceTemplate.addSpaceTooltip')"
        @click="openSpaceForm">
        <v-icon
          class="icon-default-color"
          size="20">
          fa-plus
        </v-icon>
      </v-btn>
    </td>
    <td
      v-if="!$root.isMobile"
      width="50px">
      <v-switch
        v-model="enabled"
        class="mt-0 mx-auto ps-4"
        :loading="loading"
        :title="$t('spaceTemplate.changeStatusTooltip')"
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
    },
    data: () => ({
      menu: false,
      hoverMenu: false,
    }),
    computed: {
      spaceTemplateId () {
        return this.spaceTemplate?.id;
      },
      enabled () {
        return this.spaceTemplate?.enabled;
      },
      name () {
        return this.$te(this.spaceTemplate?.name) ? this.$t(this.spaceTemplate?.name) : this.spaceTemplate?.name;
      },
      description () {
        return this.$te(this.spaceTemplate?.description) ? this.$t(this.spaceTemplate?.description) : this.spaceTemplate?.description;
      },
      icon () {
        return this.spaceTemplate?.icon;
      },
      spacesCount () {
        return this.spaceTemplate?.spacesCount;
      },
      spacesCountLabel () {
        return this.spacesCount > 9 ? '9+' : this.spacesCount;
      },
    },
    watch: {
      hoverMenu () {
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
      openSpacesList () {
        this.$root.$emit('space-list-by-template-open', this.spaceTemplate.id, this.spaceTemplate.name);
      },
      openSpaceForm () {
        this.$root.$emit('addNewSpace', this.spaceTemplate.id);
      },
      changeStatus (enabled) {
        this.$root.$emit('close-alert-message');
        this.loading = true;
        eXo.$spaceTemplateService.getSpaceTemplate(this.spaceTemplate.id)
          .then(spaceTemplate => {
            spaceTemplate.enabled = enabled;
            return eXo.$spaceTemplateService.updateSpaceTemplate(spaceTemplate)
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
    },
  };
</script>