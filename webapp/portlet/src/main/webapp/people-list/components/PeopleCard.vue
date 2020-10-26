<template>
  <v-flex :class="flipCard && 'peopleCardFlip peopleCardFlipped' || 'peopleCardFlip'">
    <div class="peopleCardFront">
      <people-card-front
        :user="user"
        :profile-action-extensions="profileActionExtensions"
        front
        @flip="flipCard = true"
        @refresh="$emit('refresh')" />
    </div>
    <div class="peopleCardBack">
      <people-card-reverse
        :user="user"
        @flip="flipCard = false" />
    </div>
  </v-flex>
</template>

<script>
export default {
  props: {
    user: {
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
