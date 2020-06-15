<template>
  <v-card class="d-flex flex-column border-radius box-shadow mr-2 mb-4" flat min-height="227">
    <v-card-text class="pa-2">
      <exo-user-avatar
        :username="poster.username"
        :fullname="poster.fullname"
        :title="poster.fullname">
      </exo-user-avatar>
    </v-card-text>
    <v-divider class="box-shadow no-border" />
    <v-card-text class="pb-0 flex-grow-1 flex-shrink-0">
      <blockquote
        v-if="excerpt"
        class="text-color mb-0"
        v-html="excerpt">
      </blockquote>
    </v-card-text>
    <v-card-actions class="pt-1">
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
    excerpt() {
      const excerpt = this.excerpts && this.excerpts.length && this.excerpts[0];
      if (!excerpt) {
        return '';
      }
      return $('<div />').html(excerpt).html();
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
