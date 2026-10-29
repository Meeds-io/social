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
        v-if="$root.spacesSize || !$root.initialized"
        v-model="$root.selectedSpaces"
        :headers="headers"
        :items="$root.spaces"
        :loading="$root.loading && !$root.isMobile"
        :hide-default-header="$root.isMobile"
        :options.sync="options"
        :disable-sort="!canSort || $root.isMobile"
        :must-sort="sortEnabled"
        :show-select="!$root.isMobile"
        class="spacesAdministrationTable full-width px-0"
        disable-pagination
        hide-default-footer>
        <template slot="header.data-table-select" slot-scope="{on, props}">
          <v-checkbox
            v-on="on"
            v-bind="props"
            :disabled="$root.isBusy"
            on-icon="fas fa-check-square fa-lg primary--text"
            indeterminate-icon="fas fa-minus-square fa-lg"
            off-icon="far fa-square fa-lg"
            class="my-auto pt-2"
            @change="on.input" />
        </template>
        <template
          v-for="extension in headerExtensions"
          :slot="`header.${extension.header.value}`"
          slot-scope="{header}">
          <span
            :key="extension.name"
            class="d-inline-flex align-center">
            {{ header.text }}
            <component
              :is="extension.headerComponentName"
              :header="header" />
          </span>
        </template>
        <template v-if="$root.selectedSpaces.length && !$root.isBulkProcessing" slot="body.prepend">
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
          <spaces-administration-item
            :key="props.item.id"
            :space="props.item"
            :headers="headers"
            :selected="props.isSelected"
            :select="props.select" />
        </template>
      </v-data-table>
      <v-card
        v-else-if="!$root.loadingSpaces"
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
    <div
      v-if="$root.canShowMore"
      id="spacesListFooter"
      class="flex-grow-0 flex-shrink-0 pb-5 border-box-sizing px-2">
      <v-btn
        :loading="$root.loadingSpaces"
        :disabled="$root.isBusy"
        class="loadMoreButton border-color elevation-0 ma-auto"
        block
        @click="$root.loadNextPage">
        {{ $t('spacesList.button.showMore') }}
      </v-btn>
    </div>
  </v-card>
</template>
<script>
export default {
  data: () => ({
    allowOptionsWatching: true,
    options: {
      sortBy: ['title'],
      sortDesc: [false],
    },
  }),
  computed: {
    sortEnabled() {
      // Sort is made by pertinence
      // When search by keyword, thus disable
      // when the text search is used
      return !this.$root.keyword?.length && !this.$root.isBulkProcessing;
    },
    canSort() {
      // Busy disables the headers only: must-sort is part of the table
      // options, which a busy page leaves unchanged
      return this.sortEnabled && !this.$root.isBusy;
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
        // A contributed sort applies to the unfiltered list with no keyword only
        const filtered = this.$root.keyword?.length
          || this.$root.isFilteredByTemplate
          || this.$root.isFilteredByRegistration
          || this.$root.isFilteredByVisibility;
        // In rank order, as the row cells are
        headers.splice(1, 0, ...this.$root.tableColumnExtensions.map(extension => ({
          ...extension.header,
          sortable: !!extension.header?.sortable && !filtered,
          text: this.$t(extension.titleKey)
        })));
      }
      return headers;
    },
    headerExtensions() {
      // A contributed column names a registered component in headerComponentName:
      // its header slot renders the label, then that component with the Vuetify
      // header as its `header` prop. Vuetify places its sort arrow after them, or
      // before them for `align: 'end'`. A click on the header cell sorts, so a
      // component that handles a click stops it
      return this.$root.tableColumnExtensions
        .filter(extension => extension.headerComponentName && extension.header?.value);
    },
    selectionLabel() {
      if (this.$root.allSpacesSelected) {
        return this.$t('social.spaces.administration.manageSpaces.allSpacesSelected', {
          0: `<strong>${this.$root.spacesSize}</strong>`,
        });
      } else if (this.$root.selectedSpaces.length === this.$root.spaces.length && this.$root.spaces.length < this.$root.spacesSize) {
        return this.$t('social.spaces.administration.manageSpaces.allDisplayedSpacesSelected', {
          0: `<strong>${this.$root.selectedSpaces.length}</strong>`,
          1: `<a class="${this.$root.isBusy ? 'text-disabled-color' : 'primary--text'} font-weight-bold" aria-disabled="${!!this.$root.isBusy}" onclick="window.dispatchEvent(new CustomEvent('select-all-spaces'))">`,
          2: this.$root.spacesSize,
          3: '</a>',
        });
      } else {
        return this.$t('social.spaces.administration.manageSpaces.selectedSpacesCount', {
          0: `<strong>${this.$root.selectedSpaces.length}</strong>`,
        });
      }
    },
    selectedSpaces() {
      return this.$root.selectedSpaces;
    },
    isBulkProcessing() {
      return this.$root.isBulkProcessing;
    },
    isAllowOptionsWatching() {
      return !this.$root.isBulkProcessing && this.allowOptionsWatching;
    },
    rootSort() {
      return `${this.$root.sortBy}:${!!this.$root.sortDesc}`;
    },
  },
  watch: {
    isBulkProcessing() {
      if (this.isBulkProcessing) {
        this.allowOptionsWatching = false;
      } else {
        // Differ to not update list right after bulk processing finished
        this.$nextTick().then(() => window.setTimeout(() => this.allowOptionsWatching = true, 200));
      }
    },
    rootSort() {
      // The root's sort was reset: the table follows it, with no load of its own
      const sortBy = this.$root.sortBy;
      const sortDesc = !!this.$root.sortDesc;
      if (this.options?.sortBy?.[0] !== sortBy || !!this.options?.sortDesc?.[0] !== sortDesc) {
        this.options = {
          ...this.options,
          sortBy: [sortBy],
          sortDesc: [sortDesc],
        };
      }
    },
    options: {
      handler () {
        if (this.options?.sortBy?.[0] === this.$root.sortBy
            && !!this.options?.sortDesc?.[0] === !!this.$root.sortDesc) {
          // The list is already sorted this way
          return;
        }
        if (!this.$root.keyword?.length && this.isAllowOptionsWatching) {
          this.$root.sortBy = this.options?.sortBy?.[0];
          this.$root.sortDesc = this.options?.sortDesc?.[0];
          this.$root.searchSpaces(true);
        }
      },
      deep: true,
    },
  },
  created() {
    this.$root.searchSpaces();
    window.addEventListener('select-all-spaces', this.selectAllSpaces);
  },
  beforeDestroy() {
    window.removeEventListener('select-all-spaces', this.selectAllSpaces);
  },
  methods: {
    selectAllSpaces() {
      // Disabled while the page is busy, as the select-all checkbox
      if (!this.$root.isBusy) {
        this.$root.allSpacesSelected = true;
      }
    },
  }
};
</script>