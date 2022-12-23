<template>
  <div class="d-flex">
    <v-avatar
      class="spaceAvatar"
      :class="hover && 'spaceAvatarHoverEdit'"
      :size="size"
      tile>
      <v-img :src="avatarData || avatarUrl || ''" role="presentation" />
    </v-avatar>
    <v-file-input
      v-if="!sendingImage"
      v-show="hover"
      ref="avatarInput"
      prepend-icon="mdi-camera"
      class="changeAvatarButton ma-0 pa-2 align-center"
      accept="image/*"
      clearable
      hide-input
      @change="uploadAvatar" />
  </div>
</template>

<script>
export default {
  props: {
    avatarUrl: {
      type: Object,
      default: () => null,
    },
    maxUploadSize: {
      type: Number,
      default: () => 0,
    },
    save: {
      type: Boolean,
      default: () => false,
    },
    hover: {
      type: Boolean,
      default: () => false,
    },
    size: {
      type: Number,
      default: () => 165,
    },
    value: {
      type: String,
      default: () => null,
    },
  },
  data: () => ({
    sendingImage: false,
    avatarData: null,
  }),
  computed: {
    maxUploadSizeInBytes() {
      return Number(this.maxUploadSize) * 1024 *1024;
    },
  },
  watch: {
    sendingImage() {
      if (this.sendingImage) {
        document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
      } else {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      }
    },
  },
  methods: {
    reset() {
      this.avatarData = null;
      this.sendingImage = false;
    },
    uploadAvatar(file) {
      if (file && file.size) {
        if (file.type && file.type.indexOf('image/') !== 0) {
          this.$emit('error', this.$t('profile.warning.message.fileType'));
          return;
        }
        if (file.size > this.maxUploadSizeInBytes) {
          this.$emit('error', this.$uploadService.avatarExcceedsLimitError);
          return;
        }
        this.sendingImage = true;
        const thiss = this;
        return this.$uploadService.upload(file)
          .then(uploadId => {
            const reader = new FileReader();
            reader.onload = (e) => {
              thiss.avatarData = e.target.result;
              thiss.$forceUpdate();
            };
            reader.readAsDataURL(file);
            this.$emit('input', uploadId);
          })
          .then(() => this.$emit('refresh'))
          .catch(error => this.$emit('error', error))
          .finally(() => {
            this.sendingImage = false;
          });
      }
    },
  },
};
</script>