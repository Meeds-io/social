<template>
  <div
    :class="clickable"
    class="peopleOverviewCard d-flex flex-column"
    @click="count && $emit('click')">
    <div class="ma-auto">
      <div
        :class="skeleton && 'skeleton-background skeleton-border-radius skeleton-text'"
        class="peopleOverviewCount text-center pb-1">
        {{ skeleton && '&nbsp;&nbsp;' || count }}
      </div>
      <div
        :class="skeleton && 'skeleton-background skeleton-border-radius skeleton-text'"
        class="peopleOverviewTitle text-center text-truncate pt-1">
        {{ skeleton && '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' || title }}
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
    skeleton: {
      type: Boolean,
      default: () => true,
    },
  },
  computed: {
    clickable() {
      return this.count && this.count !== '-' && 'clickable';
    },
  },
};
</script>

