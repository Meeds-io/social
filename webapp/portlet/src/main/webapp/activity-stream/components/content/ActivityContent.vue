<template>
  <v-card
    :loading="loading"
    flat>
    <v-card-text
      v-if="isBodyNotEmpty"
      v-sanitized-html="body"
      class="postContent text-color pt-0" />
    <v-tooltip
      v-if="sourceLink"
      :left="!$vuetify.rtl"
      :right="$vuetify.rtl"
      top>
      <template v-slot:activator="{ on, attrs }">
        <a
          :href="sourceLink"
          :target="sourceLinkTarget"
          class="d-flex flex-no-wrap activity-thumbnail-box"
          v-bind="attrs"
          v-on="tooltip && on">
          <template v-if="isMobile">
            <div class="border-color border-box-sizing col pa-4">
              <v-avatar
                v-if="supportsThumbnail"
                min-height="75vw"
                height="75vw"
                min-width="100%"
                width="100%"
                rounded
                tile>
                <v-img
                  v-if="thumbnail"
                  :src="thumbnail"
                  min-height="100%"
                  min-width="100%" />
                <v-icon
                  v-else
                  :size="defaultIconSize"
                  class="grey-text"
                  contain>
                  {{ defaultIconClass }}
                </v-icon>
              </v-avatar>
              <div v-if="title" class="pa-4 grey-background border-top-color">
                <ellipsis
                  v-if="title"
                  :title="title"
                  :data="title"
                  :line-clamp="2"
                  end-char="..."
                  class="font-weight-bold text-color ma-0 text-wrap" />
              </div>
            </div>
          </template>
          <template v-else>
            <v-avatar
              v-if="supportsThumbnail"
              class="border-color border-box-sizing ma-4"
              min-height="150px"
              height="150px"
              min-width="252px"
              width="252px"
              rounded
              tile>
              <v-img
                v-if="thumbnail"
                :src="thumbnail"
                min-height="100%"
                min-width="100%" />
              <v-icon
                v-else
                :size="defaultIconSize"
                class="grey-text"
                contain>
                {{ defaultIconClass }}
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
      </template>
      <span>{{ tooltip }}</span>
    </v-tooltip>
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
    tooltip: {
      type: String,
      default: null,
    },
    sourceLink: {
      type: String,
      default: null,
    },
    defaultIcon: {
      type: Object,
      default: null,
    },
  },
  computed: {
    defaultIconClass() {
      return this.defaultIcon && this.defaultIcon.icon || 'far fa-image';
    },
    defaultIconSize() {
      return this.defaultIcon && this.defaultIcon.size || 58;
    },
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
  created() {
    this.$root.$on('activity-stream-updating-activity-start', () => this.loading = true);
    this.$root.$emit('activity-stream-updating-activity-end', () => this.loading = false);
  },
};
</script>