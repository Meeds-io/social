<template>
  <v-container 
    id="AdministrationHamburgerNavigation"
    px-0
    py-0
    class="white">
    <v-list-item-icon
      v-if="!displaySequentially"
      class="backToMenu ma-2 icon-default-color justify-center"
      @click="$emit('close')">
      <v-icon class="fas fa-arrow-left" small />
    </v-list-item-icon>
    <v-row v-if="navigations && navigations.length" class="mx-0">
      <v-list 
        shaped 
        dense 
        min-width="90%"
        class="pb-0">
        <v-list-item-group>
          <template v-for="nav in navigationTree">
            <administration-menu-item :key="nav.key" :menu-item="nav" />
          </template>
        </v-list-item-group>
      </v-list>
    </v-row>
  </v-container>
</template>

<script>
export default {
  props: {
    navigations: {
      type: Array,
      default: () => [],
    },
    categories: {
      type: Object,
      default: () => ({}),
    },
    displaySequentially: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    sortedEmbeddedNavigationTree() {
      return this.categories && this.categories.navs && Object.keys(this.categories.navs)
        .sort((nav1, nav2) => {
          const cat1 = this.categories.navs[nav1];
          const cat2 = this.categories.navs[nav2];
          return cat1 === cat2 ? this.categories.urisOrder[nav1] - this.categories.urisOrder[nav2]
            : this.categories.categoriesOrder[cat1] - this.categories.categoriesOrder[cat2];
        }) || [];
    },
    navigationTree() {
      const navigationTree = [];
      const navigationParentObjects = {};

      let navigationsList = JSON.parse(JSON.stringify(this.navigations));
      navigationsList = this.filterDisplayedNavigations(navigationsList);
      this.computeLink(navigationsList);

      this.sortedEmbeddedNavigationTree.forEach(categoryUri => {
        let nav = this.findNodeByUri(categoryUri, navigationsList);
        if (nav) {
          nav.displayed = true;
          const catName = this.categories.navs[categoryUri];
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
  },
  methods: {
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

