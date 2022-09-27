<template>
  <v-container
    id="AdministrationHamburgerNavigation"
    px-0
    py-0
    class="white d-none d-sm-block">
    <v-row v-if="navigationTree && navigationTree.length" class="mx-0 administrationTitle">
      <v-list-item 
        @mouseover="showItemActions = true"
        @mouseleave="showItemActions = false">
        <v-list-item-icon class="mb-2 mt-3 mr-6 titleIcon"><i class="uiIcon uiIconToolbarNavItem uiAdministrationIcon"></i></v-list-item-icon>
        <v-list-item-content class="subtitle-2">
          {{ this.$t('menu.administration.title') }}
        </v-list-item-content>
        <v-list-item-action v-if="toggleArrow" class="my-0">
          <v-btn icon @click="openOrCloseDrawer()">
            <v-icon class="me-0 pa-2 icon-default-color clickable" small>
              {{ arrowIconClass }} 
            </v-icon>
          </v-btn>
        </v-list-item-action>
      </v-list-item>
    </v-row>
  </v-container>
</template>
<script>

export default {
  data() {
    return {
      navigationScope: 'ALL',
      navigationVisibilities: ['displayed'],
      loading: false,
      navigations: [],
      embeddedTree: {},
      secondeLevel: false,
      showItemActions: false,
      arrowIcon: 'fa-arrow-right'
    };
  },
  computed: {
    visibilityQueryParams() {
      return this.navigationVisibilities.map(visibilityName => `visibility=${visibilityName}`).join('&');
    },
    sortedEmbeddedNavigationTree() {
      return this.embeddedTree && this.embeddedTree.navs && Object.keys(this.embeddedTree.navs)
        .sort((nav1, nav2) => {
          const cat1 = this.embeddedTree.navs[nav1];
          const cat2 = this.embeddedTree.navs[nav2];
          return cat1 === cat2 ? this.embeddedTree.urisOrder[nav1] - this.embeddedTree.urisOrder[nav2]
            : this.embeddedTree.categoriesOrder[cat1] - this.embeddedTree.categoriesOrder[cat2];
        }) || [];
    },
    navigationTree() {
      const navigationTree = [];
      const navigationParentObjects = {};

      let navigationsList = JSON.parse(JSON.stringify(this.navigations));
      navigationsList = this.filterDisplayedNavigations(navigationsList);
      this.computeLink(navigationsList);

      this.sortedEmbeddedNavigationTree.forEach(embeddedTreeUri => {
        let nav = this.findNodeByUri(embeddedTreeUri, navigationsList);
        if (nav) {
          nav.displayed = true;
          const catName = this.embeddedTree.navs[embeddedTreeUri];
          nav = Object.assign({}, nav);
          nav.children = nav.children && nav.children.slice();

          if (navigationParentObjects[catName]) {
            navigationParentObjects[catName].children.push(nav);
          } else {
            navigationParentObjects[catName] = {
              key: catName,
              label: this.$t(`menu.administration.navigation.${catName}`),
              children: [nav],
            };
            navigationTree.push(navigationParentObjects[catName]);
          }
        }
      });

      navigationsList = this.filterDisplayedNavigations(navigationsList, true);
      const key = 'other';

      navigationsList.forEach(nav => {
        if (navigationParentObjects[key]) {
          navigationParentObjects[key].children.push(nav);
        } else {
          navigationParentObjects[key] = {
            key: key,
            label: this.$t(`menu.administration.navigation.${key}`),
            children: [nav],
          };
          navigationTree.unshift(navigationParentObjects[key]);
        }
      });
      return navigationTree;
    },
    arrowIconClass() {
      return this.arrowIcon;
    },
    toggleArrow() {
      return this.secondeLevel || this.showItemActions;
    }
  },
  created() {
    Promise.resolve(this.retrieveAdministrationMenu())
      .finally(() => this.$root.$applicationLoaded());

    document.addEventListener('second-level-hidden', () => {
      this.hideSecondeItem();
    });
    document.addEventListener('second-level-opened', (event) => {
      if ( event && event.detail && event.detail.contentDetail.id !== 'HamburgerMenuNavigationAdministration') {
        this.hideSecondeItem();
      }
    });
  },
  methods: {
    retrieveAdministrationMenu() {
      if (this.navigations.length) {
        return;
      }
      const cachedNavigations = window.sessionStorage && window.sessionStorage.getItem(`Administration_Navigations_${eXo.env.server.sessionId}`);
      if (cachedNavigations) {
        this.$nextTick().then(() => this.navigations = JSON.parse(cachedNavigations));
      }

      this.loading = true;
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/navigations/group?exclude=/spaces.*&${this.visibilityQueryParams}`, {
        method: 'GET',
        credentials: 'include',
      })
        .then(resp => resp && resp.ok && resp.json())
        .then(data => {
          const navigations = data || [];
          try {
            window.sessionStorage.setItem(`Administration_Navigations_${eXo.env.server.sessionId}`, JSON.stringify(navigations));
          } catch (e) {
            // Expected Quota Exceeded Error
          }
          this.navigations = navigations;
        })
        .then(() => fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/navigations/categories`, {
          method: 'GET',
          credentials: 'include',
        }))
        .then(resp => resp && resp.ok && resp.json())
        .then(data => this.embeddedTree = data || {})
        .finally(() => {
          this.loading = false;
        });
    },
    mountSecondLevel(parentId) {
      const VueHamburgerMenuItem = Vue.extend({
        data: () => {
          return {
            navigations: this.navigationTree,
            loading: this.loading,
          };
        },
        template: `
          <exo-administration-navigations :navigations="navigations" :loading="loading" />
        `,
      });
      new VueHamburgerMenuItem({
        i18n: new VueI18n({
          locale: this.$i18n.locale,
          messages: this.$i18n.messages,
        }),
        vuetify: Vue.prototype.vuetifyOptions,
      }).$mount(parentId);
    },
    openOrCloseDrawer() {
      this.secondeLevel = !this.secondeLevel;
      if (this.secondeLevel) {
        this.arrowIcon = 'fa-arrow-left';
        this.$emit('open-second-level');
      } else {
        this.arrowIcon = 'fa-arrow-right';
        this.$emit('close-second-level');
      }
    },
    hideSecondeItem() {
      this.arrowIcon= 'fa-arrow-right';
      this.showItemActions = false;
      this.secondeLevel = false;
    },
    filterDisplayedNavigations(navigations, excludeHidden) {
      return navigations
        .filter(nav => {
          if (nav.children) {
            nav.children = this.filterDisplayedNavigations(nav.children);
          }
          // eslint-disable-next-line no-extra-parens
          return !nav.displayed && (!excludeHidden || nav.visibility !== 'HIDDEN') && (nav.pageKey || (nav.children && nav.children.length));
        });
    },
    computeLink(navigations) {
      navigations.forEach(nav => {
        if (nav.children) {
          this.computeLink(nav.children);
        }
        const uriPart = nav.siteKey.name.replace(/\//g, ':');
        nav.link = `${eXo.env.portal.context}/g/${uriPart}/${nav.uri}`;
      });
    },
    findNodeByUri(uri, navigations) {
      for (const index in navigations) {
        const nav = navigations[index];
        if (nav.uri === uri) {
          return nav;
        } else if (nav.children) {
          const result = this.findNodeByUri(uri, nav.children);
          if (result) {
            return result;
          }
        }
      }
    },
  }
};
</script>
