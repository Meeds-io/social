<template>
  <div v-if="!retrieveExtraInformation" class="d-flex flex-nowrap position-relative">
    <exo-user-avatar
      v-for="user in usersToDisplay"
      :key="user"
      :identity="user"
      :size="iconSize"
      :extra-class="'mx-1'"
      :popover="popover"
      avatar />
    <v-avatar
      v-if="notDisplayedItems"
      :size="iconSize"
      :class="avatarOverlayPosition && 'position-relative' || 'position-absolute'"
      :style="overlayStyle"
      class="notDisplayedIdentitiesOverlay clickable"
      @click="$emit('open-detail')">
      <div class="notDisplayedIdentities d-flex align-center justify-center">
        +{{ notDisplayedItems }}
      </div>
    </v-avatar>
  </div>
  <div v-else class="d-flex flex-nowrap position-relative">
    <exo-user-avatar
      v-for="user in usersToDisplay"
      :key="user"
      :profile-id="user.userName"
      :size="iconSize"
      :extra-class="'mx-1'"
      :popover="popover"
      avatar />
    <v-avatar
      v-if="notDisplayedItems"
      :size="iconSize"
      :class="avatarOverlayPosition && 'position-relative' || 'position-absolute'"
      :style="overlayStyle"
      class="notDisplayedIdentitiesOverlay clickable"
      @click="$emit('open-detail')">
      <div class="notDisplayedIdentities d-flex align-center justify-center">
        +{{ notDisplayedItems }}
      </div>
    </v-avatar>
  </div>
</template>

<script>
export default {
  props: {
    users: {
      type: Object,
      default: () => null,
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
    }
  },
  computed: {
    usersToDisplay() {
      return this.users && this.users.slice(0, this.max);
    },
    notDisplayedItems() {
      if (this.defaultLength) {
        return this.defaultLength > this.max ? this.defaultLength - this.max : 0;
      } else {
        return this.users && this.users.length > this.max ? this.users.length - this.max : 0;
      }
    },
    overlayStyle(){
      if (!this.avatarOverlayPosition) {
        return `right: ${this.iconSize + 4}px !important`;
      } 
      return 'right: 0px !important';
    },
  },
};
</script>