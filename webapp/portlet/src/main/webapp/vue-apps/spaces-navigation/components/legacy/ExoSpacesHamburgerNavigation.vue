
<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2022 Meeds Association
  contact@meeds.io
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
    px-0
    pt-0
    class="border-box-sizing">
    <v-row class="mx-0 clickable spacesNavigationTitle">
      <v-list-item
        link
        @mouseover="openDrawer()"
        @click="openDrawer()">
        <v-list-item-icon class="mb-2 mt-3 me-6 titleIcon">
          <i class="uiIcon uiIconToolbarNavItem spacesIcon"></i>
        </v-list-item-icon>
        <v-list-item-content class="subtitle-1 titleLabel">
          {{ $t('menu.spaces.lastVisitedSpaces') }}
        </v-list-item-content>
        <v-list-item-action class="my-0">
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
export default {
  data() {
    return {
      homeLink: eXo.env.portal.homeLink,
      selectedSpace: null,
      spacesLimit: 7,
      secondLevelVueInstance: null,
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
      this.$spacesAdministrationServices.setSettingValue('USER', eXo.env.portal.userName, 'PORTAL', 'HOME', 'HOME_PAGE_URI', this.selectedSpace.spaceUrl)
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
        const VueHamburgerMenuItem = Vue.extend(Vue.options.components['exo-recent-spaces-hamburger-menu-navigation']);
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
    openDrawer() {
      this.$emit('open-second-level');
    },
  },
};
</script>
