<template>
  <v-container
    px-0
    pt-0
    class="border-box-sizing">
    <v-row class="mx-0 spacesNavigationTitle">
      <v-list-item
        v-if="isMobile"
        @click="openOrCloseDrawer()">
        <v-list-item-icon class="mb-2 mt-3 me-6 titleIcon">
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
        <v-list-item-icon class="mb-2 mt-3 me-6 titleIcon">
          <i class="uiIcon uiIconToolbarNavItem spacesIcon"></i>
        </v-list-item-icon>
        <v-list-item-content class="subtitle-2">
          {{ $t('menu.spaces.lastVisitedSpaces') }}
        </v-list-item-content>
        <v-list-item-action v-if="toggleArrow" class="my-0 d-flex flex-row align-center">
          <v-btn
            v-if="canAddSpaces"
            link
            icon 
            :href="allSpacesLink" 
            @click="leftNavigationActionEvent($event,'addNewSpace')">
            <v-icon class="me-0 pa-2 icon-default-color clickable" small>
              fa-plus
            </v-icon>
          </v-btn>
          <v-btn icon @click="openOrCloseDrawer()">
            <v-icon class="me-0 pa-2 icon-default-color clickable" small>
              {{ arrowIconClass }} 
            </v-icon>
          </v-btn>
        </v-list-item-action>
      </v-list-item>
    </v-row>
    <exo-spaces-navigation-content
      :limit="spacesLimit"
      :home-link="homeLink"
      home-icon
      shaped
      @open-space-panel="openSpacePanel($event)"
      @close-space-pane="openSpacePanel($event)" />
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
  data() {
    return {
      channelName: '/SpaceWebNotification',
      homeLink: eXo.env.portal.homeLink,
      selectedSpace: null,
      spacesLimit: 7,
      secondLevelVueInstance: null,
      secondeLevel: false,
      secondeLevelSpacePanel: false,
      showItemActions: false,
      arrowIcon: 'fa-arrow-right',
      allSpacesLink: `${eXo.env.portal.context}/${ eXo.env.portal.portalName }/all-spaces?createSpace=true`,
      canAddSpaces: false,
      isRecentSpaces: false,
      space: null,
      spacePanel: false,
    };
  },
  computed: {
    confirmMessage() {
      return this.$t('menu.confirmation.message.changeHome', {
        0: `<b>${this.selectedSpace && this.selectedSpace.displayName}</b>`,
      });
    },
    arrowIconClass() {
      return this.arrowIcon;
    },
    toggleArrow() {
      return (this.secondeLevel || this.showItemActions) && !this.spacePanel;
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'sm' || this.$vuetify.breakpoint.name === 'xs';
    },
    recentSpaces() {
      return this.isRecentSpaces;
    }
  },
  created() {
    this.$spacesAdministrationServices.checkCanCreateSpaces()
      .then(data => this.canAddSpaces = data);
    this.$socialWebSocket.initCometd(this.channelName);
    document.addEventListener('homeLinkUpdated', () => {
      this.homeLink = eXo.env.portal.homeLink;
    });
    document.addEventListener('second-level-hidden', () => {
      this.hideSecondeItem();
    });
    document.addEventListener('second-level-opened', (event) => {
      if ( event && event.detail && event.detail.contentDetail.id !== 'HamburgerMenuNavigationSpaces') { 
        this.hideSecondeItem();
      }
    });
  },
  methods: {
    changeHome() {
      setSettingValue('USER', eXo.env.portal.userName, 'PORTAL', 'HOME', 'HOME_PAGE_URI', this.url(this.selectedSpace))
        .then(() => {
          this.homeLink = eXo.env.portal.homeLink = this.url(this.selectedSpace);
          $('#UserHomePortalLink').attr('href', this.homeLink);
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
    mountSecondLevel(parentId) {
      let VueHamburgerMenuItem = null;
      const self = this;
      if (this.recentSpaces) {
        VueHamburgerMenuItem = Vue.extend({
          data: () => {
            return {
              space: this.space,
              thirdLevel: false
            };
          },
          methods: {
            openSpacePanel(event) {
              let openedSpace = null;
              if (this.space) {
                openedSpace = this.space.id;
              }
              this.space = event;
              this.thirdLevel = !this.thirdLevel;
              if (this.thirdLevel || (openedSpace !== this.space.id && openedSpace!==null)) {
                document.dispatchEvent(new CustomEvent('space-opened', {detail: openedSpace} ));
                document.dispatchEvent(new CustomEvent('display-third-level', {detail: {
                  'space': event,
                  'component': this
                }}));
              } else {
                document.dispatchEvent(new CustomEvent('hide-third-level'));
              }
              
            },
            mountThirdLevel(parentId) {
              const VueHamburgerMenuItem = Vue.extend({
                data: () => {
                  return {
                    space: this.space,
                    homeLink: self.homeLink,
                  };
                },
                methods: {
                  closeMenu() {
                    self.$emit('close-second-level');
                    self.secondeLevel = false;
                    document.dispatchEvent(new CustomEvent('hide-third-level'));
                  },
                  selectThirdLevelHome(space) {
                    self.selectHome(space);
                  },
                },
                template: `
                  <space-panel-hamburger-navigation :space="space" :home-link="homeLink" @selectHome="selectThirdLevelHome(space)" @close-menu="closeMenu" />
                `,
              });
              new VueHamburgerMenuItem({
                i18n: new VueI18n({
                  locale: this.$i18n.locale,
                  messages: this.$i18n.messages,
                }),
                vuetify: Vue.prototype.vuetifyOptions,
                el: parentId,
              });
            },
            closeMenu() {
              document.dispatchEvent(new CustomEvent('hide-third-level'));
              self.openOrCloseDrawer();
            },
          },
          template: `
            <exo-recent-spaces-hamburger-menu-navigation @open-space-panel="openSpacePanel($event)" @close-menu="closeMenu" />
          `,
        });
      } else {
        VueHamburgerMenuItem = Vue.extend({
          data: () => {
            return {
              space: this.space,
              homeLink: this.homeLink,
            };
          },
          methods: {
            selectHome(space) {
              self.selectHome(space);
            },
            closeMenu() {
              document.dispatchEvent(new CustomEvent('hide-third-level'));
              self.openSpacePanel();
            },
          },
          template: `
            <space-panel-hamburger-navigation :space="space" :home-link="homeLink" @selectHome="selectHome(space)" @close-menu="closeMenu" />
          `,
        });
      }
      this.secondLevelVueInstance = new VueHamburgerMenuItem({
        i18n: new VueI18n({
          locale: this.$i18n.locale,
          messages: this.$i18n.messages,
        }),
        vuetify: Vue.prototype.vuetifyOptions,
        el: parentId,
      });
    },
    hideSecondeItem() {
      this.arrowIcon= 'fa-arrow-right';
      this.showItemActions = false;
      this.secondeLevel = false;
      this.secondeLevelSpacePanel = false;
      this.spacePanel = false;
    },
    openOrCloseDrawer() {
      this.isRecentSpaces = true;
      this.arrowIcon = 'fa-arrow-right';
      this.secondeLevel = !this.secondeLevel;
      if (this.secondeLevel) {
        this.secondeLevelSpacePanel = false;
        this.arrowIcon = 'fa-arrow-left';
        this.$emit('open-second-level', true);
        document.dispatchEvent(new CustomEvent('hide-space-panel'));
      } else {
        this.arrowIcon = 'fa-arrow-right';
        this.$emit('close-second-level');
      }
    },
    openSpacePanel(event) {
      let openedSpace = null;
      if (this.space) {
        openedSpace = this.space.id;
      }
      this.isRecentSpaces = false;
      this.spacePanel = false;
      if (event) {
        this.space = event;
      }
      this.spacePanel = !this.spacePanel;
      this.secondeLevelSpacePanel = !this.secondeLevelSpacePanel;
      if (this.secondeLevelSpacePanel || (openedSpace !== this.space.id && openedSpace!==null)) {
        document.dispatchEvent(new CustomEvent('space-opened', {detail: openedSpace} ));
        this.secondeLevel = false;
        this.$emit('open-second-level', true);
        this.arrowIcon = 'fa-arrow-right';
      } else {
        this.$emit('close-second-level');
      }
    },
    leftNavigationActionEvent(event,clickedItem) {
      if (this.isMobile && event) {
        event.stopPropagation();
      }
      document.dispatchEvent(new CustomEvent('space-left-navigation-action', {detail: clickedItem} ));
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
