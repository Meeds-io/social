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
    id="RecentSpaceHamburgerNavigation"
    px-0
    pt-0
    class="border-box-sizing">
    <v-row class="mx-0 spacesNavigationTitle">
      <v-list-item
        v-if="isMobile"
        @click="$root.$emit('change-recent-spaces-menu')">
        <v-list-item-icon class="mb-2 mt-3 titleIcon">
          <i class="uiIcon uiIconToolbarNavItem spacesIcon"></i>
        </v-list-item-icon>
        <v-list-item-content class="subtitle-2">
          {{ $t('menu.spaces.lastVisitedSpaces') }}
        </v-list-item-content>
        <v-list-item-action class="my-0 d-flex flex-row align-center">
          <v-btn 
            v-if="canAddSpaces"
            icon 
            link
            :href="allSpacesLink" 
            @click="leftNavigationActionEvent($event,'addNewSpace')">
            <v-icon class="me-0 pa-2 icon-default-color clickable" small>
              fa-plus
            </v-icon>
          </v-btn>
        </v-list-item-action>
      </v-list-item>
      <v-list-item
        v-else
        @mouseover="showItemActions = true;spacePanel=false" 
        @mouseleave="showItemActions = false">
        <v-list-item-icon class="mb-2 mt-3 titleIcon">
          <i class="uiIcon uiIconToolbarNavItem spacesIcon"></i>
        </v-list-item-icon>
        <v-list-item-content class="subtitle-2">
          {{ $t('menu.spaces.lastVisitedSpaces') }}
        </v-list-item-content>
        <v-list-item-action v-if="toggleArrow" class="my-0 d-flex flex-row align-center">
          <v-btn
            v-if="canAddSpaces"
            :title="$t('menu.createNewSpace')"
            link
            icon 
            :href="allSpacesLink" 
            @click="leftNavigationActionEvent($event,'addNewSpace')">
            <v-icon class="me-0 pa-2 icon-default-color clickable" small>
              fa-plus
            </v-icon>
          </v-btn>
          <ripple-hover-button
            icon
            @ripple-hover="$root.$emit('change-recent-spaces-menu')">
            <v-icon class="me-0 pa-2 icon-default-color clickable" small>
              {{ arrowIconClass }} 
            </v-icon>
          </ripple-hover-button>
        </v-list-item-action>
      </v-list-item>
    </v-row>
    <spaces-navigation-content
      :limit="spacesLimit"
      :home-link="homeLink"
      :opened-space="!thirdLevel && openedSpace"
      :recent-spaces="recentSpaces"
      home-icon
      shaped />
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
    recentSpacesDrawerOpened: {
      type: Boolean,
      default: false,
    },
    openedSpace: {
      type: Object,
      default: null,
    },
    thirdLevel: {
      type: Boolean,
      default: false,
    },
    recentSpaces: {
      type: Array,
      default: null,
    },
  },
  data() {
    return {
      channelName: '/SpaceWebNotification',
      homeLink: eXo.env.portal.homeLink,
      selectedSpace: null,
      spacesLimit: 7,
      showItemActions: false,
      arrowIcon: 'fa-arrow-right',
      allSpacesLink: `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/spaces?createSpace=true`,
      canAddSpaces: false,
      space: null,
      spacePanel: false,
    };
  },
  computed: {
    arrowIconClass() {
      return this.recentSpacesDrawerOpened && this.arrowIconLeft || this.arrowIconRight;
    },
    arrowIconLeft() {
      return this.$root.ltr && 'fa-arrow-left' || 'fa-arrow-right';
    },
    arrowIconRight() {
      return this.$root.ltr && 'fa-arrow-right' || 'fa-arrow-left';
    },
    toggleArrow() {
      return this.showItemActions && !this.spacePanel;
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'sm' || this.$vuetify.breakpoint.name === 'xs';
    },
    confirmMessage() {
      return this.$t('menu.confirmation.message.changeHome', {
        0: `<b>${this.openedSpace && this.openedSpace.displayName}</b>`,
      });
    },
  },
  created() {
    this.canAddSpaces = this.$root.canAddSpaces;
    this.$socialWebSocket.initCometd(this.channelName);
    this.$root.$on('change-home-link-space', this.selectHome);
    document.addEventListener('homeLinkUpdated', () => this.homeLink = eXo.env.portal.homeLink);
  },
  methods: {
    changeHome() {
      this.$settingService.setSettingValue('USER', eXo.env.portal.userName, 'PORTAL', 'HOME', 'HOME_PAGE_URI', this.url(this.selectedSpace))
        .then(() => {
          this.homeLink = eXo.env.portal.homeLink = this.url(this.selectedSpace);
          document.querySelector('#UserHomePortalLink').href = this.homeLink;
          document.dispatchEvent(new CustomEvent('homeLinkUpdated', {detail: this.homeLink}));
        });
    },
    selectHome(space) {
      if (this.homeLink === this.url(space)) {
        return;
      }
      this.selectedSpace = space;
      this.$refs.confirmDialog.open();
    },
    url(space) {
      if (space && space.groupId) {
        const uriPart = space.groupId.replace(/\//g, ':');
        return `${eXo.env.portal.context}/g/${uriPart}/`;
      } else {
        return '#';
      }
    },
  },
};
</script>
