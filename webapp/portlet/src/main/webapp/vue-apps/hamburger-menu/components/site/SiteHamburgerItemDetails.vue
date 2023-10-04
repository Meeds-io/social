<template>
  <v-container>
    <v-card elevation="0">
      <row
        class="px-4"
        v-if="!displaySequentially"
        @click="$emit('close')">
        <v-icon
          v-if="$root.ltr"
          class="fas fa-arrow-left mt-4"
          small />
        <v-icon
          v-else
          class="fas fa-arrow-right mt-4"
          small />
      </row>
      <v-img
        :src="site.bannerUrl"
        class="mx-1 pt-1" />
      <v-card-title :title="site?.displayName" class="text-capitalize font-weight-bold text-subtitle-1">
        {{ site?.displayName }}
      </v-card-title>
      <v-card-subtitle v-sanitized-html="site?.description" class="text-subtitle-2 py-2 text-color rich-editor-content" />
      <site-hamburger-item-navigation-tree
        v-if="site?.siteNavigations?.length"
        :navigations="site.siteNavigations"
        :site-name="site?.name" />
    </v-card>
    <exo-confirm-dialog
      ref="confirmDialog"
      :title="$t('menu.confirmation.title.changeHome')"
      :message="confirmMessage"
      :ok-label="$t('menu.confirmation.ok')"
      :cancel-label="$t('menu.confirmation.cancel')"
      @ok="changeHome"
      @opened="$root.$emit('dialog-opened')"
      @closed="$root.$emit('dialog-closed')" />
  </v-container>
</template>

<script>
export default {
  props: {
    site: {
      type: Object,
      default: null
    },
    displaySequentially: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    selectedNavigation: null,
    homeLink: eXo.env.portal.homeLink,
  }),
  computed: {
    confirmMessage() {
      return this.$t('menu.confirmation.message.changeHome', {
        0: `<b>${ this.selectedNavigation?.label }</b>`,
      });
    },
  },
  created() {
    this.$root.$on('update-home-link', this.selectHome);
    document.addEventListener('homeLinkUpdated', () => this.homeLink = eXo.env.portal.homeLink);
  },
  methods: {
    navigationUri(navigation) {
      if (!navigation.pageKey) {
        return '';
      }
      let url = navigation.pageLink || `/portal/${navigation.siteKey.name}/${navigation.uri}`;
      if (!url.match(/^(https?:\/\/|javascript:|\/portal\/)/)) {
        url = `//${url}`;
      }
      return url;
    },
    selectHome(nav) {
      if (!nav?.pageKey) {
        return;
      }
      this.selectedNavigation = nav;
      this.$refs.confirmDialog.open();
    },
    changeHome() {
      const selectedNavigationUri = this.navigationUri(this.selectedNavigation);
      this.$settingService.setSettingValue('USER', eXo.env.portal.userName, 'PORTAL', 'HOME', 'HOME_PAGE_URI', selectedNavigationUri)
        .then(() => {
          this.homeLink = eXo.env.portal.homeLink = selectedNavigationUri;
          document.querySelector('#UserHomePortalLink').href = this.homeLink;
          document.dispatchEvent(new CustomEvent('homeLinkUpdated', {detail: this.homeLink}));
          this.selectedNavigation = null;
        });
    }
  }

};
</script>