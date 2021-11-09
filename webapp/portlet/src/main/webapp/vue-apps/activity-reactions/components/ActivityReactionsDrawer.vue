<template>
  <exo-drawer
    ref="activityReactionsDrawer"
    disable-pull-to-refresh
    right
    fixed>
    <template slot="title">
      {{ $t('UIActivity.label.Reactions_Number') }}
    </template>
    <template v-if="drawerOpened" slot="content">
      <div>
        <v-tabs
          fixed-tabs
          slider-size="4"
          v-model="selectedTab">
          <v-tab
            v-for="(tab, i) in enabledReactionsTabsExtensions"
            :key="i"
            :href="`#tab-${tab.componentOptions.order}`"
            class="text-capitalize">
            <span>{{ tab.componentOptions.reactionType }} ({{ numberOfReactions(tab) }})</span>
          </v-tab>
        </v-tabs>
        <v-divider dark />
      </div>
      <v-tabs-items v-model="selectedTab" class="pt-3">
        <v-tab-item
          v-for="(tab, i) in enabledReactionsTabsExtensions"
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
    reactionParams() {
      return {
        activityId: this.activityId,
      };
    },
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
    numberOfReactions(tab) {
      return tab && tab.componentOptions.vueComponent.$options.computed.numberOfReactions();
    },
    refreshReactions() {
      this.activityReactionsExtensions= [];
      const componentsToLoad = extensionRegistry.loadComponents('ActivityReactions') || [];
      // eslint-disable-next-line eqeqeq
      this.activityReactionsExtensions = componentsToLoad;
    },
  },
};
</script>
