<template>
  <v-app v-if="displaySpaceNavigations" class="spaceMenuParent white">
    <v-footer 
      v-if="isMobile" 
      class="spaceButtomNavigation white">
      <v-slide-group>
        <v-bottom-navigation
          :value="selectedNavigationUri"
          grow
          color="tertiary"
          background-color="transparent"
          class="spaceButtomNavigationParent"
          flat>
          <space-menu-item
            v-for="nav in navigations"
            :key="nav.id"
            :navigation="nav" />
        </v-bottom-navigation>
      </v-slide-group>
    </v-footer>    
    <v-tabs
      v-else
      :value="selectedNavigationUri"
      active-class="SelectedTab"
      class="mx-auto"
      show-arrows
      center-active
      slider-size="4">
      <v-tab
        v-for="nav in navigations"
        :key="nav.id"
        :value="nav.id"
        :href="nav.uri"
        @click="openUrl(nav.uri, nav?.target)"
        class="spaceNavigationTab">
        {{ nav.label }}
      </v-tab>
    </v-tabs>
  </v-app>
</template>

<script>

export default {
  props: {
    navigations: {
      type: Array,
      default: () => [],
    },
    selectedNavigationUri: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    parentScrollableSelector: '.site-scroll-parent',
  }),
  computed: {
    displaySpaceNavigations() {
      return this.navigations && this.navigations.length;
    },
    isMobile() {
      return this.$vuetify.breakpoint.sm || this.$vuetify.breakpoint.xs;
    },
  },
  watch: {
    displaySpaceNavigations() {
      this.computedSiteBodyMargin();
    },
    selectedNavigationUri() {
      this.refreshWindowSize();
    },
    navigations() {
      this.refreshWindowSize();
    },
  },
  created() {
    document.addEventListener('refreshSpaceNavigations', () => {
      this.$spaceService.getSpaceNavigations(eXo.env.portal.spaceId)
        .then(data => {
          // Compute URI of nodes of old navigation
          if (data && data.length) {
            data.forEach(nav => {
              const oldNav = this.navigations.find(oldNav => oldNav.id === nav.id);
              if (oldNav) {
                nav.uri = oldNav.uri;
                nav.target = oldNav.target;
              } else if (nav.uri && nav.uri.indexOf('/') >= 0) {
                nav.uri = nav.uri.split('/')[1];
                nav.target = 'SAME_TAB';
              }
            });
            this.navigations = data;
          }
          return this.$nextTick();
        })
        .then(() => this.$root.$emit('application-loaded'));
    });
  },
  mounted() {
    this.$root.$applicationLoaded();
    this.computedSiteBodyMargin();
  },
  methods: {
    computedSiteBodyMargin() {
      if (this.isMobile && this.displaySpaceNavigations) {
        window.setTimeout(() => {
          $(this.parentScrollableSelector).css('margin-bottom', '70px');
        }, 200);
      } else {
        $(this.parentScrollableSelector).css('margin-bottom', '');
      }
      this.refreshWindowSize();
    },
    refreshWindowSize() {
      this.$nextTick().then(() => {
        window.setTimeout(() => window.dispatchEvent(new Event('resize')), 200);
      });
    },
    openUrl(url, target) {
      target = target === 'SAME_TAB' && '_self' || '_blank' ;
      if (!url.match(/^(https?:\/\/|javascript:|\/portal\/)/) && this.isValidUrl(url) ) {
        url = `//${url}`;
      } else if (url.match(/^(\/portal\/)/)) {
        url = `${window.location.origin}${url}`;
      }
      window.open(url, target);
    },
    isValidUrl(str) {
      const pattern = new RegExp(
        '^([a-zA-Z]+:\\/\\/)?' +
      '((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.)+[a-z]{2,}|' +
      '((\\d{1,3}\\.){3}\\d{1,3}))' +
      '(\\:\\d+)?(\\/[-a-z\\d%_.~+]*)*' +
      '(\\?[;&a-z\\d%_.~+=-]*)?' +
      '(\\#[-a-z\\d_]*)?$',
        'i'
      );
      return pattern.test(str);
    }
  },
};
</script>