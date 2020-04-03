<template>
  <v-col
    :class="flipCard && 'spaceCardFlip spaceCardFlipped' || 'spaceCardFlip'"
    cols="12"
    md="3">
    <div class="spaceCardFront">
      <exo-spaces-card-item
        :space="space"
        front
        @flip="flipCard = true"
        @refresh="$emit('refresh')" />
    </div>
    <div class="spaceCardBack">
      <exo-spaces-card-item
        :space="space"
        @flip="flipCard = false"
        @refresh="$emit('refresh')" />
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
