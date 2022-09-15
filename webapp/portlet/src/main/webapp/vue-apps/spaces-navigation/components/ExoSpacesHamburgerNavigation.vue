<template>
  <v-container
    px-0
    pt-0
    class="border-box-sizing">
    <v-row class="mx-0 spacesNavigationTitle">
      <v-list-item
        link>
        <v-list-item-icon class="mb-2 mt-3 me-6 titleIcon">
          <i class="uiIcon uiIconToolbarNavItem spacesIcon"></i>
        </v-list-item-icon>
        <v-list-item-content class="subtitle-1 titleLabel">
          {{ $t('menu.spaces.lastVisitedSpaces') }}
        </v-list-item-content>
        <v-list-item-action class="my-0" @click="toggleOpenDrawer()">
          <i class="uiIcon uiArrowRightIcon" color="grey lighten-1"></i>
        </v-list-item-action>
      </v-list-item>
    </v-row>
    <exo-spaces-navigation-content
      :limit="spacesLimit"
      :home-link="homeLink"
      home-icon
      shaped
      @selectHome="selectHome" />
    <exo-confirm-dialog
      ref="confirmDialog"
      :title="$t('menu.confirmation.title.changeHome')"
      :message="confirmMessage"
      :ok-label="$t('menu.confirmation.ok')"
      :cancel-label="$t('menu.confirmation.cancel')"
      @ok="changeHome" />
  </v-container>
</template>
<script>
import RecentSpacesHamburgerNavigation from './ExoRecentSpacesHamburgerNavigation.vue';

import {setSettingValue} from '../../common/js/SettingService.js';

export default {
  data() {
    return {
      homeLink: eXo.env.portal.homeLink,
      selectedSpace: null,
      spacesLimit: 7,
      secondLevelVueInstance: null,
      secondeLevel: false
    };
  },
  computed: {
    confirmMessage() {
      return this.$t('menu.confirmation.message.changeHome', {
        0: `<b>${this.selectedSpace && this.selectedSpace.displayName}</b>`,
      });
    },
  },
  created() {
    document.addEventListener('homeLinkUpdated', () => {
      this.homeLink = eXo.env.portal.homeLink;
    });
  },
  methods: {
    changeHome() {
      setSettingValue('USER', eXo.env.portal.userName, 'PORTAL', 'HOME', 'HOME_PAGE_URI', this.selectedSpace.spaceUrl)
        .then(() => {
          this.homeLink = eXo.env.portal.homeLink = this.selectedSpace.spaceUrl;
          $('#UserHomePortalLink').attr('href', this.homeLink);
          document.dispatchEvent(new CustomEvent('homeLinkUpdated', {detail: this.homeLink}));
        });
    },
    selectHome(event, space) {
      if (this.homeLink === space.spaceUrl) {
        return;
      }
      event.preventDefault();
      event.stopPropagation();

      this.selectedSpace = space;
      this.$refs.confirmDialog.open();
    },
    mountSecondLevel(parentId) {
      if (!this.secondLevelVueInstance) {
        const VueHamburgerMenuItem = Vue.extend(RecentSpacesHamburgerNavigation);
        const vuetify = this.vuetify;
        this.secondLevelVueInstance = new VueHamburgerMenuItem({
          i18n: new VueI18n({
            locale: this.$i18n.locale,
            messages: this.$i18n.messages,
          }),
          vuetify,
          el: parentId,
        });
      } else {
        const element = $(parentId)[0];
        element.innerHTML = '';
        element.appendChild(this.secondLevelVueInstance.$el);
      }
      this.$nextTick().then(() => {
        this.secondLevelVueInstance.$on('close-menu', () => {
          this.$emit('close-second-level');
        });
      });
    },
    toggleOpenDrawer() {
      this.secondeLevel = !this.secondeLevel;
      if (this.secondeLevel) {
        this.$emit('open-second-level');
      } else {
        this.$emit('close-second-level');
      }
    },
  },
};
</script>
