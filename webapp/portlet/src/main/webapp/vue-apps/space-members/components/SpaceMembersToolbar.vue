<template>
  <v-toolbar id="peopleListToolbar" flat>
    <v-toolbar-title v-if="isManager">
      <v-btn
        class="btn pe-2 ps-0 me-4 inviteUserToSpaceButton"
        @click="$emit('invite-users')">
        <i class="uiIconInviteUser ms-2 me-1"></i>
        <span class="d-none d-sm-inline">
          {{ $t('peopleList.button.inviteUsers') }}
        </span>
      </v-btn>
    </v-toolbar-title>
    <div
      class="showingPeopleText text-sub-title d-none d-sm-flex">
      {{ $t('peopleList.label.peopleCount', {0: peopleCount}) }}
    </div>
    <v-spacer class="d-none d-sm-flex" />
    <v-scale-transition>
      <v-text-field
        v-model="keyword"
        :placeholder="$t('peopleList.label.filterPeople')"
        prepend-inner-icon="fa-filter"
        class="inputPeopleFilter pa-0 mt-0 mb-n2 me-3 my-auto" />
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
    <v-bottom-sheet v-model="bottomMenu" class="pa-0">
      <v-sheet :height="bottomNavigationHeight" class="text-center">
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
    isManager: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    filterToChange: null,
    bottomMenu: false,
    startSearchAfterInMilliseconds: 300,
    endTypingKeywordTimeout: 50,
    startTypingKeywordTimeout: 0,
    typing: false,
  }),
  computed: {
    bottomNavigationHeight() {
      return this.isManager && '255px' || '169px';
    },
    peopleFilters() {
      if (this.isManager) {
        return [{
          text: this.$t('peopleList.label.filter.member'),
          value: 'member',
        },{
          text: this.$t('peopleList.label.filter.manager'),
          value: 'manager',
        },{
          text: this.$t('peopleList.label.filter.redactor'),
          value: 'redactor',
        },{
          text: this.$t('peopleList.label.filter.publisher'),
          value: 'publisher',
        },{
          text: this.$t('peopleList.label.filter.invited'),
          value: 'invited',
        },{
          text: this.$t('peopleList.label.filter.pending'),
          value: 'pending',
        }];
      } else {
        return [{
          text: this.$t('peopleList.label.filter.member'),
          value: 'member',
        },{
          text: this.$t('peopleList.label.filter.redactor'),
          value: 'redactor',
        },{
          text: this.$t('peopleList.label.filter.publisher'),
          value: 'publisher',
        },{
          text: this.$t('peopleList.label.filter.manager'),
          value: 'manager',
        }];
      }
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
  }
};
</script>

