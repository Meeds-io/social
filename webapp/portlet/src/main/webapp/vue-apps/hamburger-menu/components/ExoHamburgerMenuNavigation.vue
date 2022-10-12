<template>
  <v-app
    color="transaprent"
    class="HamburgerNavigationMenu"
    flat>
    <a class="HamburgerNavigationMenuLink flex border-box-sizing" @click="openOrHideMenu">
      <div class="px-5 py-3">
        <v-icon size="24">fa-bars</v-icon>
      </div>
    </a>
    <exo-drawer
      id="HamburgerMenuNavigation"
      ref="HamburgerMenuNavigation"
      v-model="hamburgerMenu"
      :drawer-width="drawerWidth"
      :style="hamburgerMenuStyle"
      left
      eager>
      <template v-if="hamburgerMenuInitialized" slot="content">
        <v-row
          class="HamburgerMenuLevelsParent fill-height"
          no-gutters
          @mouseleave="hideSecondLevel()">
          <div
            :class="secondLevel && 'd-none d-sm-block'"
            class="HamburgerMenuFirstLevelParent border-box-sizing"
            role="navigation"
            :aria-label="$t('menu.role.navigation.first.level')">
            <v-flex v-for="contentDetail in contents" :key="contentDetail.id">
              <div :id="contentDetail.id"></div>
            </v-flex>
          </div>
          <div
            v-show="secondLevel"
            :class="[secondLevel && 'open', thirdLevel && 'd-none d-sm-block']"
            class="HamburgerMenuSecondLevelParent border-box-sizing"
            role="navigation"
            :aria-label="$t('menu.role.navigation.second.level')">
            <div id="HamburgerMenuSecondLevel"></div>
          </div>
          <div
            v-show="thirdLevel"
            :class="thirdLevel && 'open'"
            class="HamburgerMenuThirdLevelParent border-box-sizing"
            role="navigation"
            :aria-label="$t('menu.role.navigation.second.level')">
            <div id="HamburgerMenuThirdLevel"></div>
          </div>
          <span id="HamburgerMenuVisibility" class="d-none d-sm-block"></span>
        </v-row>
      </template>
    </exo-drawer>
  </v-app>
</template>
<script>
export default {
  data(){
    return {
      initializing: false,
      hamburgerMenu: false,
      hamburgerMenuInitialized: false,
      secondLevel: false,
      thirdLevel: false,
      openedSecondLevel: null,
      contents: [],
      vueChildInstances: {},
      idleTime: 20,
      isMobile: false,
      idleTimeToDisplaySecondLevel: 20,
    };
  },
  computed: {
    drawerWidth() {
      if (this.isMobile) {
        return '310';
      } 
      if (this.secondLevel) {
        if (this.thirdLevel) {
          return '930';
        } else {
          return '620';
        }
      } else {
        return '310';
      }
    },
    hamburgerMenuStyle() {
      return this.initializing ? 'left: -5000px;' : '';
    },
  },
  watch: {
    hamburgerMenuInitialized() {
      this.refreshMenu();
    },
    hamburgerMenu() {
      if (this.hamburgerMenu) {
        if (!this.hamburgerMenuInitialized) {
          this.hamburgerMenuInitialized = true;
        }
      }
    },
  },
  created() {
    document.addEventListener('exo-hamburger-menu-navigation-refresh', this.refreshMenu);
    document.addEventListener('display-third-level', (event) => {
      this.openThirdLevel(event && event.detail);
    });
    document.addEventListener('hide-third-level', () => {
      this.thirdLevel = false;
    });
    const extensions = extensionRegistry.loadExtensions('exo-hamburger-menu-navigation', 'exo-hamburger-menu-navigation-items');
    if (extensions) {
      extensions.forEach(contentDetail => delete contentDetail.loaded);
    }
    this.refreshMenu();
  },
  mounted() {
    this.isMobile = !$('#HamburgerMenuVisibility').is(':visible');
    $(window).resize(() => {
      this.isMobile = !$('#HamburgerMenuVisibility').is(':visible');
    });
    this.$root.$applicationLoaded();
  },
  methods: {
    refreshMenu() {
      const extensions = extensionRegistry.loadExtensions('exo-hamburger-menu-navigation', 'exo-hamburger-menu-navigation-items');
      if (extensions.length < eXo.portal.hamburgerMenuItems) {
        return;
      }
      extensions.sort((a, b) => a.priority - b.priority);
      this.contents = extensions;
      const contentsToLoad = this.contents.filter(contentDetail => !contentDetail.loaded);
      this.initializing = contentsToLoad.length;
      let contentsToLoadLength = contentsToLoad.length;
      const self = this;
      contentsToLoad.forEach(contentDetail => {
        if (!contentDetail.loaded) {
          window.setTimeout(() => {
            if ($(`#${contentDetail.id}`).length) {
              try {
                if (!this.vueChildInstances[contentDetail.id]) {
                  const VueHamburgerMenuItem = Vue.extend(contentDetail.vueComponent);
                  this.vueChildInstances[contentDetail.id] = new VueHamburgerMenuItem({
                    i18n: new VueI18n({
                      locale: this.$i18n.locale,
                      messages: this.$i18n.messages,
                    }),
                    vuetify: Vue.prototype.vuetifyOptions,
                    methods: {
                      $applicationLoaded() {
                        contentsToLoadLength--;
                        if (!contentsToLoadLength) {
                          self.$root.$applicationLoaded();
                        }
                      },
                    },
                    el: `#${contentDetail.id}`,
                  });
                  this.vueChildInstances[contentDetail.id].$on('open-second-level', (value) => {
                    window.setTimeout(() => {
                      this.openSecondLevel(contentDetail,value);
                    }, this.idleTimeToDisplaySecondLevel);
                  });
                  this.vueChildInstances[contentDetail.id].$on('close-second-level', () => {
                    this.thirdLevel = false;
                    this.secondLevel = false;
                  });
                }
              } finally {
                contentDetail.loaded = true;
                this.initializing --;
              }
            }
          }, this.idleTime);
        }
      });
    },
    openSecondLevel(contentDetail,value) {
      if (!contentDetail.secondLevel || !this.vueChildInstances[contentDetail.id]) {
        return;
      }
      this.secondLevel = true;
      this.thirdLevel = false;
      document.dispatchEvent(new CustomEvent('second-level-opened', {
        detail: {'contentDetail': contentDetail}
      }));

      if (this.openedSecondLevel !== contentDetail.id && this.vueChildInstances[contentDetail.id].mountSecondLevel || value) {
        this.openedSecondLevel = contentDetail.id;
        this.vueChildInstances[contentDetail.id].mountSecondLevel('.HamburgerMenuSecondLevelParent > div');
      }
    },
    openThirdLevel(contentDetail) {
      this.thirdLevel = true;
      contentDetail.component.mountThirdLevel('.HamburgerMenuThirdLevelParent > div');
    },
    hideSecondLevel() {
      this.secondLevel = false;
      this.thirdLevel = false;
      document.dispatchEvent(new CustomEvent('second-level-hidden'));
      document.dispatchEvent(new CustomEvent('hide-space-panel'));
    },
    openOrHideMenu() {
      this.hamburgerMenu = true;
    },
  },
};
</script>
