<template>
  <v-card
    :outlined="!isMobile"
    class="peopleCardItem d-flex flex-column" 
    flat
    hover>
    <v-btn
      absolute
      small
      icon
      class="peopleInfoIcon ma-2"
      @click="$emit('flip')">
      <v-icon size="12">fa-info</v-icon>
    </v-btn>
    <div class="d-flex pt-3 pb-2">
      <p class="font-weight-bold ma-auto">
        {{ $t('peopleList.label.aboutMe') }}
      </p>
    </div>
    <v-card-text class="aboutMeText align-center py-0 flex-grow-1">
      <div
        v-if="user.aboutMe"
        v-sanitized-html="user.aboutMe"
        :title="user.aboutMe"
        class="text-truncate-3"></div>
    </v-card-text>

    <div class="d-flex pa-2 align-end flex-row">
      <div class="d-flex flex-column flex-grow-0 ms-2">
        <p class="font-weight-bold ma-auto mb-1">
          {{ user.connectionsCount }}
        </p>
        <p class="ma-auto text-sub-title">
          {{ $t('peopleList.title.connectionsCount') }}
        </p>
      </div>
      <div class="flex-grow-1"></div>
      <div class="d-flex flex-column flex-grow-0 me-2">
        <p class="font-weight-bold ma-auto mb-1">
          {{ user.spacesCount }}
        </p>
        <p class="ma-auto text-sub-title">
          {{ $t('peopleList.title.spacesCount') }}
        </p>
      </div>
    </div>
  </v-card>
</template>

<script>
export default {
  props: {
    user: {
      type: Object,
      default: null,
    },
  },
  computed: {
    isMobile() {
      return this.$vuetify.breakpoint.smAndDown;
    },
  }
};
</script>

