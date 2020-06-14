<template>
  <v-card class="mx-1">
    <v-card-text>
      <exo-user-avatar
        :username="poster.username"
        :fullname="poster.fullname"
        :title="poster.fullname" />
      <template v-if="activityInSpace" slot="subTitle">
        {{ spaceDisplayName }}
      </template>
    </v-card-text>
    <v-divider />
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
    poster() {
      return this.result && (this.isComment && this.result.comment.poster.profile || this.result.poster.profile);
    },
    activityInSpace() {
      return this.result && this.result.streamOwner && this.result.streamOwner && this.result.streamOwner.providerId === 'space';
    },
    excerpts() {
      return this.result && (this.isComment && this.result.comment.excerpts || this.result.excerpts);
    },
    spaceDisplayName() {
      return this.result && this.result.streamOwner && this.result.streamOwner.space && this.result.streamOwner.space.displayName;
    },
  },
  created() {
    this.profileActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
  },
};
</script>
