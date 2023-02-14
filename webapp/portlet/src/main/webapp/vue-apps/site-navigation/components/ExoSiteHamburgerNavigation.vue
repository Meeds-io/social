<template>
  <v-container 
    id="SiteHamburgerNavigation"
    px-0
    py-0
    class="white">
    <div
      class="mx-0">
      <v-list  
        dense 
        min-width="90%"
        class="pb-0">
        <v-list-item-group v-model="selectedNavigationIndex">
          <v-list-item
            v-for="nav in navigationsToDisplay"
            :key="nav.uri"
            :input-value="nav.selected"
            :active="nav.selected"
            :selected="nav.selected"
            :href="nav.fullUri"
            :class="homeLink === nav.fullUri && 'UserPageLinkHome' || 'UserPageLink'"
            link>
            <v-list-item-icon class="me-6 my-2">
              <i :class="nav.iconClass"></i>
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title
                class="subtitle-2"
                v-text="nav.label" />
            </v-list-item-content>
            <v-list-item-icon @click="selectHome($event, nav)">
              <span class="fas mt-1 fa-house-user icon-default-color homePage">
              </span>
            </v-list-item-icon>
          </v-list-item>
        </v-list-item-group>
      </v-list>
    </div>
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
import {setSettingValue} from '../../common/js/SettingService.js';

export default {
  data: () => ({
    BASE_SITE_URI: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/`,
    homeLink: eXo.env.portal.homeLink,
    selectedNavigation: null,
    navigationScope: 'children',
    navigationVisibilities: ['displayed'],
    navigations: []
  }),
  computed: {
    confirmMessage() {
      return this.$t('menu.confirmation.message.changeHome', {
        0: `<b>${this.selectedNavigation && this.selectedNavigation.label}</b>`,
      });
    },
    visibilityQueryParams() {
      return this.navigationVisibilities.map(visibilityName => `visibility=${visibilityName}`).join('&');
    },
    selectedNavigationIndex() {
      return this.navigations.findIndex(nav => nav.uri === eXo.env.portal.selectedNodeUri);
    },
    navigationsToDisplay() {
      this.navigations.forEach(nav => {
        const capitilizedName = `${nav.name[0].toUpperCase()}${nav.name.slice(1)}`;
        nav.iconClass = `uiIcon uiIconFile uiIconToolbarNavItem uiIcon${capitilizedName} icon${capitilizedName} ${nav.icon}`;
        nav.fullUri = `${this.BASE_SITE_URI}${nav.uri}`;
      });
      return this.navigations.slice();
    },
  },
  watch: {
    navigations() {
      if (this.navigations.length && !this.homeLink) {
        this.homeLink = `${this.BASE_SITE_URI}${this.navigations[0].uri}`;
      }
    },
  },
  created(){
    fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/navigations/portal/?siteName=${eXo.env.portal.portalName}&scope=${this.navigationScope}&${this.visibilityQueryParams}`, {
      method: 'GET',
      credentials: 'include',
    })
      .then(resp => resp && resp.ok && resp.json())
      .then(data => {
        const navigations = data || [];
        if (!this.navigations || !this.navigations.length) {
          this.navigations = navigations;
        }
      });
    document.addEventListener('homeLinkUpdated', () => {
      this.homeLink = eXo.env.portal.homeLink;
    });
  },
  methods: {
    changeHome() {
      setSettingValue('USER', eXo.env.portal.userName, 'PORTAL', 'HOME', 'HOME_PAGE_URI', this.selectedNavigation.fullUri)
        .then(() => {
          this.homeLink = eXo.env.portal.homeLink = this.selectedNavigation.fullUri;
          $('#UserHomePortalLink').attr('href', this.homeLink);
          document.dispatchEvent(new CustomEvent('homeLinkUpdated', {detail: this.homeLink}));
        });
    },
    selectHome(event, nav) {
      event.preventDefault();
      event.stopPropagation();

      this.selectedNavigation = nav;
      this.$refs.confirmDialog.open();
    },
  }
};
</script>