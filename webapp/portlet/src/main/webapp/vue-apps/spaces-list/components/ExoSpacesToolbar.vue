<template>
  <v-toolbar id="spacesListToolbar" flat>
    <v-toolbar-title 
      v-if="canCreateSpace" 
      class="flex-shrink-0">
      <v-icon
        v-if="menuHeaderChanged"
        @click="changeHeaderMenu">
        fas fa-arrow-left
      </v-icon>
      <v-btn
        v-else
        id="addNewSpaceButton"
        class="btn btn-primary mx-2 mx-md-0 px-md-2 px-0 addNewSpaceButton"
        :small="isMobile"
        :large="!isMobile"
        @click="$root.$emit('addNewSpace')">
        <v-icon dark>mdi-plus</v-icon>
        <span class="d-none d-lg-inline">
          {{ $t('spacesList.label.addNewSpace') }}
        </span>
      </v-btn>
    </v-toolbar-title>
    <div
      class="text-sub-title ms-3 d-none d-sm-flex">
      {{ $t('spacesList.label.spacesSize', {0: spacesSize}) }}
    </div>
    <v-spacer v-if="!isMobile" />
    <div>
      <v-text-field
        v-if="!isMobile"
        v-model="keyword"
        :placeholder="$t('spacesList.label.filterSpaces')"
        prepend-inner-icon="fa-filter"
        class="inputSpacesFilter pa-0 me-3 mb-n2 my-auto" />
      <v-text-field
        v-else-if="isMobile && menuHeaderChanged"
        v-model="keyword"
        :placeholder="$t('spacesList.label.filterSpacesByName')"
        prepend-inner-icon="fa-filter"
        class="inputSpacesFilter pa-0 ms-3 mb-n2 my-auto"
        clearable />
    </div>
    <v-spacer v-if="isMobile" />
    <v-scale-transition>
      <select
        v-model="filter"
        class="selectSpacesFilter my-auto me-2 subtitle-1 ignore-vuetify-classes d-none d-sm-inline">
        <option
          v-for="spaceFilter in spaceFilters"
          :key="spaceFilter.value"
          :value="spaceFilter.value">
          {{ spaceFilter.text }}
        </option>
      </select>
    </v-scale-transition>
    <div class="d-sm-none">
      <v-icon
        v-if="!menuHeaderChanged"
        @click="changeHeaderMenu">
        fa-filter
      </v-icon>
      <v-icon
        v-else
        @click="openBottomMenu">
        fa-sliders-h
      </v-icon>
    </div>
    <v-bottom-sheet v-model="bottomMenu" class="pa-0">
      <v-sheet class="text-center" height="210px">
        <v-toolbar
          color="primary"
          dark
          class="border-box-sizing">
          <v-btn text @click="bottomMenu = false">
            {{ $t('spacesList.button.cancel') }}
          </v-btn>
          <v-spacer />
          <v-toolbar-title>
            <v-icon>fa-filter</v-icon>
            {{ $t('spacesList.label.filter') }}
          </v-toolbar-title>
          <v-spacer />
          <v-btn text @click="changeFilterSelection">
            {{ $t('spacesList.button.confirm') }}
          </v-btn>
        </v-toolbar>
        <v-list>
          <v-list-item
            v-for="spaceFilter in spaceFilters"
            :key="spaceFilter"
            @click="filterToChange = spaceFilter.value">
            <v-list-item-title class="align-center d-flex">
              <v-icon v-if="filterToChange === spaceFilter.value">fa-check</v-icon>
              <span v-else class="me-6"></span>
              <v-spacer />
              <div>
                {{ spaceFilter.text }}
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
    canCreateSpace: {
      type: Boolean,
      default: false,
    },
    keyword: {
      type: String,
      default: null,
    },
    filter: {
      type: String,
      default: null,
    },
    spacesSize: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    filterToChange: null,
    bottomMenu: false,
    menuHeaderChanged: false,
  }),
  computed: {
    spaceFilters() {
      return [{
        text: this.$t('spacesList.filter.all'),
        value: 'all',
      },{
        text: this.$t('spacesList.filter.userSpaces'),
        value: 'member',
      },{
        text: this.$t('spacesList.filter.favoriteSpaces'),
        value: 'favorite',
      }];
    },
    isMobile() {
      return this.$vuetify.breakpoint.smAndDown;
    }
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
      this.changeHeaderMenu();
    },
    changeHeaderMenu() {
      this.menuHeaderChanged = !this.menuHeaderChanged;
    }
  }
};
</script>

