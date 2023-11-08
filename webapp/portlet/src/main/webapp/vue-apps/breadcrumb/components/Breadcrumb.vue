<template>
  <v-app>
    <v-main class="white px-4 py-4">
      <div v-if="breadcrumbToDisplay.length" class="d-flex">
        <div
          v-for="(breadcrumb, index) in breadcrumbToDisplay"
          :key="index"
          :class="breadcrumbToDisplay.length === 1 && 'single-path-element' || ''"
          class="text-truncate">
          <v-tooltip
            v-if="breadcrumb.label != ellipsis"
            max-width="300"
            bottom>
            <template #activator="{ on, attrs }">
              <v-btn
                min-width="45px"
                class="pa-0 flex-shrink-1 text-truncate "
                :class="breadcrumb.uri && 'clickable' || ' not-clickable '"
                text
                v-bind="attrs"
                v-on="on"
                :href="index < breadcrumbToDisplay.length - 1 && breadcrumb.uri || null"
                :target="breadcrumb.target === 'SAME_TAB' && '_self' || '_blank'">
                <a
                  class="caption text-truncate text-h6 text-capitalize"
                  :class="index === breadcrumbToDisplay.length - 1 && ' dark-grey-color ' || (breadcrumb.uri && ' text-sub-title ' || ' text-light-color not-clickable ') ">
                  {{ breadcrumb.label }}
                </a>
              </v-btn>
            </template>
            <span class="caption breadcrumbName">
              {{ breadcrumb.label }}
            </span>
          </v-tooltip>
          <v-btn
            v-else
            min-width="45px"
            class="pa-0 flex-shrink-1 not-clickable"
            text>
            <span class="text-light-color not-clickable">
              {{ breadcrumb.label }}
            </span>
          </v-btn>
          <v-icon
            v-if="index < breadcrumbToDisplay.length-1"
            size="14"
            class="px-2">
            fa-chevron-right
          </v-icon>
        </div>
      </div>
    </v-main>
  </v-app>
</template>
<script>
export default {
  data: () => ({
    navigation: null,
    userNodeBreadcrumbItemList: null,
    scope: 'SINGLE',
    visibility: ['displayed', 'temporal'],
    siteType: 'PORTAL',
    ellipsis: '...',
  }),
  computed: {
    breadcrumbToDisplay() {
      if (!this.userNodeBreadcrumbItemList || this.userNodeBreadcrumbItemList.length <= 4) {
        return this.userNodeBreadcrumbItemList || [];
      } else {
        const length = this.userNodeBreadcrumbItemList.length;
        const userNodeBreadcrumbItemListToDisplay = [this.userNodeBreadcrumbItemList[0], ... this.userNodeBreadcrumbItemList.slice(length - 3, length)];
        userNodeBreadcrumbItemListToDisplay[1] = Object.assign({}, userNodeBreadcrumbItemListToDisplay[1], {
          label: this.ellipsis,
        });
        return userNodeBreadcrumbItemListToDisplay;
      }
    },
  },
  created() {
    this.getCurrentNavigations();
  },
  methods: {
    getCurrentNavigations() {
      this.$navigationService.getNavigations(eXo.env.portal.portalName, this.siteType, this.scope, this.visibility, null,  eXo.env.portal.selectedNodeId, true)
        .then(navigations => {
          this.navigation = navigations &&  navigations[0] || {};
          this.userNodeBreadcrumbItemList = navigations &&  navigations[0].userNodeBreadcrumbItemList || [];
        });
    },
  }
};
</script>
