<template>
  <div>
    <exo-drawer
      ref="activityReactionsDrawer"
      body-classes="hide-scroll decrease-z-index-more"
      right>
      <template slot="title">
        <div class="activityLikersDrawerTitle">
          <v-tabs v-model="tab">
            <v-tab class="allLikersAndKudos text-color pr-3 pl-0" href="#tab-1">{{ $t('UIActivity.label.Show_All_Likers') }} {{ reactionsNumber }}</v-tab>
            <v-tab class="allLikers pr-3 pl-0" href="#tab-2"><i class="uiIconThumbUp"></i> <span class="primary--text">{{ likersNumber }}</span></v-tab>
            <v-tab v-for="(tab, i) in enabledReactionsTabsExtensions"
                   :key="i"
                   :href="`#tab-${tab.order}`"
                   :class="`all${tab.class}`"
                   class="pr-3 pl-0">
              <i :class="tab.icon"></i>
              <span :class="`${tab.class}NumberLabel`">{{ tab.kudosNumber }}</span>
            </v-tab>
          </v-tabs>
        </div>
      </template>
      <template slot="content">
        <v-tabs-items v-model="tab">
          <v-tab-item value="tab-1">
            <activity-reactions-list-items
              v-for="liker in likers"
              :key="liker.id"
              :liker="liker"
              :avatar="liker.personLikeAvatarImageSource"
              :name="liker.personLikeFullName"
              class="px-3  likersList"/>
            <div v-for="(tab, i) in enabledReactionsTabsExtensions" :key="i">
              <activity-reactions-list-items
                v-for="(item, index) in tab.reactionListItems"
                :key="index"
                :liker="item"
                :avatar="item.senderAvatar"
                :name="item.senderFullName"
                :class="`${tab.class}List`"
                class="px-3"/>
            </div>

          </v-tab-item>
          <v-tab-item value="tab-2">
            <activity-reactions-list-items
              v-for="liker in likers"
              :key="liker.id"
              :liker="liker"
              :avatar="liker.personLikeAvatarImageSource"
              :name="liker.personLikeFullName"
              class="px-3 likersList"/>
          </v-tab-item>
          <v-tab-item v-for="(tab, i) in enabledReactionsTabsExtensions" :key="i" :eager="true" :value="`tab-${tab.order}`">
            <activity-reactions-list-items
              v-for="(item, index) in tab.reactionListItems"
              :key="index"
              :liker="item"
              :avatar="item.senderAvatar"
              :name="item.senderFullName"
              :class="`${tab.class}List`"
              class="px-3"/>
          </v-tab-item>
        </v-tabs-items>
      </template>
    </exo-drawer>
    <p class="reactionsNumber mb-0 pl-2 align-self-end caption">{{ reactionsNumber }} {{ $t('UIActivity.label.Reactions_Number') }}</p>
  </div>
</template>

<script>
export default {
  props: {
    likers: {
      type: Array,
      default: null,
    },
    likersNumber: {
      type: Number,
      default: 0
    },
    extensionsReactions: {
      type: Array,
      default: null
    },
    activityId: {
      type: String,
      default: () => ''
    },
  },
  data () {
    return {
      tab: null,
      activityReactionsExtensions: [],
    };
  },
  computed: {
    reactionsNumber () {
      let allReactionsNumber = this.likersNumber;
      this.enabledReactionsTabsExtensions.forEach(item => {
        allReactionsNumber += item.reactionListItems.length;
      });
      return allReactionsNumber;
    },
    enabledReactionsTabsExtensions() {
      if (!this.activityReactionsExtensions) {
        return [];
      }
      return this.activityReactionsExtensions;
    },
  },
  created() {
    this.refreshReactions();
  },
  methods: {
    open() {
      this.$refs.activityReactionsDrawer.open();
      this.refreshReactions();
    },
    cancel() {
      this.$refs.activityReactionsDrawer.close();
    },
    refreshReactions() {
      const contentsToLoad = extensionRegistry.loadExtensions('activity-reactions', 'activity-kudos-reactions') || [];
      // eslint-disable-next-line eqeqeq
      this.activityReactionsExtensions = contentsToLoad.filter(contentDetail => contentDetail.activityId == this.activityId );
    },
  },
};
</script>
