<template>
  <v-hover v-slot="{hover}" :class="marginClass">
    <v-card
      :id="id"
      :elevation="hover ? 4 : 0"
      :class="{ 'border-color': !hover }"
      :loading="loading"
      height="210px"
      max-height="210px"
      width="252px"
      max-width="100%"
      class="activity-attachment overflow-hidden d-flex flex-column border-box-sizing">
      <v-card-text class="activity-attachment-thumbnail d-flex flex-grow-1 pa-0">
        <img
          :src="attachment.thumbnailUrl"
          class="mx-auto">
      </v-card-text>
      <v-expand-transition>
        <v-card
          v-if="invalid"
          class="d-flex flex-column transition-fast-in-fast-out v-card--reveal"
          elevation="0"
          style="height: 100%;">
          <v-card-text class="pb-0 d-flex flex-row">
            <v-icon color="error">fa-exclamation-circle</v-icon>
            <p class="my-auto ms-2 font-weight-bold">
              {{ $t('attachments.errorAccessingFile') }}
            </p>
          </v-card-text>
          <v-card-text class="flex-grow-1">
            <p>{{ $t('attachments.alert.unableToAccessFile') }}</p>
          </v-card-text>
          <v-card-actions class="pt-0">
            <v-btn
              text
              color="primary"
              @click="closeErrorBox">
              {{ $t('attachments.close') }}
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-expand-transition>
    </v-card>
  </v-hover>
</template>

<script>
export default {
  props: {
    attachment: {
      type: Object,
      default: null,
    },
    activity: {
      type: Object,
      default: null,
    },
    index: {
      type: Number,
      default: 0,
    },
    count: {
      type: Number,
      default: 0,
    },
    previewHeight: {
      type: String,
      default: () => '152px',
    },
    previewWidth: {
      type: String,
      default: () => '250px',
    },
  },
  data: () => ({
    loading: false,
    invalid: false,
  }),
  computed: {
    id() {
      return `PreviewAttachment_${this.previewActivity.id}_${this.index}`;
    },
    marginClass() {
      if (this.count === 1) {
        return 'mx-auto';
      }
      const lastIndex = (this.count - 1) === this.index;
      return this.index && (lastIndex && 'ms-2' || 'mx-2') || 'me-2';
    },
    previewActivity() {
      return this.activity && this.activity.parentActivity || this.activity;
    },
  },
  methods: {
    closeErrorBox(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      window.setTimeout(() => {
        this.invalid = false;
      }, 50);
    },
  },
};
</script>
