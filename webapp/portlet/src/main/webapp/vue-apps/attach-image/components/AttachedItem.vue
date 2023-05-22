<template>
  <v-card
    :class="{ 'border-color': !hover }"
    :loading="loading"
    :width="previewWidth"
    class="attachment-card-item overflow-hidden d-flex flex-column border-box-sizing mx-2"
    height="210px"
    max-height="210px"
    max-width="100%"
    hover
    @click="$emit('preview-attachment')">
    <v-card-text class="attachment-card-item-thumbnail d-flex flex-grow-1 pa-0">
      <img
        :src="attachment.thumbnailUrl"
        alt="attached image"
        class="ma-auto full-width">
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
</template>

<script>
export default {
  props: {
    attachment: {
      type: Object,
      default: null,
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
