<template>
  <div class="d-flex flex-nowrap align-center">
    <links-item-icon
      :icon-size="iconSize"
      :icon-url="iconUrl"
      class="flex-grow-0 flex-shrink-0 me-2" />
    <div class="position-relative overflow-hidden">
      <v-file-input
        v-if="!resetInput"
        id="iconFileInput"
        ref="iconFileInput"
        accept="image/*"
        class="position-absolute t-0 l-0 full-width pa-0 ma-0"
        prepend-icon=""
        hide-details
        hide-input
        @change="uploadFile" />
      <v-btn
        :loading="sending"
        class="position-relative z-index-two btn primary"
        border
        outlined
        @click="openFileUpload">
        {{ $t('links.upload') }}
      </v-btn>
    </div>
    <v-btn
      v-if="!sending && !isDefault"
      :title="$t('links.resetDefault')"
      class="ms-2"
      icon
      @click="reset">
      <v-icon size="24">fa-undo</v-icon>
    </v-btn>
  </div>
</template>
<script>
export default {
  props: {
    link: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    iconSize: 30,
    maxFileSize: 102400,
    sending: false,
    resetInput: false,
  }),
  computed: {
    iconUrl() {
      if (this.link?.iconSrc) {
        return this.$utils.convertImageDataAsSrc(this.link.iconSrc);
      } else {
        return this.link?.iconUrl;
      }
    },
    isDefault() {
      return !this.link?.iconSrc && !this.link?.iconUrl;
    },
  },
  methods: {
    reset() {
      this.$emit('reset');
      this.sending = false;
      if (this.$refs.iconFileInput) {
        this.resetInput = true;
        this.$nextTick().then(() => this.resetInput = false);
      }
    },
    openFileUpload() {
      this.$refs.iconFileInput.$el.querySelector('input').click();
    },
    uploadFile(file) {
      this.$root.$emit('close-alert-message');
      if (file && file.size) {
        if (file.size > this.maxFileSize) {
          this.$root.$emit('alert-message', this.$t('links.tooBigFile.label'), 'error');
          return;
        }
        this.sending = true;
        const self = this;
        return this.$uploadService.upload(file)
          .then(uploadId => {
            if (uploadId) {
              const reader = new FileReader();
              reader.onload = (e) => {
                self.$emit('src', e.target.result);
                self.$forceUpdate();
              };
              reader.readAsDataURL(file);
              this.$emit('input', uploadId);
            } else {
              this.$root.$emit('alert-message', this.$t('links.uploadingError'), 'error');
            }
          })
          .catch(error => this.$root.$emit('alert-message', this.$t(String(error)), 'error'))
          .finally(() => this.sending = false);
      }
    },
  },
};
</script>