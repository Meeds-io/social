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
    <v-card-text class="pb-0">
      <blockquote
        v-for="(excerpt, index) in excerpts"
        :key="index"
        class="text-color mb-1"
        v-html="excerpt">
      </blockquote>
    </v-card-text>
    <v-card-actions>
      <v-btn
        :href="link"
        link
        text
        color="primary"
        class="text-capitalize">
        {{ $t('Search.activity.open') }}
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
    link() {
      if (this.isComment) {
        return `/${eXo.env.portal.containerName}/${eXo.env.portal.portalName}/activity?id=${this.result.id}#comment-comment${this.result.comment.id}`;
      } else {
        return `/${eXo.env.portal.containerName}/${eXo.env.portal.portalName}/activity?id=${this.activity.id}`;
      }
    },
  },
  created() {
    this.profileActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
  },
};
</script>
