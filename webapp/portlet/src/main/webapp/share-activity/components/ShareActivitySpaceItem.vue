<template>
  <v-chip
    close
    class="identitySuggesterItem me-4 mt-4"
    @click:close="$emit('remove-space', space)">
    <v-avatar left>
      <v-img :src="avatarUrl" />
    </v-avatar>
    <span class="text-truncate">
      {{ displayName }}
    </span>
  </v-chip>
</template>

<script>
export default {
  props: {
    space: {
      type: Object,
      default: () => ({}),
    },
  },
  computed: {
    avatarUrl() {
      const profile = this.space.identity && this.space.identity.profile;
      return profile && profile.avatar;
    },
    displayName() {
      const profile = this.space.identity && this.space.identity.profile;
      return profile && profile.fullname;
    },
  },
};
</script>