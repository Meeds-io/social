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
      :headers="headers"
      :items="filteredSpaceTemplates"
      :loading="loading"
      :disable-sort="$root.isMobile"
      :hide-default-header="$root.isMobile"
      must-sort
      disable-pagination
      hide-default-footer
      class="spaceTemplatesTable px-5">
      <template slot="item" slot-scope="props">
        <space-templates-management-item
          :key="props.item.id"
          :space-template="props.item" />
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
    spaceTemplates: [],
    spaceTemplateToDelete: null,
    loading: false,
  }),
  computed: {
    headers() {
      return this.$root.isMobile && [
        {
          text: this.$t('spaceTemplates.label.name'),
          value: 'name',
          align: 'left',
          sortable: true,
          class: 'space-template-name-header',
          width: '20%'
        },
        {
          text: this.$t('spaceTemplates.label.actions'),
          value: 'actions',
          align: 'center',
          sortable: false,
          class: 'space-template-actions-header',
          width: '50px'
        },
      ] || [
        {
          text: this.$t('spaceTemplates.label.name'),
          value: 'name',
          align: 'left',
          sortable: true,
          class: 'space-template-name-header',
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
    filteredSpaceTemplates() {
      const spaceTemplates = this.spaceTemplates
        ?.filter?.(t => t.name)
        ?.map?.(t => {
          t = JSON.parse(JSON.stringify(t));
          t.spacesCount = this.$root.spacesCountByTemplates?.[t.id] || 0;
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
  },
  created() {
    this.$root.$on('space-templates-deleted', this.refreshSpaceTemplates);
    this.$root.$on('space-templates-created', this.refreshSpaceTemplates);
    this.$root.$on('space-templates-updated', this.refreshSpaceTemplates);
    this.$root.$on('space-templates-enabled', this.refreshSpaceTemplates);
    this.$root.$on('space-templates-disabled', this.refreshSpaceTemplates);
    this.$root.$on('space-templates-saved', this.refreshSpaceTemplates);
    this.$root.$on('space-templates-delete', this.deleteSpaceTemplateConfirm);
    this.refreshSpaceTemplates();
  },
  beforeDestroy() {
    this.$root.$off('space-templates-deleted', this.refreshSpaceTemplates);
    this.$root.$off('space-templates-created', this.refreshSpaceTemplates);
    this.$root.$off('space-templates-updated', this.refreshSpaceTemplates);
    this.$root.$off('space-templates-enabled', this.refreshSpaceTemplates);
    this.$root.$off('space-templates-disabled', this.refreshSpaceTemplates);
    this.$root.$off('space-templates-saved', this.refreshSpaceTemplates);
    this.$root.$off('space-templates-delete', this.deleteSpaceTemplateConfirm);
  },
  methods: {
    deleteSpaceTemplateConfirm(spaceTemplate) {
      this.spaceTemplateToDelete = spaceTemplate;
      if (this.spaceTemplateToDelete) {
        this.$refs.deleteConfirmDialog.open();
      }
    },
    refreshSpaceTemplates() {
      this.loading = true;
      return this.$spaceTemplateService.getSpaceTemplates(true)
        .then(spaceTemplates => this.spaceTemplates = spaceTemplates || [])
        .finally(() => this.loading = false);
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
  },
};
</script>
