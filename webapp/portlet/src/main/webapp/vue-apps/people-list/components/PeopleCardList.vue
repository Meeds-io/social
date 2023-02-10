<template>
  <v-card flat>
    <v-progress-linear
      v-if="loadingPeople"
      indeterminate
      height="2"
      color="primary" />
    <v-card-text id="peopleListBody" class="pb-0">
      <v-item-group>
        <v-container class="pa-0">
          <v-row v-if="filteredPeople && filteredPeople.length" class="ma-0 border-box-sizing">
            <v-col
              v-for="user in filteredPeople"
              :key="user.id"
              cols="12"
              md="6"
              lg="4"
              xl="3"
              class="pa-0">
              <people-card
                :user="user"
                :profile-action-extensions="profileActionExtensions"
                @refresh="searchPeople" />
            </v-col>
          </v-row>
          <div v-else-if="!loadingPeople" class="d-flex text-center noPeopleYetBlock">
            <div class="ma-auto noPeopleYet">
              <p class="noPeopleYetIcons">
                <v-icon>fa-users</v-icon>
              </p>
              <template v-if="filter === 'connections'">
                <p class="title font-weight-bold">
                  {{ $t('peopleList.label.noConnection') }}
                </p>
              </template>
              <template v-else-if="hasPeople">
                <p class="title font-weight-bold">
                  {{ $t('peopleList.label.noResults') }}
                </p>
              </template>
              <template v-else>
                <p class="title font-weight-bold">
                  {{ $t('peopleList.label.noPeopleYet') }}
                </p>
              </template>
            </div>
          </div>
        </v-container>
      </v-item-group>
    </v-card-text>
    <v-card-actions id="peopleListFooter" class="pt-0 px-5 border-box-sizing">
      <v-btn
        v-if="canShowMore"
        :loading="loadingPeople"
        :disabled="loadingPeople"
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
    loadingPeople: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    profileExtensions: [],
    spaceMemberExtensions: [],
    fieldsToRetrieve: 'all,spacesCount,relationshipStatus,connectionsCount,binding',
    userType: 'internal',
    initialized: false,
    hasPeople: false,
    offset: 0,
    pageSize: 20,
    limit: 20,
    users: [],
    limitToFetch: 0,
    originalLimitToFetch: 0,
  }),
  computed: {
    profileActionExtensions() {
      const profileActionExtensions = [...this.profileExtensions, ...this.spaceMemberExtensions];
      profileActionExtensions.sort((a, b) => (a.sort || 100) - (b.sort || 100));
      return profileActionExtensions;
    },
    canShowMore() {
      return this.loadingPeople || this.users.length >= this.limitToFetch;
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
      this.waitForInitializing();

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
    document.addEventListener('space-member-extension-updated', this.refreshExtensions);
    document.addEventListener('profile-extension-updated', this.refreshExtensions);

    // To broadcast event about current page supporting profile extensions
    document.dispatchEvent(new CustomEvent('profile-extension-init'));
    this.$root.$on('reset-advanced-filter', this.searchPeople);

    this.$root.$on('advanced-filter', profileSettings => this.getUsersByadvancedfilter(profileSettings));

    this.refreshExtensions();
  },
  methods: {
    refreshExtensions() {
      this.profileExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
      this.spaceMemberExtensions = this.spaceId && extensionRegistry.loadExtensions('space-member-extension', 'action') || [];
    },
    searchPeople() {
      this.loadingPeople = true;
      // Using 'limitToFetch + 1' to retrieve current user and then delete it from result
      // to finally let only 'limitToFetch' users
      let searchUsersFunction;
      if (this.filter === 'connections') {
        searchUsersFunction = this.$userService.getConnections(this.keyword, this.offset, this.limitToFetch + 1, this.fieldsToRetrieve);
      } else if (this.filter === 'member'
          || this.filter === 'manager'
          || this.filter === 'invited'
          || this.filter === 'pending'
          || this.filter === 'redactor'
          || this.filter === 'publisher') {
        searchUsersFunction = this.$spaceService.getSpaceMembers(this.keyword, this.offset, this.limitToFetch + 1, this.fieldsToRetrieve, this.filter, this.spaceId);
      } else {
        searchUsersFunction = this.$userService.getUsers(this.keyword, this.offset, this.limitToFetch + 1, this.fieldsToRetrieve);
      }
      return searchUsersFunction.then(data => {
        let users = data && data.users || [];
        if (this.filter === 'all') {
          users = users.filter(user => user && user.username !== eXo.env.portal.userName);
        }
        users = users.slice(0, this.limitToFetch);
        this.users = users;
        this.peopleCount = data && data.size && data.size || 0;
        if (this.peopleCount > 0 && this.filter === 'all' && !this.keyword) {
          this.peopleCount = this.peopleCount - 1;
        }
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
    getUsersByadvancedfilter(profileSettings) {

      this.loadingPeople = true;
      // Using 'limitToFetch + 1' to retrieve current user and then delete it from result
      // to finally let only 'limitToFetch' users
      let filterUsersFunction;
      if (this.filter) {
        filterUsersFunction = this.$userService.getUsersByAdvancedFilter(profileSettings, this.offset, this.limitToFetch + 1, this.fieldsToRetrieve,this.filter);
      }
      return filterUsersFunction.then(data => {
        let users = data && data.users || [];
        if (this.filter === 'all') {
          users = users.filter(user => user && user.username !== eXo.env.portal.userName);
        }
        users = users.slice(0, this.limitToFetch);
        this.users = users;
        this.peopleCount = data && data.size && data.size || 0;
        if (this.peopleCount > 0 && this.filter === 'all' && !this.keyword) {
          this.peopleCount = this.peopleCount - 1;
        }
        this.hasPeople = this.hasPeople || this.peopleCount > 0;
        this.$emit('loaded', this.peopleCount);
        return this.$nextTick();
      })
        .then(() => {
          if ( this.filteredPeople.length < this.originalLimitToFetch && this.users.length >= this.limitToFetch) {
            this.limitToFetch += this.pageSize;
          }
        })
        .finally(() => {
          if (!this.initialized) {
            this.$root.$applicationLoaded();
          }
          this.loadingPeople = false;
          this.initialized = true;
        });

    },
    waitForInitializing() {
      window.setTimeout(() => {
        if (this.initialized) {
          this.searchPeople();
        } else {
          this.waitForInitializing();
        }
      }, 50);
    }
  }
};
</script>

