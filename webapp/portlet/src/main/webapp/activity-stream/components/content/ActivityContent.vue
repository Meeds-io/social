<template>
  <v-card flat>
    <v-card-text v-sanitized-html="body" class="postContent text-color pt-0" />
    <a
      v-if="sourceLink"
      :href="sourceLink"
      :target="sourceLinkTarget"
      class="d-flex flex-no-wrap border-box-sizing pa-4">
      <v-avatar
        v-if="supportsThumbnail"
        class="border-color me-4"
        height="150"
        width="250"
        rounded
        tile>
        <v-img
          v-if="thumbnail"
          :src="thumbnail"
          max-height="150"
          max-width="250"
          contain />
        <v-icon
          v-else
          size="56"
          contain>
          far fa-image
        </v-icon>
      </v-avatar>
      <div>
        <div
          v-if="title"
          v-sanitized-html="title"
          class="font-weight-bold text-color pb-4">
        </div>
        <div
          v-if="summary"
          v-sanitized-html="summary"
          class="caption text-light-color pb-4">
        </div>
        <a
          v-if="sourceLink"
          :href="sourceLink"
          :target="sourceLinkTarget">
          <v-icon
            v-if="sourceIcon"
            v-text="sourceIcon"
            class="primary--text"
            size="12" />
          {{ sourceLink }}
        </a>
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
    sourceIcon: {
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
  },
};
</script>