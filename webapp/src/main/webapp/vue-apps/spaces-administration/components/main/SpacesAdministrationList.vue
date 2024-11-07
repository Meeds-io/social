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
        :headers="headers"
        :items="spaces"
        :loading="loadingSpaces"
        :disable-sort="$root.isMobile"
        :hide-default-header="$root.isMobile"
        :custom-sort="applySortOnItems"
        must-sort
        disable-pagination
        hide-default-footer
        class="spacesAdministrationTable px-0">
        <template slot="item" slot-scope="props">
          <spaces-administration-item
            :key="props.item.id"
            :space="props.item" />
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
    offset: 0,
    pageSize: 25,
    spacesSize: 0,
    filter: 'all',
    expand: 'managers,groupBinding',
    spaces: [],
  }),
  computed: {
    canShowMore() {
      return this.spaces?.length && this.spacesSize > this.spaces.length;
    },
    headers() {
      return this.$root.isMobile && [
        {
          text: this.$t('social.spaces.administration.manageSpaces.name'),
          value: 'displayName',
          align: 'left',
          sortable: true,
          class: 'space-name-header',
          width: '20%'
        },
        {
          text: this.$t('social.spaces.administration.manageSpaces.actions'),
          value: 'actions',
          align: 'center',
          sortable: false,
          class: 'space-actions-header',
          width: '50px'
        },
      ] || [
        {
          text: this.$t('social.spaces.administration.manageSpaces.name'),
          value: 'displayName',
          align: 'left',
          sortable: true,
          class: 'space-name-header pe-0',
          width: 'auto'
        },
        {
          text: this.$t('social.spaces.administration.manageSpaces.description'),
          value: 'description',
          align: 'left',
          sortable: false,
          class: 'space-description-header pe-0',
          width: 'auto'
        },
        {
          text: this.$t('social.spaces.administration.manageSpaces.template'),
          value: 'templateId',
          align: 'center',
          sortable: true,
          class: 'space-template-header px-1',
          width: '120px'
        },
        {
          text: this.$t('social.spaces.administration.manageSpaces.registration'),
          value: 'registration',
          align: 'center',
          sortable: true,
          class: 'space-registration-header px-1',
          width: '120px'
        },
        {
          text: this.$t('social.spaces.administration.manageSpaces.visibility'),
          value: 'visibility',
          align: 'center',
          sortable: false,
          class: 'space-visibility-header px-1',
          width: '120px'
        },
        {
          text: this.$t('social.spaces.administration.manageSpaces.admins'),
          value: 'managersCount',
          align: 'center',
          sortable: false,
          class: 'space-admins-header px-1',
          width: '90px'
        },
        {
          text: this.$t('social.spaces.administration.manageSpaces.users'),
          value: 'membersCount',
          align: 'center',
          sortable: false,
          class: 'space-users-header px-1',
          width: '60px'
        },
        {
          text: this.$t('social.spaces.administration.manageSpaces.bindingStatus'),
          value: 'totalBoundUsers',
          align: 'center',
          sortable: false,
          class: 'space-group-binding-header px-1',
          width: '60px'
        },
        {
          text: this.$t('social.spaces.administration.manageSpaces.actions'),
          value: 'actions',
          align: 'center',
          sortable: false,
          class: 'space-actions-header px-1',
          width: '60px'
        },
      ];
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
  }, 
  created() {
    this.searchSpaces();
    this.$root.$on('spaces-administration-list-refresh', this.refreshSpaces);
  },
  beforeDestroy() {
    this.$root.$off('spaces-administration-list-refresh', this.refreshSpaces);
  },
  methods: {
    refreshSpaces() {
      this.searchSpaces(true);
    },
    searchSpaces(refresh) {
      const offset = refresh ? 0 : this.offset;
      const limit = refresh ? this.offset + this.pageSize : this.pageSize;
      this.$emit('loading-spaces', true);
      return this.$spaceService.getSpaces(
        this.keyword,
        offset,
        limit,
        this.filter,
        this.expand,
        this.selectedTemplateId && Number(this.selectedTemplateId)
      )
        .then(data => {
          if (offset) {
            this.spaces.push(...data.spaces);
          } else {
            this.spaces = data.spaces || [];
          }
          this.spacesSize = data?.size || 0;
          this.$emit('loaded', this.spacesSize);
        })
        .finally(() => {
          this.$emit('loading-spaces', false);
          this.initialized = true;
        });
    },
    loadNextPage() {
      this.offset += this.pageSize;
    },
  }
};
</script>