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
      :width="$root.isMobile && '100%' || 'auto'"
      class="px-0">
      <div class="d-flex align-center">
        <v-card
          class="d-flex align-center justify-center me-2"
          width="36"
          flat>
          <v-icon size="20">{{ icon }}</v-icon>
        </v-card>
        <div v-sanitized-html="name" class="text-truncate"></div>
      </div>
    </td>
    <!-- description -->
    <td v-if="!$root.isMobile">
      <div
        v-sanitized-html="description"
        class="text-truncate-3">
      </div>
    </td>
    <td
      v-if="!$root.isMobile"
      class="text-truncate text-center"
      width="120px">
      <space-templates-management-item-permission
        v-for="expression in spaceTemplate.permissions"
        :key="expression"
        :expression="expression" />
    </td>
    <td
      v-if="!$root.isMobile"
      class="text-truncate text-center"
      width="120px">
      {{ spacesCount }}
    </td>
    <td
      v-if="!$root.isMobile"
      class="text-center"
      width="50px">
      <v-switch
        v-model="enabled"
        :loading="loading"
        :aria-label="enabled && $t('spaceTemplate.label.disableTemplate') || $t('spaceTemplate.label.enableTemplate')"
        class="mt-0 mx-auto"
        @click="changeStatus" />
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
      return this.$root.spacesCountByTemplates?.[this.spaceTemplate?.id] || 0;
    },
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
    changeStatus() {
      this.$root.$emit('close-alert-message');
      this.loading = true;
      this.$spaceTemplateService.getSpaceTemplate(this.spaceTemplate.id)
        .then(spaceTemplate => {
          spaceTemplate.enabled = !this.enabled;
          return this.$spaceTemplateService.updateSpaceTemplate(spaceTemplate)
            .then(() => {
              this.$root.$emit(`space-templates-${this.enabled && 'disabled' || 'enabled'}`, spaceTemplate);
            });
        })
        .then(() => {
          this.$root.$emit('alert-message', this.$t('spaceTemplate.status.update.success'), 'success');
        })
        .catch(() => this.$root.$emit('alert-message', this.$t('spaceTemplate.status.update.error'), 'error'))
        .finally(() => this.loading = false);
    },
  },
};
</script>