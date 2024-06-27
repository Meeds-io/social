<!--
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 -->

<template>
  <exo-drawer
    id="quickSearchUsersListDrawer"
    ref="quickSearchUsersListDrawer"
    allow-expand
    right
    @expand-updated="expanded = $event">
    <template slot="title">
      <span class="text-color ma-auto">
        {{ $t('profileContactInformation.quick.search.label', {0: propertyValue}) }}
      </span>
    </template>
    <template slot="content">
      <people-toolbar
        filter="all"
        :hide-filter="true"
        :hide-right-filter-button="true"
        :compact="true"
        :filter-message="filterMessage"
        filter-message-class="position-absolute filter-message ps-1"
        class="transparent"
        @keyword-changed="keyword = $event" />
      <complementary-filter
        class="mt-n1 z-index-two position-relative"
        :object-ids="objectIds"
        :attributes="listProperties"
        :show-message="false"
        index-alias="profile_alias"
        :loading-call-back="loadingCallBack"
        :parent-expanded="expanded"
        @build-suggestions-terminated="buildSuggestionsTerminated"
        @filter-changed="selectedSuggestionsUpdated"
        @filter-suggestion-unselected="unselectSuggestion"
        @filter-drawer-closed="filterDrawerClosed" />
      <div
        v-if="!isSearching && !listUsers.length"
        class="mt-auto mb-auto pt-5 align-center">
        <v-icon
          size="50">
          fas fa-users
        </v-icon>
        <p>
          {{ $t('profileContactInformation.quickSearch.noPeople') }}
        </p>
        <v-btn
          class="btn btn-primary"
          @click="resetFilter">
          {{ $t('profileContactInformation.quickSearch.resetFilter') }}
        </v-btn>
      </div>
      <div v-else>
        <div
          v-if="expanded"
          class="pa-2 quickSearchResultExpanded">
          <v-container
            fluid
            class="pa-3">
            <v-row>
              <v-col
                v-for="user in listUsers"
                :key="user.id"
                :id="`peopleCardItem${user.id}`"
                cols="12"
                sm="6"
                md="4"
                lg="3"
                xl="3"
                class="pa-2">
                <people-card
                  :user="user"
                  :user-navigation-extensions="userExtensions"
                  :profile-action-extensions="profileActionExtensions"
                  :user-card-settings="userCardSettings" />
              </v-col>
            </v-row>
          </v-container>
        </div>
        <div
          v-else
          class="pt-2 mt-n1 quickSearchResultExpanded quickSearchResultCollapsed">
          <people-card
            v-for="user in listUsers"
            :key="user.id"
            :id="`peopleCardItem${user.id}`"
            :user="user"
            :user-navigation-extensions="userExtensions"
            :profile-action-extensions="profileActionExtensions"
            :compact-display="true" />
        </div>
      </div>
    </template>
    <template
      v-if="hasMore"
      slot="footer">
      <div class="ma-auto d-flex width-full">
        <v-btn
          :loading="isLoading"
          class="btn btn-primary width-full"
          text
          outlined
          @click="search(true)">
          {{ $t('Search.button.loadMore') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {

  data() {
    return {
      users: [],
      pageSize: 9,
      limit: 0,
      offset: 0,
      fieldsToRetrieve: 'all,relationshipStatus,settings',
      hasMore: false,
      profileActionExtensions: [],
      userExtensions: [],
      profileSetting: null,
      expanded: false,
      propertyValue: null,
      isSearching: false,
      keyword: null,
      hasCombinations: false,
      selectedSuggestions: [],
      isLoading: false
    };
  },
  props: {
    properties: {
      type: Array,
      default: () => []
    },
    userCardSettings: {
      type: Object,
      default: null
    }
  },
  computed: {
    listUsers() {
      return this.users;
    },
    objectIds() {
      return this.listUsers.map(user => user.id);
    },
    listProperties() {
      return this.profileSetting && this.properties?.filter(property => property !== Object.keys(this.profileSetting)[0])
          || this.properties;
    },
    filterMessage() {
      return this.hasCombinations && this.$t('complementaryFilter.suggestions.message') || ' ';
    }
  },
  watch: {
    isSearching() {
      this.loadingCallBack(this.isSearching);
    },
    keyword() {
      this.users = [];
      this.search();
    },
  },
  created() {
    this.refreshExtensions();
    this.refreshUserExtensions();
    document.addEventListener('profile-extension-updated', this.refreshExtensions);
    document.addEventListener('user-extension-updated', this.refreshUserExtensions);
    this.$root.$on('open-quick-search-users-drawer', this.open);
  },
  methods: {
    refreshUserExtensions() {
      this.userExtensions = extensionRegistry.loadExtensions('user-extension', 'navigation') || [];
    },
    resetFilter() {
      this.$root.$emit('filter-reset-selections');
      this.$root.$emit('reset-filter');
    },
    loadingCallBack(isLoading) {
      if (isLoading) {
        this.$refs.quickSearchUsersListDrawer.startLoading();
      } else {
        this.$refs.quickSearchUsersListDrawer.endLoading();
      }
    },
    refreshExtensions() {
      this.profileActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
      this.profileActionExtensions.sort((elementOne, elementTwo) => (elementOne.order || 100) - (elementTwo.order || 100));
    },
    selectedSuggestionsUpdated(suggestions) {
      if (!suggestions.length && !this.listUsers.length) {
        this.$root.$emit('update-filter-suggestions');
        return;
      }
      this.isSearching = true;
      this.selectedSuggestions = suggestions;
      this.users = [];
      this.search();
    },
    unselectSuggestion(suggestion) {
      delete this.profileSetting[suggestion.key];
    },
    buildSuggestionsTerminated(suggestions) {
      this.hasCombinations = suggestions?.length;
    },
    filterDrawerClosed(close) {
      if (close) {
        this.close();
      }
    },
    close() {
      this.$refs.quickSearchUsersListDrawer.close();
    },
    open(profileSetting, propertyValue) {
      this.resetFilter();
      this.profileSetting = profileSetting;
      this.propertyValue = propertyValue;
      this.hasCombinations = false;
      this.selectedSuggestions = [];
      this.users = [];
      this.search(true);
      this.$refs.quickSearchUsersListDrawer.open();
    },
    search(loadMore) {
      if (this.selectedSuggestions?.length) {
        this.selectedSuggestions.forEach(suggestion => {
          this.profileSetting[suggestion.key] = suggestion.value;
        });
      }
      if (this.selectedSuggestions?.length) {
        this.selectedSuggestions.forEach(suggestion => {
          this.profileSetting[suggestion.key] = suggestion.value;
        });
      }
      this.isSearching = true;
      this.isLoading = loadMore;
      if (this.abortController) {
        this.abortController.abort();
      }
      this.abortController = new AbortController();
      this.offset = this.users.length || 0;
      this.limit = this.limit || this.pageSize;
      this.$userService.getUsersByAdvancedFilter(this.profileSetting, this.offset, this.limit + 1, this.fieldsToRetrieve,'all', this.keyword, false, this.abortController.signal, 'false').then(data => {
        this.users.push(...data.users);
        this.hasMore = data.users?.length > this.limit;
      }).finally(() => {
        this.abortController = null;
        this.isSearching = false;
        this.isLoading = false;
        this.$root.$emit('update-filter-suggestions');
      });
    }
  }
};
</script>
