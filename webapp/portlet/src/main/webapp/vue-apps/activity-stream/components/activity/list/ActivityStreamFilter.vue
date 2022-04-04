<template>
  <div id="activityFilter">
    <select
      v-if="displayStreamFilter"
      id="filterStream"
      v-model="selectedFilter"
      name="status"
      class="activityFilter ignore-vuetify-classes">
      <option value="all">{{ $t('activity.stream.filter.all') }}</option>
      <option value="myPosted"> {{ $t('activity.stream.filter.myposted') }} </option>
    </select>
  </div>
</template>

<script>
export default {
  props: {
    selectedFilter: {
      type: String,
      default: ''
    }
  },
  computed: {
    displayStreamFilter() {
      return !eXo.env.portal.spaceId;
    }
  },
  watch: {
    selectedFilter(newValue, oldValue) {
      if (oldValue !== '') {
        localStorage.setItem('ActivityFilter',newValue);
        this.$root.$emit('filter-activities',newValue);
      }
    }
  }
};
</script>
