<template>
  <v-container 
    id="SiteHamburgerNavigation"
    px-0
    py-0
    class="white">
    <v-row
      class="mx-0">
      <v-list 
        shaped 
        dense 
        min-width="90%"
        class="pb-0">
        <v-list-item-group v-model="selectedNavigationIndex">
          <v-list-item
            v-for="nav in navigations"
            :key="nav.uri"
            :input-value="nav.selected"
            :active="nav.selected"
            :selected="nav.selected"
            :href="`${BASE_SITE_URI}${nav.uri}`"
            :class="homeLink === nav.uri && 'SitePageLinkHome' || 'SitePageLink'"
            link>
            <v-list-item-icon class="mr-6 my-2">
              <i :class="nav.iconClass"></i>
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title
                class="subtitle-2"
                v-text="nav.label">
              </v-list-item-title>
            </v-list-item-content>
            <v-list-item-icon>
              <span class="SitePageHome" @click="selectHome($event, nav)">
              </span>
            </v-list-item-icon>
          </v-list-item>
        </v-list-item-group>
      </v-list>
    </v-row>
  </v-container>
</template>
<script>
import {updateProfileField} from '../../common/js/UserService.js';

export default {
  data: () => ({
    BASE_SITE_URI: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/`,
    homeLink: eXo.env.portal.homeLink,
    navigationScope: 'children',
    navigationVisibilities: ['displayed'],
    navigations: [],
  }),
  computed:{
    visibilityQueryParams() {
      return this.navigationVisibilities.map(visibilityName => `visibility=${visibilityName}`).join('&');
    },
    selectedNavigationIndex() {
      return this.navigations.findIndex(nav => nav.uri === eXo.env.portal.selectedNodeUri);
    },
  },
  watch: {
    navigations() {
      this.navigations.forEach(nav => {
        const capitilizedName = `${nav.name[0].toUpperCase()}${nav.name.slice(1)}`;
        nav.iconClass = `uiIcon uiIconFile uiIconToolbarNavItem uiIcon${capitilizedName} icon${capitilizedName} ${nav.icon}`;
      });
    },
  },
  created(){
    fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/navigations/portal/${eXo.env.portal.portalName}?scope=${this.navigationScope}&${this.visibilityQueryParams}`, {
      method: 'GET',
      credentials: 'include',
    })
      .then(resp => resp && resp.ok && resp.json())
      .then(data => {
        this.navigations = data;
        if (this.navigations && this.navigations.length && !this.homeLink) {
          this.homeLink = this.navigations[0].uri;
        }
      })
      .finally(() => {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      });
  },
  methods: {
    selectHome(event, nav) {
      event.preventDefault();
      event.stopPropagation();

      updateProfileField(eXo.env.portal.userName, 'homePage', nav.uri)
        .then(() => {
          this.homeLink = eXo.env.portal.homeLink = nav.uri;
          $('#UserHomePortalLink').attr('href', `${this.BASE_SITE_URI}${nav.uri}`);
        });
    },
  }
};
</script>

