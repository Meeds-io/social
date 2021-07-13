<template>
  <v-container
    id="AdministrationHamburgerNavigation"
    px-0
    py-0
    class="white d-none d-sm-block">
    <v-row v-if="navigationTree && navigationTree.length" class="mx-0 administrationTitle">
      <v-list-item @mouseover="openDrawer()" @click="openDrawer()">
        <v-list-item-icon class="mb-2 mt-3 mr-6 titleIcon"><i class="uiIcon uiIconToolbarNavItem uiAdministrationIcon"></i></v-list-item-icon>
        <v-list-item-content class="subtitle-2 titleLabel clickable">
          {{ this.$t('menu.administration.title') }}
        </v-list-item-content>
        <v-list-item-action class="my-0">
          <i class="uiIcon uiArrowRightIcon" color="grey lighten-1"></i>
        </v-list-item-action>
      </v-list-item>
    </v-row>
  </v-container>
</template>
<script>

export default {
  data() {
    return {
      vuetify: new Vuetify(eXo.env.portal.vuetifyPreset),
      navigationScope: 'ALL',
      navigationVisibilities: ['displayed'],
      navigations: [],
      embeddedTree: {
        // users and spaces
        'usersManagement': 'usersAndSpaces',
        'groupsManagement': 'usersAndSpaces',
        'membershipsManagement': 'usersAndSpaces',
        'spacesAdministration': 'usersAndSpaces',
        // Documents
        'editors': 'documents',
        'cloudStorage': 'documents',
        // content
        'siteExplorer': 'content',
        'wcmAdmin': 'content',
        // gamification
        'hook_management': 'gamification',
        'gamification/rules': 'gamification',
        'gamification/badges': 'gamification',
        'gamification/domains': 'gamification',
        // security
        'dlp-quarantine': 'security',
        'transferRules': 'security',
        'multifactor-authentication': 'security',
        // rewards
        'rewardAdministration/kudosAdministration': 'reward',
        'rewardAdministration/walletAdministration': 'reward',
        'rewardAdministration/rewardAdministration': 'reward',
        // portal
        'portalnavigation': 'portal',
        'groupnavigation': 'portal',
        'administration/pageManagement': 'portal',
        'administration/registry': 'portal',
      },
    };
  },
  computed: {
    visibilityQueryParams() {
      return this.navigationVisibilities.map(visibilityName => `visibility=${visibilityName}`).join('&');
    },
    navigationTree() {
      const navigationTree = [];
      const navigationParentObjects = {};

      let navigationsList = this.navigations.slice();
      navigationsList = this.filterDisplayedNavigations(navigationsList);
      this.computeLink(navigationsList);

      Object.keys(this.embeddedTree).forEach(embeddedTreeUri => {
        let nav = this.findNodeByUri(embeddedTreeUri, navigationsList);
        if (nav) {
          nav.displayed = true;
          const key = this.embeddedTree[embeddedTreeUri];
          nav = Object.assign({}, nav);
          nav.children = nav.children && nav.children.slice();

          if (navigationParentObjects[key]) {
            navigationParentObjects[key].children.push(nav);
          } else {
            navigationParentObjects[key] = {
              key: key,
              label: this.$t(`menu.administration.navigation.${key}`),
              children: [nav],
            };
            navigationTree.push(navigationParentObjects[key]);
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
  created() {
    // Differ retrieving data from server
    // eslint-disable-next-line no-magic-numbers
    window.setTimeout(this.retrieveAdministrationMenu, 1000);
  },
  methods: {
    retrieveAdministrationMenu() {
      if (this.navigations.length) {
        return;
      }
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/navigations/group?exclude=/spaces.*&${this.visibilityQueryParams}`, {
        method: 'GET',
        credentials: 'include',
      })
        .then(resp => resp && resp.ok && resp.json())
        .then(data => this.navigations = data || []);
    },
    mountSecondLevel(parentId) {
      this.retrieveAdministrationMenu();
      const VueHamburgerMenuItem = Vue.extend({
        data: () => {
          return {
            navigations: this.navigationTree,
          };
        },
        template: `
          <exo-administration-navigations :navigations="navigations" />
        `,
      });
      new VueHamburgerMenuItem({
        i18n: new VueI18n({
          locale: this.$i18n.locale,
          messages: this.$i18n.messages,
        }),
        vuetify: this.vuetify,
      }).$mount(parentId);
    },
    openDrawer() {
      this.$emit('open-second-level');
    },
    filterDisplayedNavigations(navigations, excludeHidden) {
      return navigations.filter(nav => {
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
