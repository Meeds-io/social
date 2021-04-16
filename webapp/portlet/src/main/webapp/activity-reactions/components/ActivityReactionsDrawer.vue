<template>
  <exo-drawer
    ref="activityReactionsDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right>
    <template slot="title">
      <div class="activityReactionsTitle">
        <v-tabs v-model="selectedTab">
          <v-tab class="allLikersAndKudos text-color pe-3 ps-0" href="#tab-1">{{ $t('UIActivity.label.Show_All_Likers') }} {{ reactionsNumber }}</v-tab>
          <v-tab class="allLikers pe-3 ps-0" href="#tab-2"><i class="uiIconThumbUp"></i> <span class="primary--text">{{ likersNumber }}</span></v-tab>
          <v-tab
            v-for="(tab, i) in enabledReactionsTabsExtensions"
            :key="i"
            :href="`#tab-${tab.order}`"
            :class="`all${tab.class}`"
            class="pe-3 ps-0">
            <i :class="tab.icon"></i>
            <span :class="`${tab.class}NumberLabel`">{{ tab.kudosNumber }}</span>
          </v-tab>
        </v-tabs>
      </div>
    </template>
    <template v-if="drawerOpened" slot="content">
      <v-tabs-items v-model="selectedTab">
        <v-tab-item value="tab-1">
          <activity-reactions-list-items
            v-for="liker in likers"
            :key="liker.id"
            :user-id="liker.likerId"
            :avatar="liker.personLikeAvatarImageSource"
            :name="liker.personLikeFullName"
            :profile-url="liker.personLikeProfileUri"
            class="px-3  likersList" />
          <div v-for="(tab, i) in enabledReactionsTabsExtensions" :key="i">
            <activity-reactions-list-items
              v-for="(item, index) in tab.reactionListItems"
              :key="index"
              :user-id="item.senderId"
              :avatar="item.senderAvatar"
              :name="item.senderFullName"
              :class="`${tab.class}List`"
              :profile-url="item.senderURL"
              class="px-3" />
          </div>
        </v-tab-item>
        <v-tab-item value="tab-2">
          <activity-reactions-list-items
            v-for="liker in likers"
            :key="liker.id"
            :user-id="liker.likerId"
            :avatar="liker.personLikeAvatarImageSource"
            :name="liker.personLikeFullName"
            :profile-url="liker.personLikeProfileUri"
            class="px-3 likersList" />
        </v-tab-item>
        <v-tab-item
          v-for="(tab, i) in enabledReactionsTabsExtensions"
          :key="i"
          :eager="true"
          :value="`tab-${tab.order}`">
          <activity-reactions-list-items
            v-for="(item, index) in tab.reactionListItems"
            :key="index"
            :user-id="item.senderId"
            :avatar="item.senderAvatar"
            :name="item.senderFullName"
            :class="`${tab.class}List`"
            :profile-url="item.senderURL"
            class="px-3" />
        </v-tab-item>
      </v-tabs-items>
    </template>
  </exo-drawer>
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
    maxItemsToShow: {
      type: Number,
      default: 0
    }
  },
  data () {
    return {
      selectedTab: null,
      drawerOpened: false,
      activityReactionsExtensions: [],
      user: {}
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
    kudosNumber() {
      return this.reactionsNumber - this.likersNumber;
    },
    enabledReactionsTabsExtensions() {
      if (!this.activityReactionsExtensions) {
        return [];
      }
      return this.activityReactionsExtensions;
    },
  },
  methods: {
    open() {
      this.drawerOpened = true;
      this.refreshReactions();
      this.$refs.activityReactionsDrawer.open();
    },
    cancel() {
      this.$refs.activityReactionsDrawer.close();
    },
    refreshReactions() {
      this.activityReactionsExtensions= [];
      const contentsToLoad = extensionRegistry.loadExtensions('activity-reactions', 'activity-reactions') || [];
      // eslint-disable-next-line eqeqeq
      this.activityReactionsExtensions = contentsToLoad.filter(contentDetail => contentDetail.activityId == this.activityId );
      this.$emit('reactions', this.kudosNumber);
    },
  },
};
</script>
