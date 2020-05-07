<template>
  <v-card flat>
    <v-card-text id="peopleListBody" class="pb-0">
      <v-item-group>
        <v-container class="pa-0">
          <v-row v-if="skeleton" class="ma-0 border-box-sizing">
            <people-card
              v-for="i in pageSize"
              :key="i"
              :user="{}"
              :skeleton="skeleton"
              :profile-action-extensions="profileActionExtensions"
              @refresh="searchPeople" />
          </v-row>
          <v-row v-else-if="filteredPeople && filteredPeople.length" class="ma-0 border-box-sizing">
            <people-card
              v-for="user in filteredPeople"
              :key="user.id"
              :user="user"
              :profile-action-extensions="profileActionExtensions"
              @refresh="searchPeople" />
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
        v-if="skeleton || canShowMore"
        :loading="loadingPeople"
        :disabled="skeleton || loadingPeople"
        :class="skeleton && 'skeleton-background skeleton-text'"
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
    peopleCount: {
      type: Number,
      default: 0,
    },
    loadingPeople: {
      type: Boolean,
      default: false,
    },
    skeleton: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    profileActionExtensions: [],
    startSearchAfterInMilliseconds: 600,
    endTypingKeywordTimeout: 50,
    startTypingKeywordTimeout: 0,
    fieldsToRetrieve: 'all,spacesCount,relationshipStatus,connectionsCount',
    hasPeople: false,
    offset: 0,
    pageSize: 20,
    limit: 20,
    users: [],
    limitToFetch: 0,
    originalLimitToFetch: 0,
  }),
  computed:{
    searchUsersFunction() {
      return this.filter === 'connections' ? this.$userService.getConnections : this.$userService.getUsers;
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
      this.startTypingKeywordTimeout = Date.now();
      if (!this.loadingPeople) {
        this.loadingPeople = true;
        this.waitForEndTyping();
      }
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
    document.addEventListener('profile-extension-updated', this.refreshExtensions);

    // To broadcast event about current page supporting profile extensions
    document.dispatchEvent(new CustomEvent('profile-extension-init'));

    this.refreshExtensions();
  },
  methods: {
    refreshExtensions() {
      this.profileActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
    },
    searchPeople() {
      this.loadingPeople = true;
      this.loadingPeople = true;
      // Using 'limitToFetch + 1' to retrieve current user and then delete it from result
      // to finally let only 'limitToFetch' users 
      return this.searchUsersFunction(this.keyword, this.offset, this.limitToFetch + 1, this.fieldsToRetrieve)
        .then(data => {
          let users = data && data.users || [];
          users = users.filter(user => user && user.username !== eXo.env.portal.userName).slice(0, this.limitToFetch);
          this.users = users;
          this.peopleCount = data && data.size && data.size - 1 || 0;
          this.hasPeople = this.hasPeople || this.peopleCount > 0;
          this.$emit('loaded', this.peopleCount);
          return this.$nextTick();
        })
        .then(() => {
          if (this.keyword && this.filteredPeople.length < this.originalLimitToFetch && this.users.length >= this.limitToFetch) {
            this.limitToFetch += this.pageSize;
          }
        })
        .finally(() => this.loadingPeople = false);
    },
    resetSearch() {
      if (this.limitToFetch !== this.originalLimitToFetch) {
        this.limitToFetch = this.originalLimitToFetch;
      }
    },
    loadNextPage() {
      this.originalLimitToFetch = this.limitToFetch += this.pageSize;
    },
    waitForEndTyping() {
      window.setTimeout(() => {
        if (Date.now() - this.startTypingKeywordTimeout > this.startSearchAfterInMilliseconds) {
          this.searchPeople();
        } else {
          this.waitForEndTyping();
        }
      }, this.endTypingKeywordTimeout);
    },
  }
};
</script>

