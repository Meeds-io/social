<template>
  <div>
    <exo-user-avatar
      v-for="user in usersToDisplay"
      :key="user"
      :username="user"
      :size="iconSize"
      class="mr-1" />
    <v-avatar :size="iconSize" class="notDisplayedManagersOverlay">
      <div class="notDisplayedManagers">
        +{{ notDisplayedItems }}
      </div>
    </v-avatar>
  </div>
</template>

<script>
export default {
  props: {
    users: {
      type: Array,
      default: () => [],
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
    notDisplayedItems() {
      return this.users && this.users.length > this.max ? this.users.length - this.max : 0;
    },
    usersToDisplay() {
      return this.users && this.users.slice(0, this.max);
    }
  }
};
</script>