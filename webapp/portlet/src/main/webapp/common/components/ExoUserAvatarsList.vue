<template>
  <div class="d-flex flex-nowrap">
    <exo-user-avatar
      v-for="user in usersToDisplay"
      :key="user"
      :username="user.username"
      :title="user.fullname"
      :size="iconSize"
      :labels="labels"
      class="mx-auto" />
    <v-avatar
      v-if="notDisplayedItems"
      :size="iconSize"
      :tiptip="false"
      class="notDisplayedIdentitiesOverlay"
      @click="$emit('open-detail')">
      <div class="notDisplayedIdentities">
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
  },
  computed: {
    usersToDisplay() {
      return this.users && this.users.slice(0, this.max);
    },
    notDisplayedItems() {
      return this.users && this.users.length > this.max ? this.users.length - this.max : 0;
    },
  }
};
</script>