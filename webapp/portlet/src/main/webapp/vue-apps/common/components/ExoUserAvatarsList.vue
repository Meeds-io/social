<template>
  <div class="d-flex flex-nowrap">
    <exo-user
      v-for="user in usersToDisplay"
      :key="user"
      :identity="user"
      :size="iconSize"
      :class="avatarOverlayPosition && 'mx-1' || ''"
      :popover="popover"
      avatar />
    <v-avatar
      v-if="notDisplayedItems"
      :size="iconSize"
      :tiptip="false"
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
  data() {
    return {
      spaceManagers: [],
    };
  },
  computed: {
    usersToDisplay() {
      if (this.retrieveExtraInformation) {
        return this.spaceManagers && this.spaceManagers.slice(0, this.max);
      } else {
        return this.users && this.users.slice(0, this.max);
      }
    },
    notDisplayedItems() {
      return this.users && this.users.length > this.max ? this.users.length - this.max : 0;
    },
    overlayStyle(){
      if (this.avatarOverlayPosition) {
        return `right: ${this.iconSize + 4}px !important`;
      } 
      return '';
    },
  },
  created() {
    if (this.retrieveExtraInformation) {
      this.users.forEach(user => {
        this.$userService.getUser(user.userName)
          .then(item => {
            this.spaceManagers.push(item);
          });
      });
    }
  }
};
</script>