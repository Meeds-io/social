<template>
  <exo-drawer
    ref="activityLikersAndKudosDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right>
    <template slot="title">
      <div class="activityLikersDrawerTitle">
        <span class="allLikersAndKudos">{{ $t('UIActivity.label.Show_All_Likers') }} {{ likersAndKudosNumber }}</span>
        <span class="allLikers"><i class="uiIconThumbUp"></i> {{ likersNumber }}</span>
        <span class="allKudos"><i class="uiIconAward"></i> {{ kudosNumber }}</span>
      </div>
    </template>
    <template slot="content">
      <exo-likers-kudos-list-item
        v-for="liker in likers"
        :key="liker.id"
        :liker="liker"
        class="px-3">
        {{ liker.personLikeFullName }}
      </exo-likers-kudos-list-item>
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
    }
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

