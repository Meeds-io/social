<template>
  <v-toolbar id="peopleListToolbar" flat>
    <div class="showingPeopleText text-sub-title ms-3 d-none d-sm-flex">
      {{ $t('peopleList.label.peopleCount', {0: peopleCount}) }}
    </div>
    <v-spacer class="d-none d-sm-flex" />
    <v-scale-transition>
      <v-text-field
        v-model="keyword"
        :placeholder="$t('peopleList.label.filterPeople')"
        prepend-inner-icon="fa-filter"
        class="inputPeopleFilter pa-0 me-3 my-auto" />
    </v-scale-transition>
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
    <v-icon
      class="d-sm-none"
      @click="openBottomMenu">
      fa-filter
    </v-icon>
    <v-scale-transition v-if="peopleAdvancedFilterFeatureEnabled">
      <v-btn
          class="btn px-2 btn-primary"
          outlined
          @click="openPeopleAdvancedFilterDrawer()"
      >
        <i class="uiIcon uiIcon24x24 settingsIcon primary--text mr-1"></i>
        <span class="d-none font-weight-regular caption d-sm-inline mr-1">
            {{ $t('profile.label.search.openSearch') }}
          </span>
      </v-btn>
    </v-scale-transition>
    <v-bottom-sheet v-model="bottomMenu" class="pa-0">
      <v-sheet class="text-center" height="169px">
        <v-toolbar
          color="primary"
          dark
          class="border-box-sizing">
          <v-btn text @click="bottomMenu = false">
            {{ $t('peopleList.label.cancel') }}
          </v-btn>
          <v-spacer />
          <v-toolbar-title>
            <v-icon>fa-filter</v-icon>
            {{ $t('peopleList.label.filter') }}
          </v-toolbar-title>
          <v-spacer />
          <v-btn text @click="changeFilterSelection">
            {{ $t('peopleList.label.confirm') }}
          </v-btn>
        </v-toolbar>
        <v-list>
          <v-list-item
            v-for="peopleFilter in peopleFilters"
            :key="peopleFilter"
            @click="filterToChange = peopleFilter.value">
            <v-list-item-title class="align-center d-flex">
              <v-icon v-if="filterToChange === peopleFilter.value">fa-check</v-icon>
              <span v-else class="me-6"></span>
              <v-spacer />
              <div>
                {{ peopleFilter.text }}
              </div>
              <v-spacer />
              <span class="me-6"></span>
            </v-list-item-title>
          </v-list-item>
        </v-list>
      </v-sheet>
    </v-bottom-sheet>
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
    peopleAdvancedFilterFeatureEnabled: false,
  }),
  created() {
    this.$featureService.isFeatureEnabled('peopleAdvancedFilter')
      .then(enabled => this.peopleAdvancedFilterFeatureEnabled = enabled);
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
  },
  watch: {
    keyword() {
      this.$emit('keyword-changed', this.keyword);
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
  }
};
</script>

