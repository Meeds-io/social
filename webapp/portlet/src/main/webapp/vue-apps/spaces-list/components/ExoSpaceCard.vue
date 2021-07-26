<template>
  <v-flex :class="flipCard && 'spaceCardFlip spaceCardFlipped' || 'spaceCardFlip'">
    <div class="spaceCardFront">
      <exo-space-card-front
        :space="space"
        :profile-action-extensions="profileActionExtensions"
        front
        @flip="flipCard = true"
        @refresh="$emit('refresh')" />
    </div>
    <div class="spaceCardBack">
      <exo-space-card-reverse
        :space="space"
        @flip="flipCard = false" />
    </div>
  </v-flex>
</template>

<script>
export default {
  props: {
    space: {
      type: Object,
      default: () => ({}),
    },
    profileActionExtensions: {
      type: Array,
      default: () => [],
    },
  },
  data: () => ({
    flipCard: false,
  }),
};
</script>
