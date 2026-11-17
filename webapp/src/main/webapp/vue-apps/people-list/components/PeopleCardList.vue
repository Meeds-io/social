<template>
  <v-card
    :class="{
      'mobile-cards': compact,
    }"
    class="people-list-application"
    flat>
    <v-card
      class="position-relative mx-4 overflow-hidden"
      height="8"
      flat>
      <v-progress-linear
        v-if="displayLoading"
        class="position-absolute"
        color="primary"
        indeterminate
        height="2" />
    </v-card>
    <div
      v-if="initialized"
      class="people-list-body"
      :class="noMargins && compact && 'pa-0' || (noMargins && 'pt-1 px-3 pb-0') || 'px-4 pb-4'">
      <div
        v-if="filteredPeople && filteredPeople.length"
        ref="peopleCardsRow"
        class="d-flex flex-wrap ma-n2 border-box-sizing">
        <div
          v-for="user in filteredPeople"
          :key="user.id"
          :id="`peopleCardItem${user.id}`"
          :class="[noMargins && compact && 'pa-0' || 'pa-2', 'border-box-sizing', `col-${columnSpan}`]">
          <people-card
            :user="user"
            :space-id="spaceId"
            :space-members-extensions="spaceMembersActionExtensions"
            :user-navigation-extensions="userExtensions"
            :profile-action-extensions="profileActionExtensions"
            :compact-display="compactDisplay"
            :mobile-display="mobileDisplay" />
        </div>
      </div>
      <div
        v-else-if="!displayLoading"
        role="status"
        class="d-flex text-center noPeopleYetBlock">
        <div class="ma-auto noPeopleYet">
          <p class="noPeopleYetIcons">
            <v-icon aria-hidden="true">fa-users</v-icon>
          </p>
          <template v-if="directoryEmpty">
            <p class="text-title">
              {{ $t('peopleList.label.noResults') }}
            </p>
            <p v-if="$root.canEdit" class="people-list-review-settings">
              {{ reviewSettingsPrefix }}<button
                class="people-list-review-settings-link primary--text pa-0"
                type="button"
                @click="$root.$emit('people-list-settings-open')">{{ $t('peopleList.label.reviewSettings.link') }}</button>{{ reviewSettingsSuffix }}
            </p>
          </template>
          <template v-else-if="filter === 'connections'">
            <p class="text-title">
              {{ $t('peopleList.label.noConnection') }}
            </p>
          </template>
          <template v-else-if="hasPeople">
            <p class="text-title">
              {{ $t('peopleList.label.noResults') }}
            </p>
          </template>
          <template v-else>
            <p class="text-title">
              {{ $t('peopleList.label.noPeopleYet') }}
            </p>
          </template>
          <v-btn
            v-if="!directoryEmpty"
            class="btn btn-primary"
            @click="resetFilters">
            {{ $t('pepole.advanced.filter.button.reset') }}
          </v-btn>
        </div>
      </div>
    </div>
    <v-card-actions
      v-if="!noLoadMoreButton"
      class="people-list-footer pt-0 px-4 border-box-sizing">
      <v-btn
        v-if="hasMore"
        :loading="displayLoading"
        class="loadMoreButton ma-auto btn"
        block
        @click="loadNextPage">
        {{ $t('peopleList.button.showMore') }}
      </v-btn>
    </v-card-actions>
  </v-card>
</template>
<script>
export default {
  props: {
    keyword: {
      type: String,
      default: null,
    },
    filter: {
      type: String,
      default: null,
    },
    loading: {
      type: String,
      default: null,
    },
    spaceId: {
      type: Number,
      default: 0,
    },
    peopleCount: {
      type: Number,
      default: 0,
    },
    isManager: {
      type: Boolean,
      default: false,
    },
    compactDisplay: {
      type: Boolean,
      default: false,
    },
    mobileDisplay: {
      type: Boolean,
      default: false,
    },
    noMargins: {
      type: Boolean,
      default: false,
    },
    noLoadMoreButton: {
      type: Boolean,
      default: false,
    },
    // number of users listed per page and added by 'Show more'
    itemsPerPage: {
      type: Number,
      default: 12,
    },
    // the People Directory lists through its portlet resource, which applies
    // the instance's filter server-side; other lists keep their REST calls
    usersResourceUrl: {
      type: String,
      default: null,
    },
    // whether the instance filters its users by profile attribute
    directoryFiltered: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    profileExtensions: [],
    userExtensions: [],
    spaceMemberExtensions: [],
    fieldsToRetrieve: 'settings,all,binding',
    userType: 'internal',
    initialized: false,
    hasPeople: false,
    hasMore: false,
    offset: 0,
    pageSize: 12,
    limit: 12,
    users: [],
    limitToFetch: 0,
    originalLimitToFetch: 0,
    abortController: null,
    loadingPeople: false,
    userCardSettings: null,
    advancedFilterSettings: null,
    resizeObserver: null,
    cardsRowWidth: 0,
    baseCardMinWidth: 200,
    baseActionIconsCount: 5,
    actionIconWidth: 36,
    // PeopleDirectoryService.MAX_LIMIT: the users resource refuses more
    maxResourceLimit: 100,
    // the search the listed directory users answer, null when they must be
    // fetched again from the first one
    directoryQueryKey: null,
  }),
  computed: {
    displayLoading() {
      return !this.noLoadMoreButton && (this.loadingPeople || this.loading);
    },
    profileActionExtensions() {
      return [...this.profileExtensions].sort((a, b) => (a.order || 100) - (b.order || 100));
    },
    spaceMembersActionExtensions() {
      return [...this.spaceMemberExtensions].sort((a, b) => (a.order || 100) - (b.order || 100));
    },
    compact() {
      return this.compactDisplay || this.mobileDisplay;
    },
    maxColumnsCount() {
      const smallestSpan = this.$attrs.xl || this.$attrs.lg || this.$attrs.md || this.$attrs.sm || 4;
      return Math.max(1, Math.floor(12 / smallestSpan));
    },
    maxActionIconsCount() {
      return this.filteredPeople.reduce((max, user) => {
        const navigationIconsCount = this.userExtensions.filter(extension => extension.enabled(user)).length;
        const actionIconsCount = this.profileActionExtensions.filter(extension => extension.enabled(user?.dataEntity || user, null, true)).length;
        return Math.max(max, navigationIconsCount + actionIconsCount);
      }, 0) + (this.spaceId && 1 || 0);
    },
    cardMinWidth() {
      const extraIconsCount = Math.max(0, this.maxActionIconsCount - this.baseActionIconsCount);
      return this.baseCardMinWidth + extraIconsCount * this.actionIconWidth;
    },
    columnsCount() {
      if (!this.cardsRowWidth) {
        return this.maxColumnsCount;
      }
      const fittingColumnsCount = Math.max(1, Math.floor(this.cardsRowWidth / this.cardMinWidth));
      return Math.min(this.maxColumnsCount, fittingColumnsCount);
    },
    columnSpan() {
      return 12 / this.columnsCount;
    },
    // no user matches the directory's own filter: nothing the viewer did
    // narrows the list
    directoryEmpty() {
      return !!this.usersResourceUrl
        && this.directoryFiltered
        && !this.keyword
        && !this.advancedFilterSettings
        && this.filter !== 'connections';
    },
    // the sentence around the settings link, whose label is translated apart
    reviewSettingsParts() {
      return this.$t('peopleList.label.reviewSettings', {0: '\u0000'}).split('\u0000');
    },
    reviewSettingsPrefix() {
      return this.reviewSettingsParts[0];
    },
    reviewSettingsSuffix() {
      return this.reviewSettingsParts[1] || '';
    },
    filteredPeople() {
      if (!this.keyword || !this.loadingPeople) {
        return this.users;
      } else {
        return this.users.slice().filter(user => {
          return user.fullname && user.fullname.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0
                 || user.position && user.position.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0;
        });
      }
    },
  },
  watch: {
    keyword() {
      if (!this.keyword) {
        this.resetSearch();
        this.searchPeople();
        return;
      }
      this.searchPeople();

    },
    hasMore() {
      this.$emit('has-more', this.hasMore);
    },
    itemsPerPage() {
      // the limitToFetch watcher refetches when the fetched window changes;
      // an unchanged window already lists the right users
      this.applyItemsPerPage();
    },
    loadingPeople() {
      this.$emit('loading', this.loadingPeople);
    },
    limitToFetch() {
      this.searchPeople();
    },
    filter() {
      this.searchPeople();
    },
  },
  created() {
    this.applyItemsPerPage();

    // To refresh menu when a new extension is ready to be used
    document.addEventListener('extension-profile-extension-action-updated', this.refreshExtensions);
    document.addEventListener('extension-space-member-extension-action-updated', this.refreshExtensions);
    document.addEventListener('user-extension-updated', this.refreshUserExtensions);
    document.addEventListener('people-list-refresh', this.searchPeopleNoFilters);
    // the instance's settings were saved: its filter may have changed
    this.$root.$on('people-list-directory-updated', this.searchPeopleNoFilters);

    this.$root.$on('reset-advanced-filter', () => {
      this.advancedFilterSettings = null;
      this.searchPeople();
    });
    this.$root.$on('advanced-filter', profileSettings => this.searchPeople(profileSettings));

    this.refreshExtensions();
    this.refreshUserExtensions();
  },
  mounted() {
    this.resizeObserver = new ResizeObserver(this.computeCardsRowWidth);
    this.resizeObserver.observe(this.$el);
    this.computeCardsRowWidth();
  },
  beforeDestroy() {
    document.removeEventListener('extension-profile-extension-action-updated', this.refreshExtensions);
    document.removeEventListener('extension-space-member-extension-action-updated', this.refreshExtensions);
    document.removeEventListener('user-extension-updated', this.refreshUserExtensions);
    document.removeEventListener('people-list-refresh', this.searchPeopleNoFilters);
    this.resizeObserver?.disconnect?.();
  },
  methods: {
    computeCardsRowWidth() {
      this.cardsRowWidth = this.$refs.peopleCardsRow?.clientWidth || 0;
    },
    resetFilters() {
      this.$root.$emit('reset-filter');
      this.$root.$emit('reset-advanced-filter');
      this.$root.$emit('reset-advanced-filter-count');
    },
    refreshUserExtensions() {
      this.userExtensions = extensionRegistry.loadExtensions('user-extension', 'navigation') || [];
    },
    refreshExtensions() {
      this.profileExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
      this.spaceMemberExtensions = this.spaceId && extensionRegistry.loadExtensions('space-member-extension', 'action') || [];
    },
    searchPeopleNoFilters() {
      // the saved settings may list other users for the same search
      this.directoryQueryKey = null;
      this.searchPeople();
    },
    searchPeople(profileSettings) {
      this.loadingPeople = true;
      // Using 'limitToFetch + 1' to retrieve current user and then delete it from result
      // to finally let only 'limitToFetch' users
      if (this.abortController) {
        this.abortController.abort();
      }
      this.abortController = new AbortController();
      let searchUsersFunction;
      if (profileSettings) {
        this.advancedFilterSettings = profileSettings;
      }
      if (this.usersResourceUrl && (!this.filter || this.filter === 'all' || this.filter === 'connections')) {
        searchUsersFunction = this.getDirectoryUsers(this.limitToFetch + 1, this.abortController.signal);
      } else if (this.filter === 'connections') {
        searchUsersFunction = this.$userService.getConnections(this.keyword, this.offset, this.limitToFetch + 1, this.fieldsToRetrieve, this.abortController.signal);
      } else if (this.filter === 'member'
          || this.filter === 'manager'
          || this.filter === 'redactor'
          || this.filter === 'publisher'
          || this.filter === 'disabled') {
        searchUsersFunction = this.$spaceService.getSpaceMemberships({
          query: this.keyword,
          offset: this.offset,
          limit: this.limitToFetch + 1,
          status: this.filter,
          expand: `users,${this.fieldsToRetrieve}`,
          space: this.spaceId,
          returnSize: true,
          signal: this.abortController.signal,
        }).then(data => {
          this.$root.space.membersCount = data.size;
          return {
            size: data.size || 0,
            users: data?.spacesMemberships?.map?.(m => m.user),
          };
        });
      } else if (profileSettings) {
        this.advancedFilterSettings = profileSettings;
        searchUsersFunction = this.$userService.getUsersByAdvancedFilter(this.advancedFilterSettings, this.offset, this.limitToFetch + 1, this.fieldsToRetrieve,this.filter, this.keyword, false, this.abortController.signal);
      } else if (this.advancedFilterSettings) {
        searchUsersFunction = this.$userService.getUsersByAdvancedFilter(this.advancedFilterSettings, this.offset, this.limitToFetch + 1, this.fieldsToRetrieve,this.filter, this.keyword, false, this.abortController.signal);
      } else {
        searchUsersFunction = this.$userService.getUsers(this.keyword, this.offset, this.limitToFetch + 1, this.fieldsToRetrieve, this.abortController.signal);
      }
      return searchUsersFunction.then(data => {
        const users = data && data.users || [];
        this.users = users.slice(0, this.limitToFetch);
        this.$emit('loaded', data.size);
        this.hasPeople = this.hasPeople || data.size > 0;
        this.hasMore = users.length > this.limitToFetch;
        return this.$nextTick();
      })
        .then(() => {
          if (this.keyword && this.filteredPeople.length < this.originalLimitToFetch && this.users.length >= this.limitToFetch) {
            this.limitToFetch += this.pageSize;
          }
        })
        .finally(() => {
          if (!this.initialized) {
            this.$root.$applicationLoaded();
          }
          this.loadingPeople = false;
          this.initialized = true;
          this.abortController = null;
        });
    },
    /**
     * Lists the directory through its portlet resource, by requests of at
     * most 100 users, the resource's bound. When only the number of users
     * grew for the same search ('Show more'), the listed users are kept and
     * only the next ones are fetched; any other change fetches from the
     * first one.
     *
     * @param {number} count the number of users to fetch
     * @param {AbortSignal} signal aborts the requests
     * @returns {Promise<{users: Array, size: number}>} the users and their total number
     */
    getDirectoryUsers(count, signal) {
      const queryKey = JSON.stringify([
        this.keyword || '',
        this.filter === 'connections' ? 'connections' : 'all',
        this.advancedFilterSettings || null,
      ]);
      const listed = queryKey === this.directoryQueryKey && count > this.users.length ? this.users.slice() : [];
      this.directoryQueryKey = null;
      const fetchChunk = (users, size) => {
        if (users.length >= count || (size !== null && users.length >= size)) {
          return {users: users.slice(0, count), size: size || 0};
        }
        const params = new URLSearchParams({
          q: this.keyword || '',
          filter: this.filter === 'connections' ? 'connections' : 'all',
          offset: String(users.length),
          limit: String(Math.min(count - users.length, this.maxResourceLimit)),
        });
        Object.keys(this.advancedFilterSettings || {})
          .filter(name => this.advancedFilterSettings[name])
          .forEach(name => params.append(`attr.${name}`, this.advancedFilterSettings[name]));
        const separator = this.usersResourceUrl.includes('?') ? '&' : '?';
        return fetch(`${this.usersResourceUrl}${separator}${params.toString()}`, {
          method: 'GET',
          credentials: 'include',
          signal,
        }).then(resp => {
          if (!resp?.ok) {
            throw new Error('Error while listing the people directory');
          }
          return resp.json();
        }).then(data => {
          const chunk = data?.users || [];
          const allUsers = users.concat(chunk);
          return chunk.length ? fetchChunk(allUsers, data?.size || 0) : {users: allUsers, size: data?.size || 0};
        });
      };
      return Promise.resolve(fetchChunk(listed, null)).then(result => {
        this.directoryQueryKey = queryKey;
        return result;
      });
    },
    applyItemsPerPage() {
      this.pageSize = this.limit = this.itemsPerPage;
      this.originalLimitToFetch = this.limitToFetch = this.limit;
    },
    resetSearch() {
      if (this.limitToFetch !== this.originalLimitToFetch) {
        this.limitToFetch = this.originalLimitToFetch;
      }
    },
    loadNextPage() {
      this.originalLimitToFetch = this.limitToFetch += this.pageSize;
    },
  }
};
</script>

