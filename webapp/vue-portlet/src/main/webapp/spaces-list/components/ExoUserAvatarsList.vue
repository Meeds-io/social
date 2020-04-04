<template>
  <div class="flex-nowrap">
    <exo-user-avatar
      v-for="user in usersToDisplay"
      :key="user"
      :username="user.username"
      :title="user.fullname"
      :size="iconSize"
      class="mx-auto" />
    <v-avatar
      v-if="notDisplayedItems"
      :size="iconSize"
      class="notDisplayedManagersOverlay"
      @click="$root.$emit('displaySpaceManagers', space)">
      <div class="notDisplayedManagers">
        +{{ notDisplayedItems }}
      </div>
    </v-avatar>
  </div>
</template>

<script>
export default {
  props: {
    space: {
      type: Object,
      default: () => null,
    },
    max: {
      type: Number,
      default: () => 0,
    },
  },
  data() {
    return {
      iconSize: 37,
    };
  },
  computed: {
    users() {
      return this.space && this.space.managers;
    },
    usersToDisplay() {
      return this.users && this.users.slice(0, this.max);
    },
    notDisplayedItems() {
      return this.users && this.users.length > this.max ? this.users.length - this.max : 0;
    },
  }
};
</script>