<template>
  <application-toolbar
    id="peopleListToolbar"
    :right-text-filter="filter !== 'disabled' && {
      minCharacters: 3,
      placeholder: $t('peopleList.label.filterPeople'),
      tooltip: $t('peopleList.label.filterPeople')
    }"
    :right-select-box="{
      selected: filter,
      items: peopleFilters,
    }"
    @filter-text-input-end-typing="keyword = $event"
    @filter-select-change="filterValue = $event"
    @toggle-select="updateFilter($event)">
    <template
      #left>
      <div class="d-flex">
        <v-btn
          v-if="isManager"
          class="btn pe-2 ps-0 me-4 inviteUserToSpaceButton"
          @click="$emit('invite-users')">
          <v-icon
            size="18"
            class="primary--text ms-2 me-1">
            fas fa-user-plus
          </v-icon>
          <span class="d-none d-sm-inline">
            {{ $t('peopleList.button.inviteUsers') }}
          </span>
        </v-btn>
        <div class="showingPeopleText text-subtitle d-none my-auto d-sm-flex">
          {{ $t('peopleList.label.peopleCount', {0: peopleCount}) }}
        </div>
      </div>
    </template>
  </application-toolbar>
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
    filterValue: null
  }),
  computed: {
    isMobile() {
      return this.$vuetify.breakpoint.smAndDown;
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
          text: this.$t('peopleList.label.filter.disabled'),
          value: 'disabled',
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
      this.filterValue = this.filter;
    },
    filterValue() {
      this.updateFilter();
    }
  },
  created() {
    this.filterValue = this.filter;
  },
  methods: {
    updateFilter() {
      this.$emit('filter-changed', this.filterValue);
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

