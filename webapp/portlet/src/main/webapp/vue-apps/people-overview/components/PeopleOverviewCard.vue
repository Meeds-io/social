<template>
  <div
    :class="clickable"
    class="peopleOverviewCard d-flex flex-column"
    @click="count && $emit('click')">
    <div class="ma-auto">
      <div
        class="peopleOverviewCount d-flex align-center justify-center">
        <v-icon size="22" class="tertiary--text me-2">{{ icon }}</v-icon>
        <span class="text-h5 font-weight-bold text-color">{{ count }}</span>
      </div>
      <div class="peopleOverviewTitle text-center text-truncate subtitle-1 pt-3 text-color d-none d-md-block">
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
    icon: {
      type: String,
      default: () => ''
    }
  },
  computed: {
    clickable() {
      return this.count && this.count !== '-' && 'clickable';
    },
  },
};
</script>

