<template>
  <exo-drawer
    ref="activityReactionsDrawer"
    disable-pull-to-refresh
    right
    fixed
    @closed="selectedTab= null">
    <template slot="title">
      {{ $t('UIActivity.label.reactions') }}
    </template>
    <template v-if="drawerOpened" slot="content">
      <div>
        <v-tabs
          slider-size="4"
          fixed-tabs
          v-model="selectedTab">
          <v-tab
            v-for="(tab, i) in enabledReactionsTabsExtensions"
            :key="i"
            :href="`#${tab.componentOptions.id}`"
            class="text-capitalize">
            <span>{{ $t(`${tab.componentOptions.reactionLabel}`) }} ({{ tab.componentOptions.numberOfReactions }})</span>
          </v-tab>
        </v-tabs>
        <v-divider dark />
      </div>
      <v-tabs-items v-model="selectedTab" class="pt-3">
        <v-tab-item
          v-for="(tab, i) in enabledReactionsTabsExtensions"
          :key="i"
          :eager="true"
          :value="`${tab.componentOptions.id}`">
          <extension-registry-component
            :component="tab"
            :params="reactionParams" />
        </v-tab-item>
      </v-tabs-items>
    </template>
    <template v-if="hasMoreLikers" slot="footer">
      <v-btn
        :loading="loading"
        :disabled="loading"
        block
        class="btn pa-0"
        @click="loadMore">
        {{ $t('Search.button.loadMore') }}
      </v-btn>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  props: {
    extensionsReactions: {
      type: Array,
      default: null
    },
    activityId: {
      type: String,
      default: () => ''
    },
    maxItemsToShow: {
      type: Number,
      default: 0
    }
  },
  data () {
    return {
      lastLoadedActivityId: null,
      limit: 10,
      selectedTab: null,
      drawerOpened: false,
      activityReactionsExtensions: [],
      user: {}
    };
  },
  computed: {
    enabledReactionsTabsExtensions() {
      if (!this.activityReactionsExtensions) {
        return [];
      }
      return this.activityReactionsExtensions.slice().sort((extension1, extension2) => {
        return extension1.componentOptions.rank - extension2.componentOptions.rank;
      });
    },
    reactionParams() {
      return {
        activityId: this.activityId,
      };
    },
  },
  created() {
    document.addEventListener('update-reaction-extension' , this.updateReaction);
    document.addEventListener(`open-reaction-drawer-selected-tab-${this.activityId}` , this.openSelectedTab);
  },
  methods: {
    open() {
      this.refreshReactions();
      this.$refs.activityReactionsDrawer.open();
      this.drawerOpened = true;
      if (this.lastLoadedActivityId !== this.activityId) {
        this.limit = 10;
        this.lastLoadedActivityId = this.activityId;
      }
    },
    openSelectedTab(event) {
      if (event && event.detail && event.detail.activityId && event.detail.activityId === this.activityId) {
        this.selectedTab = event.detail.tab;
        this.open();
      }
    },
    loadMore() {
      this.limit += 10;
    },
    cancel() {
      this.$refs.activityReactionsDrawer.close();
    },
    refreshReactions() {
      this.activityReactionsExtensions= [];
      this.activityReactionsExtensions = extensionRegistry.loadComponents('ActivityReactions') || [];
    },
    updateReaction(event) {
      if (event && event.detail) {
        const extensionIndex = this.enabledReactionsTabsExtensions.findIndex(extension => extension.componentOptions.id === event.detail.type);
        if (extensionIndex >= 0 ) {
          const extension = this.enabledReactionsTabsExtensions[extensionIndex];
          extension.componentOptions.numberOfReactions = event.detail.numberOfReactions;
          this.enabledReactionsTabsExtensions.splice(extensionIndex, 1, extension);
        }
      }
    }
  },
};
</script>
