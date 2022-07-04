<template>
  <v-flex class="d-flex content-box-sizing justify-end">
    <select
      v-model="streamFilter"
      class="width-auto my-auto ignore-vuetify-classes"
      @change="applyFilter">
      <option value="all">{{ $t('activity.filter.all') }}</option>
      <option value="myActivities">{{ $t('activity.filter.myActivities') }}</option>
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
      this.$emit('filterChanged', this.streamFilter);
    },
  },
};
</script>