<template>
  <div
    :class="extraClass"
    class="d-flex flex-nowrap align-center border-box-sizing">
    <template v-for="(user, index) in usersToDisplay">
      <div
        v-if="!retrieveExtraInformation"
        :key="`${user}_${index}`"
        :ripple="clickable"
        color="transparent"
        @click="$emit('open-detail')">
        <v-hover v-slot="{ hover }">
          <exo-user-avatar
            :identity="user"
            :size="iconSize"
            :extra-class="parentClass"
            :popover="popover"
            :clickable="clickable"
            :compact="compact"
            :margin-left="index > 0 && marginLeft || ''"
            :class="{ 'mt-n1 z-index-two': hover && compact }"
            avatar />
        </v-hover>
      </div>
      <div
        v-else
        :key="`${user}_${index}`"
        :ripple="clickable"
        color="transparent py-1 border-box-sizing"
        @click="$emit('open-detail')">
        <v-hover v-slot="{ hover }">
          <exo-user-avatar
            :profile-id="user.userName"
            :size="iconSize"
            :extra-class="parentClass"
            :popover="popover"
            :clickable="clickable"
            :compact="compact"
            :margin-left="index > 0 && marginLeft || ''"
            :class="{ 'mt-n1 z-index-two': hover && compact }"
            avatar />
        </v-hover>
      </div>
    </template>
    <v-btn
      v-if="showMoreAvatarsNumber"
      :height="iconSize"
      :width="iconSize"
      :class="{ 'mt-n1 z-index-two': compact && showAnimation, 'ml-n4': compact }"
      fab
      depressed
      @click="$emit('open-detail')"
      @mouseover="showAnimation = true"
      @mouseleave="showAnimation = false">
      <v-avatar
        :size="iconSize"
        :class="{ 'border-white content-box-sizing ': compact }"
        class="notDisplayedIdentitiesOverlay">
        <div class="notDisplayedIdentities d-flex align-center justify-center subtitle-2 white--text font-weight-bold z-index-one text-center">
          +{{ showMoreAvatarsNumber }}
        </div>
      </v-avatar>
    </v-btn>
  </div>
</template>

<script>
export default {
  props: {
    users: {
      type: Array,
      default: () => null,
    },
    clickable: {
      type: Boolean,
      default: () => false,
    },
    max: {
      type: Number,
      default: () => 0,
    },
    defaultLength: {
      type: Number,
      default: () => 0,
    },
    iconSize: {
      type: Number,
      default: () => 37,
    },
    avatarOverlayPosition: {
      type: Boolean,
      default: () => false,
    },
    popover: {
      type: Boolean,
      default: () => true
    },
    retrieveExtraInformation: {
      type: Boolean,
      default: () => false
    },
    extraClass: {
      type: String,
      default: () => '',
    },
    compact: {
      type: Boolean,
      default: () => false
    },
    allowAnimation: {
      type: Boolean,
      default: () => false,
    },
    marginLeft: {
      type: String,
      default: () => '',
    },
  },
  data() {
    return {
      showAnimation: false
    };
  },
  computed: {
    usersToDisplay() {
      return this.users && this.users.slice(0, this.max);
    },
    displayedUsersCount() {
      return this.usersToDisplay?.length || 0;
    },
    totalUsersCount() {
      if (this.defaultLength) {
        return this.defaultLength;
      } else {
        return this.users && this.users.length;
      }
    },
    remainingAvatarsCount() {
      return this.totalUsersCount > this.displayedUsersCount ? this.totalUsersCount - this.displayedUsersCount : 0;
    },
    showMoreAvatarsNumber() {
      return this.remainingAvatarsCount > 99 ? 99 : this.remainingAvatarsCount;
    },
    overlayStyle() {
      if (!this.avatarOverlayPosition) {
        return `right: ${this.iconSize + 4}px !important`;
      } 
      return 'right: 0px !important';
    },
    parentClass() {
      return `${this.compact && 'transition-2s'} mx-1`;
    },
    displayAnimation() {
      return this.showAnimation;
    }
  },
};
</script>
