<template>
  <v-toolbar id="peopleListToolbar" flat>
    <div>
      <div class="showingPeopleText text-sub-title ms-3 d-none d-sm-flex">
        {{ $t('peopleList.label.peopleCount', {0: peopleCount}) }}
      </div>
      <span v-show="mobileFilter" class="hidden-sm-and-up">
        <v-icon
          left
          size="20"
          @click="showMobileFilter">fa-arrow-left</v-icon>
      </span>
    </div>
    <div class="d-flex align-center flex-grow-1">
      <v-col>
        <v-text-field
          v-show="isMobile && mobileFilter || !isMobile"
          v-model="keyword"
          :placeholder="$t('peopleList.label.filterPeople')"
          prepend-inner-icon="fa-filter"
          class="inputPeopleFilter pa-0 mb-n2 my-auto ms-auto" />
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
      <v-scale-transition>
        <v-btn
          v-show="isMobile && mobileFilter || !isMobile"
          class="btn px-2 btn-primary"
          :min-width="iconWidth"
          outlined
          @click="openPeopleAdvancedFilterDrawer()">
          <v-icon small class="primary--text me-lg-1">fa-sliders-h</v-icon>
          <span class="d-none font-weight-regular caption d-lg-inline me-1">
            {{ $t('profile.label.search.openSearch') }} </span>
          <span class="font-weight-regular caption ms-1"> {{ advancedFilterCountDisplay }} </span>
        </v-btn>
      </v-scale-transition>
      <v-icon
        size="24"
        class="text-sub-title pa-1 my-auto mt-2 ms-auto"
        v-show="isMobile && !mobileFilter"
        @click="showMobileFilter()">
        mdi-filter-outline
      </v-icon>
    </div>
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
    mobileFilter: false,
    iconWidth: '24px'
  }),
  created() {
    this.$root.$on('advanced-filter-count', (filterCount) => this.advancedFilterCount = filterCount);
    this.$root.$on('reset-advanced-filter-count', () => {
      this.advancedFilterCount = 0;
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
      return this.advancedFilterCount > 0 ? `(${this.advancedFilterCount})`:'';
    },
    isMobile() {
      return this.$vuetify.breakpoint.width < 768;
    }
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
    showMobileFilter() {
      this.mobileFilter = !this.mobileFilter;
    }
  }
};
</script>

