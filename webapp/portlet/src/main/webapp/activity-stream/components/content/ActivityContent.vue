<template>
  <v-card flat>
    <v-card-text
      v-if="isBodyNotEmpty"
      v-sanitized-html="body"
      class="postContent text-color pt-0" />
    <a
      v-if="sourceLink"
      :href="sourceLink"
      :target="sourceLinkTarget"
      class="d-flex flex-no-wrap pa-4 activity-thumbnail-box">
      <v-avatar
        v-if="supportsThumbnail"
        class="border-color border-box-sizing me-4"
        min-height="100"
        height="100"
        min-width="130"
        width="130"
        rounded
        tile>
        <v-img
          v-if="thumbnail"
          :src="thumbnail"
          min-height="100%"
          min-width="100%" />
        <v-icon
          v-else
          class="text-sub-title"
          size="42"
          contain>
          far fa-image
        </v-icon>
      </v-avatar>
      <div class="text-truncate">
        <h5
          v-if="title"
          :title="title"
          v-sanitized-html="title"
          class="font-weight-bold text-color pb-4 pt-0 ma-0 text-truncate">
        </h5>
        <ellipsis
          v-if="summary"
          :title="summary"
          :data="summary"
          :line-clamp="4"
          end-char="..."
          class="caption text-light-color pb-4 text-wrap" />
      </div>
    </a>
  </v-card>
</template>

<script>
export default {
  props: {
    activityLink: {
      type: String,
      default: null,
    },
    body: {
      type: String,
      default: null,
    },
    title: {
      type: String,
      default: null,
    },
    summary: {
      type: String,
      default: null,
    },
    supportsThumbnail: {
      type: Boolean,
      default: false,
    },
    thumbnail: {
      type: String,
      default: null,
    },
    sourceLink: {
      type: String,
      default: null,
    },
  },
  computed: {
    sourceLinkTarget() {
      return this.sourceLink && this.sourceLink.indexOf('/') === 0 && '_self' || '_blank';
    },
    isBodyNotEmpty() {
      return this.body && this.body.trim() !== '<p></p>';
    },
  },
};
</script>