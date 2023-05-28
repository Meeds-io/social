<template>
  <v-hover>
    <v-card
      v-if="!retrieveExtraInformation"
      slot-scope="{ hover }"
      :class="hover && clickable && 'light-grey-background'"
      :ripple="clickable"
      class="d-flex flex-nowrap position-relative"
      color="transparent"
      flat
      @click="$emit('open-detail')">
      <exo-user-avatar
        v-for="(user, index) in usersToDisplay"
        :key="`${user}_${index}`"
        :identity="user"
        :size="iconSize"
        :extra-class="'mx-1'"
        :popover="popover"
        :clickable="clickable"
        avatar
        @avatar-click="$emit('open-detail', user)" />
      <v-btn
        v-if="showMoreAvatarsNumber"
        :height="iconSize"
        :width="iconSize"
        fab
        depressed>
        <v-avatar
          :size="iconSize"
          class="notDisplayedIdentitiesOverlay"
          @click="$emit('open-detail')">
          <div class="notDisplayedIdentities d-flex align-center justify-center caption">
            +{{ showMoreAvatarsNumber }}
          </div>
        </v-avatar>
      </v-btn>
    </v-card>
    <v-card
      v-else
      slot-scope="{ hover }"
      :class="hover && clickable && 'light-grey-background'"
      :ripple="clickable"
      class="d-flex flex-nowrap position-relative"
      color="transparent"
      flat
      @click="$emit('open-detail')">
      <exo-user-avatar
        v-for="(user, index) in usersToDisplay"
        :key="`${user}_${index}`"
        :profile-id="user.userName"
        :size="iconSize"
        :extra-class="'mx-1'"
        :popover="popover"
        :clickable="clickable"
        avatar
        @avatar-click="$emit('open-detail', user)" />
      <v-btn
        v-if="showMoreAvatarsNumber"
        :height="iconSize"
        :width="iconSize"
        fab
        depressed>
        <v-avatar
          :size="iconSize"
          class="notDisplayedIdentitiesOverlay"
          @click="$emit('open-detail')">
          <div class="notDisplayedIdentities d-flex align-center justify-center caption">
            +{{ showMoreAvatarsNumber }}
          </div>
        </v-avatar>
      </v-btn>
    </v-card>
  </v-hover>
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
  },
  computed: {
    usersToDisplay() {
      return this.users && this.users.slice(0, this.max);
    },
    totalUsersCount() {
      if (this.defaultLength) {
        return this.defaultLength;
      } else {
        return this.users && this.users.length;
      }
    },
    remainingAvatarsCount() {
      return this.totalUsersCount > this.max ? this.totalUsersCount - this.max : 0;
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
  },
};
</script>