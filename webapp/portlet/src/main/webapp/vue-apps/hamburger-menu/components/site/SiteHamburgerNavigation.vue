<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

-->
<template>
  <v-container 
    id="SiteHamburgerNavigation"
    px-0
    py-0
    class="white">
    <div v-if="navigations" class="mx-0">
      <v-list  
        dense 
        min-width="90%"
        class="pb-0"
        :class="extraClass">
        <v-list-item-group v-model="selectedNavigationIndex" :aria-label="navigationsLabel">
          <v-list-item
            v-for="nav in navigationsToDisplay"
            :key="nav.uri"
            :input-value="nav.selected"
            :active="nav.selected"
            :selected="nav.selected"
            :href="nav.fullUri"
            :class="homeLink === nav.fullUri && 'UserPageLinkHome' || 'UserPageLink'"
            :target="nav.uriTarget"
            link>
            <v-list-item-icon class="flex align-center flex-grow-0 my-2">
              <v-icon> {{ nav.icon || defaultIcon }}</v-icon>
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title
                class="subtitle-2"
                v-text="nav.label" />
            </v-list-item-content>
            <v-list-item-action class="my-auto">
              <v-btn
                v-bind="attrs" 
                v-on="on" 
                link
                icon
                @click="selectHome($event, nav)">
                <span class="fas fa-house-user icon-default-color homePage">
                </span>
              </v-btn>
            </v-list-item-action>
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
    navigations: {
      type: Array,
      default: null,
    },
    extraClass: {
      type: String,
      default: '',
    },
  },
  data: () => ({
    BASE_SITE_URI: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/`,
    homeLink: eXo.env.portal.homeLink,
    selectedNavigation: null,
    defaultIcon: 'fas fa-folder'
  }),
  computed: {
    confirmMessage() {
      return this.$t('menu.confirmation.message.changeHome', {
        0: `<b>${this.selectedNavigation && this.selectedNavigation.label}</b>`,
      });
    },
    selectedNavigationIndex() {
      return this.navigations && this.navigations.findIndex(nav => nav.uri === eXo.env.portal.selectedNodeUri);
    },
    navigationsToDisplay() {
      this.navigations.forEach(nav => {
        nav.fullUri = nav?.pageLink && this.urlVerify(nav?.pageLink) || `/portal/${nav.siteKey.name}/${nav.uri}`;
        nav.uriTarget = nav?.target === 'SAME_TAB' && '_self' || '_blank';
      });
      return this.navigations.filter(nav => nav.visibility === 'DISPLAYED' || nav.visibility === 'TEMPORAL').slice();
    },
    navigationsLabel() {
      return `${this.site}MetaSiteNavigations`;
    } 
  },
  watch: {
    navigations() {
      if (this.navigations.length && !this.homeLink) {
        this.homeLink = `${this.BASE_SITE_URI}${this.navigations[0].uri}`;
      }
    },
  },
  created(){
    document.addEventListener('homeLinkUpdated', () => {
      this.homeLink = eXo.env.portal.homeLink;
    });
  },
  methods: {
    changeHome() {
      this.$settingService.setSettingValue('USER', eXo.env.portal.userName, 'PORTAL', 'HOME', 'HOME_PAGE_URI', this.selectedNavigation.fullUri)
        .then(() => {
          this.homeLink = eXo.env.portal.homeLink = this.selectedNavigation.fullUri;
          document.querySelector('#UserHomePortalLink').href = this.homeLink;
          document.dispatchEvent(new CustomEvent('homeLinkUpdated', {detail: this.homeLink}));
        });
    },
    selectHome(event, nav) {
      event.preventDefault();
      event.stopPropagation();

      if (this.homeLink !== nav.fullUri) {
        this.selectedNavigation = nav;
        this.$refs.confirmDialog.open();
      }
    },
    urlVerify(url) {
      if (!url.match(/^(https?:\/\/|javascript:|\/portal\/)/)) {
        url = `//${url}`;
      }
      return url ;
    },
  }
};
</script>