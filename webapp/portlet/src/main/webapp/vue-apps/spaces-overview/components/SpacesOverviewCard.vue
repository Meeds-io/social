<template>
  <div
    :id="id"
    :class="clickable"
    class="spacesOverviewCard d-flex flex-column"
    @click="count && $emit('click')">
    <div class="ma-auto d-flex flex-column">
      <div class="spacesOverviewCount d-flex align-center justify-center">
        <v-icon size="22" class="tertiary--text me-2">{{ icon }}</v-icon>
        <span class="text-h5 font-weight-bold text-color"> {{ count }} </span>
      </div>
      <div class="spacesOverviewTitle text-center text-truncate subtitle-1 pt-3 text-color d-none d-md-block">
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
      return this.count && this.count > 0 && 'clickable';
    },
  },
};
</script>

