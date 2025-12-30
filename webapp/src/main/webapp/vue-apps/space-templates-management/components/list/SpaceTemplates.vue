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
  <div>
    <v-data-table
      v-model="$root.selectedSpaceTemplates"
      :headers="headers"
      :items="filteredSpaceTemplates"
      :loading="loading"
      :disable-sort="$root.isMobile"
      :hide-default-header="$root.isMobile"
      :show-select="!$root.isMobile"
      must-sort
      disable-pagination
      hide-default-footer
      class="spaceTemplatesTable px-5">
      <template slot="header.data-table-select" slot-scope="{on, props}">
        <v-checkbox
          v-on="on"
          v-bind="props"
          on-icon="fas fa-check-square fa-lg primary--text"
          indeterminate-icon="fas fa-minus-square fa-lg"
          off-icon="far fa-square fa-lg"
          class="my-auto pt-2"
          @change="on.input" />
      </template>
      <template v-if="$root.selectedSpaceTemplates.length" slot="body.prepend">
        <tr>
          <td :colspan="headers.length + 1" class="px-0">
            <v-alert
              :icon="false"
              class="ma-0 ps-5 no-border-radius"
              border="left"
              type="info"
              colored-border>
              <div v-html="selectionLabel"></div>
            </v-alert>
          </td>
        </tr>
      </template>
      <template slot="item" slot-scope="props">
        <space-templates-management-item
          :key="props.item.id"
          :space-template="props.item"
          :selected="props.isSelected"
          :select="props.select" />
      </template>
    </v-data-table>
    <exo-confirm-dialog
      ref="deleteConfirmDialog"
      :title="$t('spaceTemplate.label.confirmDeleteTitle')"
      :message="$t('spaceTemplate.label.confirmDeleteMessage', {0: `<br><strong>${nameToDelete}</strong>`})"
      :ok-label="$t('spaceTemplate.label.confirm')"
      :cancel-label="$t('spaceTemplate.label.cancel')"
      @ok="deleteSpaceTemplate(spaceTemplateToDelete)"
      @closed="spaceTemplateToDelete = null" />
  </div>
</template>
<script>
export default {
  props: {
    keyword: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    spaceTemplateToDelete: null,
  }),
  computed: {
    headers() {
      return this.$root.isMobile && [
        {
          text: '',
          value: 'icon',
          align: 'left',
          sortable: false,
          class: 'space-template-icon-header',
          width: '35px'
        },
        {
          text: this.$t('spaceTemplates.label.name'),
          value: 'name',
          align: 'left',
          sortable: true,
          class: 'space-template-name-header',
          width: 'auto'
        },
        {
          text: this.$t('spaceTemplates.label.actions'),
          value: 'actions',
          align: 'center',
          sortable: false,
          class: 'space-template-actions-header',
          width: '50px'
        },
      ] || (this.$vuetify.breakpoint.lgAndDown && [
        {
          text: '',
          value: 'icon',
          align: 'left',
          sortable: false,
          class: 'space-template-icon-header',
          width: '35px'
        },
        {
          text: this.$t('spaceTemplates.label.name'),
          value: 'name',
          align: 'left',
          sortable: true,
          class: 'space-template-name-header ps-0',
          width: 'auto'
        },
        {
          text: this.$t('spaceTemplates.label.status'),
          value: 'enabled',
          align: 'center',
          sortable: true,
          class: 'space-template-category-header text-no-wrap',
          width: '90px'
        },
        {
          text: this.$t('spaceTemplates.label.actions'),
          value: 'actions',
          align: 'center',
          sortable: false,
          class: 'space-template-actions-header text-no-wrap',
          width: '90px'
        },
      ]) || [
        {
          text: '',
          value: 'icon',
          align: 'left',
          sortable: false,
          class: 'space-template-icon-header',
          width: '35px'
        },
        {
          text: this.$t('spaceTemplates.label.name'),
          value: 'name',
          align: 'left',
          sortable: true,
          class: 'space-template-name-header ps-0',
          width: 'auto'
        },
        {
          text: this.$t('spaceTemplates.label.description'),
          value: 'description',
          align: 'left',
          sortable: false,
          class: 'space-template-description-header',
          width: 'auto'
        },
        {
          text: this.$t('spaceTemplates.type.label'),
          value: 'type',
          align: 'center',
          sortable: true,
          class: 'space-template-type-header',
          width: 'auto'
        },
        {
          text: this.$t('spaceTemplates.label.permissions'),
          value: 'permissions',
          align: 'center',
          sortable: true,
          class: 'space-template-permissions-header',
          width: '120px'
        },
        {
          text: this.$t('spaceTemplates.label.spacesCount'),
          value: 'spacesCount',
          align: 'center',
          sortable: true,
          class: 'space-template-spaces-count-header',
          width: '120px'
        },
        {
          text: this.$t('spaceTemplates.label.status'),
          value: 'enabled',
          align: 'center',
          sortable: true,
          class: 'space-template-category-header text-no-wrap',
          width: '90px'
        },
        {
          text: this.$t('spaceTemplates.label.actions'),
          value: 'actions',
          align: 'center',
          sortable: false,
          class: 'space-template-actions-header text-no-wrap',
          width: '90px'
        },
      ];
    },
    spaceTemplates() {
      return this.$root.spaceTemplates;
    },
    filteredSpaceTemplates() {
      const spaceTemplates = this.spaceTemplates
        ?.filter?.(t => t.name)
        ?.map?.(t => {
          t = JSON.parse(JSON.stringify(t));
          t.spacesCount = this.$root.spacesCountByTemplates?.[t.id] || 0;
          t.type = this.resolveSpaceTemplateType(t);
          return t;
        }) || [];
      spaceTemplates.sort((a, b) => this.$root.collator.compare(a.name.toLowerCase(), b.name.toLowerCase()));
      return this.keyword?.length && spaceTemplates.filter(t => {
        const name = this.$te(t.name) ? this.$t(t.name) : t.name;
        const description = this.$te(t.description) ? this.$t(t.description) : t.description;
        return name?.toLowerCase?.()?.includes(this.keyword.toLowerCase())
          || this.$utils.htmlToText(description)?.toLowerCase?.()?.includes(this.keyword.toLowerCase());
      }) || spaceTemplates;
    },
    nameToDelete() {
      return this.spaceTemplateToDelete && this.$te(this.spaceTemplateToDelete?.name) ? this.$t(this.spaceTemplateToDelete?.name) : this.spaceTemplateToDelete?.name;
    },
    selectionLabel() {
      if (this.$root.allSpaceTemplatesSelected) {
        return this.$t('spaceTemplate.label.allSpaceTemplatesSelected', {
          0: `<strong>${this.$root.spaceTemplatesSize}</strong>`,
        });
      } else {
        return this.$t('spaceTemplate.label.selectedSpaceTemplatesCount', {
          0: `<strong>${this.$root.selectedSpaceTemplates.length}</strong>`,
        });
      }
    },
  },
  watch: {
    keyword() {
      this.$root.allSpaceTemplatesSelected = false;
      this.$root.selectedSpaceTemplates = [];
    },
  },
  created() {
    this.$root.$on('space-templates-delete', this.deleteSpaceTemplateConfirm);
  },
  beforeDestroy() {
    this.$root.$off('space-templates-delete', this.deleteSpaceTemplateConfirm);

  },
  methods: {
    deleteSpaceTemplateConfirm(spaceTemplate) {
      this.spaceTemplateToDelete = spaceTemplate;
      if (this.spaceTemplateToDelete) {
        this.$refs.deleteConfirmDialog.open();
      }
    },
    deleteSpaceTemplate(spaceTemplate) {
      this.loading = true;
      this.$spaceTemplateService.deleteSpaceTemplate(spaceTemplate.id)
        .then(() => {
          this.$root.$emit('space-templates-deleted', spaceTemplate);
          this.$root.$emit('alert-message', this.$t('spaceTemplate.delete.success'), 'success');
        })
        .catch(() => this.$root.$emit('alert-message', this.$t('spaceTemplate.delete.error'), 'error'))
        .finally(() => this.loading = false);
    },
    resolveSpaceTemplateType(spaceTemplate) {
      if (this.$root.subspacesTemplateIds.includes(spaceTemplate?.id)) {
        return this.$t('spaceTemplate.subspaceTemplate.type.label');
      }
      if (spaceTemplate?.allowedSubspaceTemplates?.length) {
        return this.$t('spaceTemplate.parentSpaceTemplate.type.label');
      }
      return '-';
    },
  },
};
</script>
