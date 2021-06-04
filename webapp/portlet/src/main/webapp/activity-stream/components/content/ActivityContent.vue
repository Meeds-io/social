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
      class="d-flex flex-no-wrap activity-thumbnail-box">
      <template v-if="isMobile">
        <div class="border-color border-box-sizing ma-4">
          <v-avatar
            v-if="supportsThumbnail"
            min-height="75vw"
            height="75vw"
            min-width="calc(100vw - 56px)"
            width="calc(100vw - 56px)"
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
              size="58"
              contain>
              far fa-image
            </v-icon>
          </v-avatar>
          <div v-if="title" class="pa-4 grey-background border-top-color">
            <h6
              :title="title"
              v-sanitized-html="title"
              class="font-weight-bold text-color ma-0 text-truncate">
            </h6>
          </div>
        </div>
      </template>
      <template v-else>
        <v-avatar
          v-if="supportsThumbnail"
          class="border-color border-box-sizing ma-4"
          min-height="150"
          height="150"
          min-width="252"
          width="252"
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
            size="58"
            contain>
            far fa-image
          </v-icon>
        </v-avatar>
        <div class="text-truncate me-4 my-4">
          <h5
            v-if="title"
            :title="title"
            v-sanitized-html="title"
            class="font-weight-bold text-color pb-2 pt-0 ma-0 text-truncate">
          </h5>
          <ellipsis
            v-if="summary"
            :title="summary"
            :data="summary"
            :line-clamp="3"
            end-char="..."
            class="caption text-light-color text-wrap" />
        </div>
      </template>
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
    isMobile() {
      return this.$vuetify.breakpoint.name === 'xs';
    },
    sourceLinkTarget() {
      return this.sourceLink && this.sourceLink.indexOf('/') === 0 && '_self' || '_blank';
    },
    isBodyNotEmpty() {
      return this.body && this.body.trim() !== '<p></p>';
    },
  },
};
</script>