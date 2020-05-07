<template>
  <v-col
    :class="flipCard && 'peopleCardFlip peopleCardFlipped' || 'peopleCardFlip'"
    cols="12"
    md="6"
    lg="4"
    xl="3">
    <div class="peopleCardFront">
      <space-member-card-front
        :user="user"
        :profile-action-extensions="profileActionExtensions"
        :skeleton="skeleton"
        front
        @flip="flipCard = true"
        @refresh="$emit('refresh')" />
    </div>
    <div v-if="!skeleton" class="peopleCardBack">
      <space-member-card-reverse
        :user="user"
        @flip="flipCard = false" />
    </div>
  </v-col>
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
    skeleton: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    flipCard: false,
  }),
};
</script>
