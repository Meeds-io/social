<template>
  <v-flex class="d-flex content-box-sizing justify-end">
    <select
      v-model="streamFilter"
      class="width-auto my-auto ignore-vuetify-classes"
      @change="applyFilter">
      <option value="all">{{ $t('activity.filter.all') }}</option>
      <option value="user_stream">{{ $t('activity.filter.myActivities')}}</option>
      <option value="manage_spaces_stream">{{ $t('activity.filter.manageSpaces')}}</option>
      <option value="favorite_spaces_stream">{{ $t('activity.filter.favoriteSpaces')}}</option>
    </select>
  </v-flex>
</template>
<script>
export default {
  props: {
    streamFilter: {
      type: String,
      default: () => 'all',
    },
  },
  methods: {
    applyFilter() {
      document.dispatchEvent(new CustomEvent('filter-applied', {detail: this.streamFilter}));
    },
  },
};
</script>