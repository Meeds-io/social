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
  <v-card
    class="d-flex flex-column"
    min-height="calc(var(--100vh, 100vh) - 220px)"
    flat>
    <div id="spacesAdministrationListBody" class="flex-grow-1 flex-shrink-1 pt-2">
      <v-data-table
        v-if="spacesSize || !initialized"
        v-model="selectedSpaces"
        :headers="headers"
        :items="spaces"
        :loading="loadingSpaces && !$root.isMobile"
        :hide-default-header="$root.isMobile"
        :options.sync="options"
        :disable-sort="!canSort || $root.isMobile"
        :must-sort="canSort"
        :show-select="!$root.isMobile"
        class="spacesAdministrationTable full-width px-0"
        disable-pagination
        hide-default-footer>
        <template v-if="selectedSpaces.length" slot="body.prepend">
          <tr>
            <td :colspan="headers.length + 1" class="px-0">
              <v-alert
                :icon="false"
                class="ma-0 ps-5 no-border-radius"
                border="left"
                type="info"
                colored-border>
                <!-- eslint-disable-next-line -->
                <div v-html="selectionLabel" /> <!-- NOSONAR -->
              </v-alert>
            </td>
          </tr>
        </template>
        <template slot="item" slot-scope="props">
          <spaces-administration-item
            :key="props.item.id"
            :space="props.item"
            :headers="headers"
            :selected="props.isSelected"
            :select="props.select" />
        </template>
      </v-data-table>
      <v-card
        v-else-if="!loadingSpaces"
        min-height="calc(var(--100vh, 100vh) - 280px)"
        class="d-flex text-center noSpacesYetBlock"
        flat>
        <div class="ma-auto noSpacesYet">
          <p class="noSpacesYetIcons">
            <v-icon class="fa-9x">fa-chevron-left</v-icon>
            <v-icon class="fa-9x">fa-chevron-right</v-icon>
          </p>
          <template v-if="spacesSize > 0">
            <p class="text-title">
              {{ $t('spacesList.label.noResults') }}
            </p>
          </template>
          <template v-else>
            <p class="text-title">
              {{ $t('spacesList.label.noSpacesYet') }}
            </p>
            <div>
              {{ $t('spacesList.label.noSpacesYetDescription1') }}
            </div>
            <span>
              {{ $t('spacesList.label.noSpacesYetDescription2') }}
              <v-btn
                link
                text
                class="primary--text px-0 pb-1 addNewSpaceLink"
                @click="$root.$emit('addNewSpace')">
                {{ $t('spacesList.label.noSpacesLink') }}
              </v-btn>
            </span>
          </template>
        </div>
      </v-card>
    </div>
    <div id="spacesListFooter" class="flex-grow-0 flex-shrink-0 pb-5 border-box-sizing px-2">
      <v-btn
        v-if="canShowMore"
        :loading="loadingSpaces"
        class="loadMoreButton border-color elevation-0 ma-auto"
        block
        @click="loadNextPage">
        {{ $t('spacesList.button.showMore') }}
      </v-btn>
    </div>
  </v-card>
</template>
<script>
export default {
  props: {
    keyword: {
      type: String,
      default: null,
    },
    loadingSpaces: {
      type: Boolean,
      default: false,
    },
    selectedTemplateId: {
      type: String,
      default: () => '0',
    },
  },
  data: () => ({
    initialized: false,
    loading: false,
    offset: 0,
    pageSize: 25,
    spacesSize: 0,
    filter: 'all',
    expand: 'managers,groupBinding',
    spaces: [],
    options: {
      sortBy: ['title'],
      sortDesc: [false],
    },
    allSpacesSelected: false,
    selectedSpaces: [],
  }),
  computed: {
    canShowMore() {
      return this.spaces?.length && this.spacesSize > this.spaces.length;
    },
    canSort() {
      // Sort is made by pertinence
      // When search by keyword, thus disable
      // when the text search is used
      return !this.keyword?.length;
    },
    headers() {
      const headers = this.$root.isMobile && [
        {
          text: this.$t('social.spaces.administration.manageSpaces.name'),
          value: 'title',
          align: 'left',
          sortable: false,
          class: 'space-name-header px-0',
          width: '70%'
        },
        {
          text: this.$t('social.spaces.administration.manageSpaces.users'),
          value: 'membersCount',
          align: 'center',
          sortable: false,
          class: 'space-users-header px-0',
          width: '15%'
        },
        {
          text: this.$t('social.spaces.administration.manageSpaces.actions'),
          value: 'actions',
          align: 'center',
          sortable: false,
          class: 'space-actions-header px-0',
          width: '15%'
        },
      ] || [
        {
          text: this.$t('social.spaces.administration.manageSpaces.name'),
          value: 'title',
          align: 'left',
          sortable: true,
          class: 'space-name-header pe-0',
          width: 'auto'
        },
        {
          text: this.$t('social.spaces.administration.manageSpaces.template'),
          value: 'templateId',
          align: 'center',
          sortable: false,
          class: 'space-template-header px-1',
          width: '90px'
        },
        {
          text: this.$t('social.spaces.administration.manageSpaces.admins'),
          value: 'managersCount',
          align: 'center',
          sortable: false,
          class: 'space-admins-header px-1',
          width: '120px'
        },
        {
          text: this.$t('social.spaces.administration.manageSpaces.users'),
          value: 'membersCount',
          align: 'center',
          sortable: false,
          class: 'space-users-header px-1',
          width: '90px'
        },
        {
          text: this.$t('social.spaces.administration.manageSpaces.bindingStatus'),
          value: 'totalBoundUsers',
          align: 'center',
          sortable: false,
          class: 'space-group-binding-header px-1',
          width: '90px'
        },
        {
          text: this.$t('social.spaces.administration.manageSpaces.actions'),
          value: 'actions',
          align: 'center',
          sortable: false,
          class: 'space-actions-header px-1',
          width: '90px'
        },
      ];
      if (!this.$root.isMobile) {
        this.$root.tableColumnExtensions.forEach(extension => headers.splice(1, 0, {
          ...extension.header,
          text: this.$t(extension.titleKey)
        }));
      }
      return headers;
    },
    selectionLabel() {
      if (this.allSpacesSelected) {
        return this.$t('social.spaces.administration.manageSpaces.allSpacesSelected', {
          0: `<strong>${this.spacesSize}</strong>`,
        });
      } else if (this.selectedSpaces.length === this.spaces.length && this.spaces.length < this.spacesSize) {
        return this.$t('social.spaces.administration.manageSpaces.allDisplayedSpacesSelected', {
          0: `<strong>${this.selectedSpaces.length}</strong>`,
          1: '<a class="primary--text font-weight-bold" onclick="window.dispatchEvent(new CustomEvent(\'select-all-spaces\'))">',
          2: this.spacesSize,
          3: '</a>',
        });
      } else {
        return this.$t('social.spaces.administration.manageSpaces.selectedSpacesCount', {
          0: `<strong>${this.selectedSpaces.length}</strong>`,
        });
      }
    },
  },
  watch: {
    keyword() {
      if (this.initialized) {
        this.offset = 0;
        this.spaces = [];
        this.searchSpaces();
      }
    },
    offset() {
      if (this.initialized) {
        this.searchSpaces();
      }
    },
    filter() {
      if (this.initialized) {
        this.offset = 0;
        this.spaces = [];
        this.searchSpaces();
      }
    },
    selectedTemplateId() {
      if (this.initialized) {
        this.offset = 0;
        this.spaces = [];
        this.searchSpaces();
      }
    },
    loading() {
      this.$emit('loading-spaces', this.loading);
    },
    options: {
      handler () {
        if (!this.keyword?.length) {
          this.searchSpaces(true);
        }
      },
      deep: true,
    },
    selectedSpaces() {
      this.allSpacesSelected = false;
    },
  },
  created() {
    this.searchSpaces();
    window.addEventListener('select-all-spaces', this.selectAllSpaces);
    this.$root.$on('spaces-administration-list-refresh', this.refreshSpaces);
  },
  beforeDestroy() {
    window.removeEventListener('select-all-spaces', this.selectAllSpaces);
    this.$root.$off('spaces-administration-list-refresh', this.refreshSpaces);
  },
  methods: {
    refreshSpaces() {
      this.searchSpaces(true);
    },
    async searchSpaces(refresh) {
      if (this.loading) {
        return;
      }
      const offset = refresh ? 0 : this.offset;
      const limit = refresh ? this.offset + this.pageSize : this.pageSize;
      const sortBy = this.options.sortBy[0];
      const sortDesc = this.options.sortDesc[0];

      this.loading = true;
      try {
        const customSort = this.$root.tableColumnExtensions?.find(e => e.sortBy === sortBy)?.customSort;
        if (customSort
            && !this.keyword?.length
            && this.filter === 'all') {
          this.spaces = await customSort({
            offset: offset,
            limit: limit,
            expand: this.expand,
            templateId: (this.selectedTemplateId && Number(this.selectedTemplateId)) ? this.selectedTemplateId : null,
            sortDesc,
            currentSpaces: this.spaces,
            currentSpacesSize: this.spacesSize,
          });
        } else {
          if (!offset) {
            this.spaces = [];
          }
          const data = await this.$spaceService.getSpaces(
            this.keyword,
            offset,
            limit,
            this.filter,
            this.expand,
            this.selectedTemplateId && Number(this.selectedTemplateId),
            'title',
            sortDesc ? 'desc' : 'asc',
          );
          if (offset) {
            this.spaces.push(...data.spaces);
          } else {
            this.spaces = data.spaces || [];
          }
          this.spacesSize = data?.size || 0;
          this.$emit('loaded', this.spacesSize);
        }
      } finally {
        this.loading = false;
        this.initialized = true;
      }
    },
    selectAllSpaces() {
      this.allSpacesSelected = true;
    },
    loadNextPage() {
      this.offset += this.pageSize;
    },
  }
};
</script>