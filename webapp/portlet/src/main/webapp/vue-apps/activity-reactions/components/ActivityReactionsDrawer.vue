<template>
  <exo-drawer
    ref="activityReactionsDrawer"
    disable-pull-to-refresh
    right
    fixed>
    <template slot="title">
      {{ $t('UIActivity.label.reactions') }}
    </template>
    <template v-if="drawerOpened" slot="content">
      <div>
        <v-tabs
          slider-size="4"
          v-model="selectedTab">
          <v-tab
            v-for="(tab, i) in enabledReactionsTabsExtensionsToDisplay"
            :key="i"
            :href="`#tab-${tab.componentOptions.order}`"
            class="text-capitalize">
            <span>  {{ $t(`UIActivity.label.${tab.componentOptions.reactionLabel}`) }}({{ tab.componentOptions.numberOfReactions }})</span>
          </v-tab>
        </v-tabs>
        <v-divider dark />
      </div>
      <v-tabs-items v-model="selectedTab" class="pt-3">
        <v-tab-item
          v-for="(tab, i) in enabledReactionsTabsExtensionsToDisplay"
          :key="i"
          :eager="true"
          :value="`tab-${tab.componentOptions.order}`">
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
      return this.activityReactionsExtensions;
    },
    enabledReactionsTabsExtensionsToDisplay() {
      return this.enabledReactionsTabsExtensions && this.enabledReactionsTabsExtensions.slice().filter(extension => extension.componentOptions.numberOfReactions > 0) || [];
    },
    reactionParams() {
      return {
        activityId: this.activityId,
      };
    },
  },
  created() {
    document.addEventListener('update-reaction-extension' ,
      (event) => {
        this.updateReaction(event);
      });
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
        const extension = this.enabledReactionsTabsExtensions[extensionIndex];
        extension.componentOptions.numberOfReactions = event.detail.numberOfReactions;
        this.enabledReactionsTabsExtensions.splice(extensionIndex,1,extension);
      }
    }
  },
};
</script>
