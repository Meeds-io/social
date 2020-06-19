<template>
  <exo-drawer ref="managersDrawer" right>
    <template slot="title">
      {{ $t('spacesList.title.managers') }}
    </template>
    <template v-if="space && space.managers" slot="content">
      <v-layout column class="ma-3">
        <v-flex
          v-for="manager in space.managers"
          :key="manager.id"
          class="flex-grow-1 text-truncate mb-1">
          <exo-user-avatar
            :username="manager.username"
            :fullname="manager.fullname"
            :title="manager.fullname" />
        </v-flex>
      </v-layout>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    space: null,
  }),
  mounted() {
    this.$root.$on('displaySpaceManagers', space => {
      this.space = space;
      this.$refs.managersDrawer.open();
    });
  }
};
</script>