<template>
  <application-toolbar
    id="peopleListToolbar"
    :right-text-filter="filter !== 'disabled' && {
      minCharacters: 3,
      placeholder: $t('peopleList.label.filterPeople'),
      tooltip: $t('peopleList.label.filterPeople')
    }"
    :right-select-box="!hideFilter && {
      selected: filter,
      items: peopleFilters,
    }"
    compact
    no-text-truncate
    @filter-text-input-end-typing="$emit('keyword-changed', $event)"
    @filter-select-change="$emit('filter-changed', $event)"
    @toggle-select="$emit('filter-changed', $event)"
    @loading="$emit('loading', $event)">
    <template
      #left>
      <div class="d-flex">
        <space-invite-buttons-group
          v-if="isManager"
          class="px-2 me-4" />
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
    hideFilter: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
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
};
</script>

