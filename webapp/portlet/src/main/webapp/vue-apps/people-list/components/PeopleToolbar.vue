<template>
  <v-toolbar id="peopleListToolbar" flat>
    <div class="showingPeopleText text-sub-title ms-3 d-none d-sm-flex">
      {{ $t('peopleList.label.peopleCount', {0: peopleCount}) }}
    </div>
    <span>
      <v-icon v-if="showMobileFilter && isMobile" left size="20" @click="mobileFilter">fa-arrow-left</v-icon>
    </span>
    <v-spacer class="d-none d-sm-flex" />
    <v-col>
      <v-text-field
          v-show="isMobile && showMobileFilter || !isMobile"
          v-model="keyword"
        :placeholder="$t('peopleList.label.filterPeople')"
        prepend-inner-icon="fa-filter"
        class="inputPeopleFilter pa-0 my-auto" />
    </v-col>
    <v-scale-transition>
      <select
        v-model="filter"
        class="selectPeopleFilter my-auto me-2 subtitle-1 ignore-vuetify-classes d-none d-sm-inline">
        <option
          v-for="peopleFilter in peopleFilters"
          :key="peopleFilter.value"
          :value="peopleFilter.value">
          {{ peopleFilter.text }}
        </option>
      </select>
    </v-scale-transition>
    <v-scale-transition >
      <v-btn
          v-show="isMobile && showMobileFilter || !isMobile"
          class="btn px-2 btn-primary"
          outlined
          @click="openPeopleAdvancedFilterDrawer()"
      >
        <i class="uiIcon uiIcon24x24 settingsIcon primary--text mr-1"></i>
        <span class="d-none font-weight-regular caption d-sm-inline mr-1">
            {{ $t('profile.label.search.openSearch') }}
            <span v-if="advancedFilterCountDisplay > 0">{{`(${advancedFilterCountDisplay})`}}</span>
          </span>
      </v-btn>
    </v-scale-transition>
    <v-icon
        size="24"
        class="text-sub-title pa-1 my-auto mt-2 ml-auto"
        v-show="isMobile && !showMobileFilter"
        @click="mobileFilter()">
        mdi-filter-outline
    </v-icon>
  </v-toolbar>
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
      type: String,
      default: null,
    },
  },
  data: () => ({
    filterToChange: null,
    bottomMenu: false,
    startSearchAfterInMilliseconds: 300,
    endTypingKeywordTimeout: 50,
    startTypingKeywordTimeout: 0,
    typing: false,
    advancedFilterCount: 0,
    showMobileFilter: false,
  }),
  created() {
    this.$root.$on('advanced-filter-count', (filterCount) => this.advancedFilterCount = filterCount );
    this.$root.$on('reset-advanced-filter-count', () => {
      this.advancedFilterCount = 0;
      this.advancedFilterCountDisplay();
    });
  },
  computed: {
    peopleFilters() {
      return [{
        text: this.$t('peopleList.label.filter.all'),
        value: 'all',
      },{
        text: this.$t('peopleList.label.filter.connections'),
        value: 'connections',
      }];
    },
    advancedFilterCountDisplay() {
      return this.advancedFilterCount || 0;
    },
    isMobile() {
      return this.$vuetify.breakpoint.width < 768;
    },
  },
  watch: {
    keyword() {
      this.startTypingKeywordTimeout = Date.now() + this.startSearchAfterInMilliseconds;
      if (!this.typing) {
        this.typing = true;
        this.waitForEndTyping();
      }
    },
    filter() {
      this.$emit('filter-changed', this.filter);
    },
  },
  methods: {
    openBottomMenu() {
      this.filterToChange = this.filter;
      this.bottomMenu = true;
    },
    changeFilterSelection() {
      this.bottomMenu = false;
      this.filter = this.filterToChange;
    },
    openPeopleAdvancedFilterDrawer() {
      this.$root.$emit('open-people-advanced-filter-drawer');
    },
    waitForEndTyping() {
      window.setTimeout(() => {
        if (Date.now() > this.startTypingKeywordTimeout) {
          this.typing = false;
          this.$emit('keyword-changed', this.keyword);
        } else {
          this.waitForEndTyping();
        }
      }, this.endTypingKeywordTimeout);
    },
    mobileFilter() {
      this.showMobileFilter = !this.showMobileFilter;
    }
  }
};
</script>

