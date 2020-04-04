<template>
  <v-col
    :class="flipCard && 'spaceCardFlip spaceCardFlipped' || 'spaceCardFlip'"
    cols="12"
    md="3">
    <div class="spaceCardFront">
      <exo-space-card-front
        :space="space"
        front
        @flip="flipCard = true"
        @refresh="$emit('refresh')" />
    </div>
    <div class="spaceCardBack">
      <exo-space-card-reverse
        :space="space"
        @flip="flipCard = false" />
    </div>
  </v-col>
</template>

<script>
export default {
  props: {
    space: {
      type: Object,
      default: () => ({}),
    },
  },
  data: () => ({
    flipCard: false,
  }),
};
</script>
