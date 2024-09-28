<template>
  <v-card
    id="peopleListApplication"
    :class="{
      'mobile-cards': compact,
    }"
    flat>
    <v-progress-linear
      v-if="displayLoading"
      color="primary"
      class="position-absolute"
      indeterminate
      height="2" />
    <v-card-text
      v-if="initialized"
      id="peopleListBody"
      :class="noMargins && compact && 'pa-0' || (noMargins && 'px-3 pb-0') || 'pb-0'">
      <v-item-group>
        <v-container
          class="pa-0"
          fluid>
          <v-row v-if="filteredPeople && filteredPeople.length" class="ma-0 border-box-sizing">
            <v-col
              v-for="user in filteredPeople"
              :key="user.id"
              :id="`peopleCardItem${user.id}`"
              :sm="$attrs.sm || 6"
              :md="$attrs.md || 6"
              :lg="$attrs.lg || 4"
              :xl="$attrs.xl || 4"
              :class="noMargins && compact && 'pa-0' || 'pa-2'"
              cols="12">
              <people-card
                :user="user"
                :space-id="spaceId"
                :space-members-extensions="spaceMembersActionExtensions"
                :user-navigation-extensions="userExtensions"
                :profile-action-extensions="profileActionExtensions"
                :compact-display="compactDisplay"
                :mobile-display="mobileDisplay" />
            </v-col>
          </v-row>
          <div v-else-if="!displayLoading" class="d-flex text-center noPeopleYetBlock">
            <div class="ma-auto noPeopleYet">
              <p class="noPeopleYetIcons">
                <v-icon>fa-users</v-icon>
              </p>
              <template v-if="filter === 'connections'">
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
                class="btn btn-primary"
                @click="resetFilters">
                {{ $t('pepole.advanced.filter.button.reset') }}
              </v-btn>
            </div>
          </div>
        </v-container>
      </v-item-group>
    </v-card-text>
    <v-card-actions
      v-if="!noLoadMoreButton"
      id="peopleListFooter"
      class="pt-0 px-5 border-box-sizing">
      <v-btn
        v-if="canShowMore"
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
  },
  data: () => ({
    profileExtensions: [],
    userExtensions: [],
    spaceMemberExtensions: [],
    fieldsToRetrieve: 'settings,all,binding',
    userType: 'internal',
    initialized: false,
    hasPeople: false,
    offset: 0,
    pageSize: 20,
    limit: 20,
    users: [],
    limitToFetch: 0,
    originalLimitToFetch: 0,
    abortController: null,
    loadingPeople: false,
    userCardSettings: null,
    advancedFilterSettings: null,
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
    canShowMore() {
      return this.users.length >= this.limitToFetch;
    },
    compact() {
      return this.compactDisplay || this.mobileDisplay;
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
    canShowMore() {
      this.$emit('has-more', this.canShowMore);
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
    this.originalLimitToFetch = this.limitToFetch = this.limit;

    // To refresh menu when a new extension is ready to be used
    document.addEventListener('extension-profile-extension-action-updated', this.refreshExtensions);
    document.addEventListener('extension-space-member-extension-action-updated', this.refreshExtensions);
    document.addEventListener('user-extension-updated', this.refreshUserExtensions);
    document.addEventListener('people-list-refresh', this.searchPeopleNoFilters);

    this.$root.$on('reset-advanced-filter', this.searchPeople);
    this.$root.$on('advanced-filter', profileSettings => this.searchPeople(profileSettings));

    this.refreshExtensions();
    this.refreshUserExtensions();
  },
  beforeDestroy() {
    document.removeEventListener('extension-profile-extension-action-updated', this.refreshExtensions);
    document.removeEventListener('extension-space-member-extension-action-updated', this.refreshExtensions);
    document.removeEventListener('user-extension-updated', this.refreshUserExtensions);
    document.removeEventListener('people-list-refresh', this.searchPeopleNoFilters);
  },
  methods: {
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
      if (this.filter === 'connections') {
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
        }).then(data => ({
          size: data.size || 0,
          users: data?.spacesMemberships?.map?.(m => m.user),
        }));
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
        this.peopleCount = data && data.size && data.size || 0;
        this.hasPeople = this.hasPeople || this.peopleCount > 0;
        this.$emit('loaded', this.peopleCount);
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

