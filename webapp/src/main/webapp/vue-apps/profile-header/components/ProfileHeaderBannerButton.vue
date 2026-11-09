<template>
  <div
    class="d-flex flex-row changeBannerButton"
    @focusin="onFocusIn"
    @focusout="onFocusOut">
    <div
      class="d-flex"
      v-if="owner">
      <div
        v-show="!isDefaultBanner"
        :class="{ 'd-sr-only-focusable': !revealed }"
        class="changeBannerButtonIcon me-2">
        <v-btn
          :title="$t('UIPopupBannerUploader.title.deleteBanner')"
          :aria-label="$t('UIPopupBannerUploader.title.deleteBanner')"
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
        :class="{ 'd-sr-only-focusable': !revealed }"
        class="changeBannerButtonIcon">
        <v-btn
          :title="$t('UIPopupBannerUploader.title.ChangeBanner')"
          :aria-label="$t('UIPopupBannerUploader.title.ChangeBanner')"
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
    <div
      v-if="isAdmin"
      :class="{ 'd-sr-only-focusable': !revealed }"
      class="changeBannerButtonIcon ms-2">
      <v-btn
        id="profileHeaderEditOptions"
        :title="$t('profileHeader.edit.tooltip')"
        :aria-label="$t('profileHeader.edit.tooltip')"
        class="border-color"
        outlined
        icon
        dark
        @click="$emit('open-settings')">
        <v-icon size="18">fas fa-cog</v-icon>
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
    owner: {
      type: Boolean,
      default: false
    },
    isAdmin: {
      type: Boolean,
      default: false
    }
  },
  data: () => ({
    focused: false,
  }),
  computed: {
    revealed() {
      return this.hover || this.focused;
    },
    isDefaultBanner() {
      return this.user && this.user.banner && this.user.banner.startsWith('/portal/rest/v1/social/users/default-image/');
    },
  },
  methods: {
    onFocusIn(event) {
      this.focused = event.target.matches(':focus-visible');
    },
    onFocusOut(event) {
      this.focused = !!(event.relatedTarget && event.currentTarget.contains(event.relatedTarget));
    },
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
