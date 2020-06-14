<template>
  <v-card class="mx-1">
    <v-card-text>
      <exo-user-avatar
        :username="poster.username"
        :fullname="poster.fullname"
        :title="poster.fullname">
      </exo-user-avatar>
    </v-card-text>
    <v-divider />
    <v-card-text v-if="spaceDisplayName" class="pb-0">
      <p class="font-weight-bold text-truncate pb-0 mb-0">
        <exo-space-avatar :space="streamOwner" size="21" />
      </p>
    </v-card-text>
    <v-card-text>
      <p v-if="isComment" class="font-weight-bold">
        {{ $t('Search.activity.inComment') }}
      </p>
      <div
        v-for="(excerpt, index) in excerpts"
        :key="index"
        class="text-color"
        v-html="excerpt">
      </div>
    </v-card-text>
    <v-card-actions>
      <v-btn text color="primary">
        Go to activity
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script>
export default {
  props: {
    term: {
      type: String,
      default: null,
    },
    result: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    profileActionExtensions: [],
    displayDrawers: false,
  }),
  computed: {
    isComment() {
      return this.result && this.result.comment;
    },
    activity() {
      return this.isComment && this.result.comment || this.result;
    },
    poster() {
      console.log('poster.profile', this.activity && this.activity.poster.profile);
      return this.activity && this.activity.poster.profile;
    },
    streamOwner() {
      return this.activity && this.activity.streamOwner.space || this.activity.streamOwner.profile;
    },
    spaceDisplayName() {
      return this.streamOwner && this.streamOwner.displayName;
    },
    excerpts() {
      return this.activity && this.activity.excerpts;
    },
  },
  created() {
    this.profileActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
  },
};
</script>
