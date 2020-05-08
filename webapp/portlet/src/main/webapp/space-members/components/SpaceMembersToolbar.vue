<template>
  <v-toolbar id="peopleListToolbar" flat>
    <v-toolbar-title v-if="skeleton || isManager">
      <v-btn
        :disabled="skeleton"
        :class="skeleton && 'skeleton-text skeleton-background'"
        class="btn pr-2 pl-0 inviteUserToSpaceButton"
        @click="$emit('invite-users')">
        <span v-if="skeleton" class="mx-2">&nbsp;</span>
        <i v-else class="uiIconInviteUser ml-2 mr-1" />
        <span class="d-none d-sm-inline">
          {{ skeleton && '&nbsp;' || $t('peopleList.button.inviteUsers') }}
        </span>
      </v-btn>
    </v-toolbar-title>
    <div
      :class="skeleton && 'skeleton-text skeleton-background skeleton-border-radius'"
      class="showingPeopleText text-sub-title ml-3 d-none d-sm-flex">
      {{ skeleton && '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' || $t('peopleList.label.peopleCount', {0: peopleCount}) }}
    </div>
    <v-spacer class="d-none d-sm-flex" />
    <v-scale-transition>
      <v-text-field
        v-model="keyword"
        :disabled="skeleton"
        :class="skeleton && 'skeleton-text'"
        :placeholder="!skeleton && $t('peopleList.label.filterPeople') || '&nbsp;'"
        prepend-inner-icon="fa-filter"
        class="inputPeopleFilter pa-0 mr-3 my-auto"></v-text-field>
    </v-scale-transition>
    <v-scale-transition>
      <select
        v-model="filter"
        :disabled="skeleton"
        :class="skeleton && 'skeleton-background skeleton-text'"
        class="selectPeopleFilter my-auto mr-2 subtitle-1 ignore-vuetify-classes d-none d-sm-inline">
        <option
          v-for="peopleFilter in peopleFilters"
          :key="peopleFilter.value"
          :value="peopleFilter.value">
          {{ peopleFilter.text }}
        </option>
      </select>
    </v-scale-transition>
    <v-icon
      :class="skeleton && 'skeleton-text'"
      class="d-sm-none"
      @click="openBottomMenu">
      fa-filter
    </v-icon>
    <v-bottom-sheet v-model="bottomMenu" class="pa-0">
      <v-sheet :height="bottomNavigationHeight" class="text-center">
        <v-toolbar color="primary" dark class="border-box-sizing">
          <v-btn text @click="bottomMenu = false">
            {{ $t('peopleList.label.cancel') }}
          </v-btn>
          <v-spacer></v-spacer>
          <v-toolbar-title>
            <v-icon>fa-filter</v-icon>
            {{ $t('peopleList.label.filter') }}
          </v-toolbar-title>
          <v-spacer></v-spacer>
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
              <span v-else class="mr-6"></span>
              <v-spacer />
              <div>
                {{ peopleFilter.text }}
              </div>
              <v-spacer />
              <span class="mr-6"></span>
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
    skeleton: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    filterToChange: null,
    bottomMenu: false,
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
          text: this.$t('peopleList.label.filter.manager'),
          value: 'manager',
        }];
      }
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
  }
};
</script>

