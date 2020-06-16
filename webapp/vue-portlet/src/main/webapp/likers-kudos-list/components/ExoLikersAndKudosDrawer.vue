<template>
  <exo-drawer
    ref="activityLikersAndKudosDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right>
    <template slot="title">
      <div class="activityLikersDrawerTitle">
        <v-tabs v-model="tab">
          <v-tab v-if="kudosList && kudosList.length > 0" class="allLikersAndKudos text-color pr-3 pl-0" href="#tab-1">{{ $t('UIActivity.label.Show_All_Likers') }} {{ likersAndKudosNumber }}</v-tab>
          <v-tab class="allLikers pr-3 pl-0" href="#tab-2"><i class="uiIconThumbUp"></i> <span class="primary--text">{{ likersNumber }}</span></v-tab>
          <v-tab class="allKudos pr-3 pl-0" href="#tab-3"><i class="uiIconAward"></i> <span class="kudosNumberLabel">{{ kudosNumber }}</span></v-tab>
        </v-tabs>
      </div>
    </template>
    <template slot="content">
      <v-tabs-items v-model="tab">
        <v-tab-item v-if="likers && likers.length > 0" value="tab-1">
          <exo-likers-kudos-list-item
            v-for="liker in likers"
            :key="liker.id"
            :liker="liker"
            :avatar="liker.personLikeAvatarImageSource"
            :name="liker.personLikeFullName"
            class="px-3  likersList"/>
          <exo-likers-kudos-list-item
            v-for="(kudos, index) in kudosList"
            :key="index"
            :kudos="kudos"
            :avatar="kudos.senderAvatar"
            :name="kudos.senderFullName"
            class="px-3 kudosList"/>
        </v-tab-item>
        <v-tab-item value="tab-2">
          <exo-likers-kudos-list-item
            v-for="liker in likers"
            :key="liker.id"
            :liker="liker"
            :avatar="liker.personLikeAvatarImageSource"
            :name="liker.personLikeFullName"
            class="px-3 likersList"/>
        </v-tab-item>
        <v-tab-item v-if="kudosList && kudosList.length > 0" value="tab-3">
          <exo-likers-kudos-list-item
            v-for="(kudos, index) in kudosList"
            :key="index"
            :kudos="kudos"
            :avatar="kudos.senderAvatar"
            :name="kudos.senderFullName"
            class="px-3 kudosList"/>
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
    kudosNumber: {
      type: Number,
      default: 0
    },
    kudosList: {
      type: Array,
      default: null,
    },
  },
  data () {
    return {
      tab: null
    };
  },
  computed: {
    likersAndKudosNumber () {
      return this.likersNumber + this.kudosNumber;
    }
  },
  methods: {
    open() {
      this.$refs.activityLikersAndKudosDrawer.open();
    },
    cancel() {
      this.$refs.activityLikersAndKudosDrawer.close();
    },
  },
};
</script>

