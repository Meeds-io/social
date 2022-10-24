<template>
  <v-avatar
    :class="owner && hover && 'profileHeaderAvatarHoverEdit'"
    :size="size"
    class="align-start flex-grow-0 ms-3 my-3 profileHeaderAvatar">
    <v-img
      :lazy-src="(avatarData || user && `${user.avatar}&size=165x165`) || ''"
      :src="(avatarData || user && `${user.avatar}&size=165x165`) || ''"
      transition="none"
      eager
      role="presentation" />
    <v-file-input
      v-if="owner && !sendingImage"
      v-show="hover"
      ref="avatarInput"
      prepend-icon="mdi-camera"
      class="changeAvatarButton"
      accept="image/*"
      clearable
      @change="uploadAvatar" />
  </v-avatar>
</template>

<script>
export default {
  props: {
    user: {
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
    owner: {
      type: Boolean,
      default: () => true,
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
        if (file.size > this.maxUploadSize) {
          this.$emit('error', this.$uploadService.avatarExcceedsLimitError);
          return;
        }
        this.sendingImage = true;
        const thiss = this;
        return this.$uploadService.upload(file)
          .then(uploadId => {
            if (this.save) {
              return this.$userService.updateProfileField(eXo.env.portal.userName, 'avatar', uploadId);
            } else {
              const reader = new FileReader();
              reader.onload = (e) => {
                thiss.avatarData = e.target.result;
                thiss.$forceUpdate();
              };
              reader.readAsDataURL(file);
              this.$emit('input', uploadId);
            }
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