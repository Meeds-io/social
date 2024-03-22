<template>
  <v-app>
    <div
      v-if="breadcrumbToDisplay.length"
      id="breadcrumbParent"
      class="white px-2 py-2 card-border-radius d-flex">
      <div
        v-for="(breadcrumb, index) in breadcrumbToDisplay"
        :key="index"
        :class="breadcrumbToDisplay.length === 1 && 'single-path-element' || ''"
        class="text-truncate text-body-1">
        <v-tooltip
          v-if="breadcrumb.label != ellipsis"
          max-width="300"
          bottom>
          <template #activator="{ on, attrs }">
            <div
              class="text-truncate d-inline not-clickable"
              v-bind="attrs"
              v-on="on">
              <v-btn
                :href="breadcrumb.uri"
                :target="breadcrumb.target === 'SAME_TAB' && '_self' || '_blank'"
                :disabled="!breadcrumb.uri"
                min-width="45px"
                max-width="250px"
                class="pa-0"
                text>
                <span
                  :class="index < (breadcrumbToDisplay.length - 1) && 'text-sub-title' || 'text-color'"
                  class="text-truncate text-none">
                  {{ breadcrumb.label }}
                </span>
              </v-btn>
            </div>
          </template>
          <span class="caption">
            {{ breadcrumb.label }}
          </span>
        </v-tooltip>
        <v-btn
          v-else
          disabled
          min-width="45px"
          class="pa-0 flex-shrink-1"
          text>
          <span class="text-sub-title">
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
    spaceType: 'GROUP',
    ellipsis: '...',
  }),
  computed: {
    breadcrumbToDisplay() {
      if (!this.userNodeBreadcrumbItemList || (!this.isMobile && this.userNodeBreadcrumbItemList.length <= 4) || (this.isMobile && this.userNodeBreadcrumbItemList.length === 1)) {
        return this.userNodeBreadcrumbItemList || [];
      }
      const length = this.userNodeBreadcrumbItemList.length;
      let userNodeBreadcrumbItemListToDisplay = [];
      if (!this.isMobile) {
        userNodeBreadcrumbItemListToDisplay  = [this.userNodeBreadcrumbItemList[0], ... this.userNodeBreadcrumbItemList.slice(length - 3, length)];
        userNodeBreadcrumbItemListToDisplay[1] = Object.assign({}, userNodeBreadcrumbItemListToDisplay[1], {
          label: this.ellipsis,
        });
        return userNodeBreadcrumbItemListToDisplay;
      } else if (!this.$root.noThreeDots) {
        userNodeBreadcrumbItemListToDisplay = [this.userNodeBreadcrumbItemList[0], this.userNodeBreadcrumbItemList[length - 1]];
        userNodeBreadcrumbItemListToDisplay[0] = Object.assign({}, userNodeBreadcrumbItemListToDisplay[0], {
          label: this.ellipsis,
        });
        return userNodeBreadcrumbItemListToDisplay;
      } else {
        return this.userNodeBreadcrumbItemList.slice(length - 1);
      }
    },
    isMobile() {
      return this.$vuetify.breakpoint.width < 980;
    },
  },
  created() {
    this.getCurrentNavigations();
  },
  mounted() {
    window.setTimeout(() => document.dispatchEvent(new CustomEvent('breadcrumb-app-mounted')), 50);
  },
  methods: {
    getCurrentNavigations() {
      this.$navigationService.getNavigations((!!eXo.env.portal.spaceId && `/spaces/${eXo.env.portal.spaceGroup}`) || eXo.env.portal.portalName, (!!eXo.env.portal.spaceId && this.spaceType) || this.siteType, this.scope, this.visibility, null,  eXo.env.portal.selectedNodeId, true)
        .then(navigations => {
          this.navigation = navigations &&  navigations[0] || {};
          this.userNodeBreadcrumbItemList = navigations &&  navigations[0].userNodeBreadcrumbItemList || [];
        });
    },
  }
};
</script>
