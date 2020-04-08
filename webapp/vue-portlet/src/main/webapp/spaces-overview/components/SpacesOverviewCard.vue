<template>
  <div
    :id="id"
    class="spacesOverviewCard clickable d-flex flex-column"
    @click="$emit('click')">
    <v-flex class="ma-auto" flat>
      <div class="spacesOverviewCount text-center pb-1">
        {{ count }}
      </div>
      <div class="spacesOverviewTitle text-center text-truncate pt-1">
        {{ title }}
      </div>
    </v-flex>
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
};
</script>

