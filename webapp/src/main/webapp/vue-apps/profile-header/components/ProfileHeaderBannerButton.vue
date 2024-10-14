<template>
  <div class="d-flex flex-row changeBannerButton">
    <div 
      v-show="!isDefaultBanner && hover"
      class="changeBannerButtonIcon me-2">
      <v-btn
        :title="$t('UIPopupBannerUploader.title.deleteBanner')"
        id="profileBannerDeleteButton"
        class="border-color"
        outlined
        icon
        dark
        @click="removeBanner">
        <v-icon size="18">mdi-delete</v-icon>
      </v-btn>
    </div>
    <div 
      class="changeBannerButtonIcon"
      v-show="hover">
      <v-btn
        :title="$t('UIPopupBannerUploader.title.ChangeBanner')"
        id="profileBannerEditButton"
        class="border-color"
        outlined
        icon
        dark
        @click="$emit('edit')">
        <v-icon size="18">fas fa-file-image</v-icon>
      </v-btn>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    hover: {
      type: Boolean,
      default: () => false,
    },
    user: {
      type: Object,
      default: () => null,
    },
  },
  computed: {
    isDefaultBanner() {
      return this.user && this.user.banner && this.user.banner.startsWith('/portal/rest/v1/social/users/default-image/');
    },
  },
  methods: {
    removeBanner() {
      return this.$userService.updateProfileField(eXo.env.portal.userName, 'banner', 'DEFAULT_BANNER')
        .then(() => {
          this.isDefaultBanner = true;
          this.$root.$emit('alert-message', this.$t('UIPopupBannerUploader.title.BannerDeleted'), 'success');
          this.$emit('refresh');
        })
        .catch(error => this.$emit('error', error));
    },
  },
};
</script>