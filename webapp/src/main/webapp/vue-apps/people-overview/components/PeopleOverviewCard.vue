<template>
  <div
    class="peopleOverviewCard d-flex flex-column"
    :class="clickable"
    @click="count && $emit('click')">
    <div class="ma-auto">
      <div
        class="peopleOverviewCount d-flex align-center justify-center">
        <v-icon
          class="tertiary--text me-2"
          size="22">
          {{ icon }}
        </v-icon>
        <span class="text-h5 font-weight-bold text-color">{{ count }}</span>
      </div>
      <div class="peopleOverviewTitle text-center text-truncate pt-3 text-body d-none d-md-block">
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
        default: () => '',
      },
    },
    computed: {
      clickable () {
        return this.count && this.count !== '-' && 'clickable';
      },
    },
  };
</script>

