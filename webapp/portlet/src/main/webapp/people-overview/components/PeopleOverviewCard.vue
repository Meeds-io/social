<template>
  <div
    :class="clickable"
    class="peopleOverviewCard d-flex flex-column"
    @click="count && $emit('click')">
    <div class="ma-auto">
      <div
        class="peopleOverviewCount text-center pb-1">
        {{ count }}
      </div>
      <div class="peopleOverviewTitle text-center text-truncate pt-1">
        {{ title }}
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    id: {
      type: String,
      default: () => null,
    },
    title: {
      type: String,
      default: () => null,
    },
    count: {
      type: Number,
      default: () => 0,
    },
  },
  computed: {
    clickable() {
      return this.count && this.count !== '-' && 'clickable';
    },
  },
};
</script>

