<template>
  <exo-drawer ref="managersDrawer" right>
    <template slot="title">
      {{ $t('spacesList.title.managers') }}
    </template>
    <template v-if="space && space.managers" slot="content">
      <v-layout column class="ma-3">
        <exo-user-avatar
          v-for="manager in space.managers"
          :key="manager.id"
          :identity="manager"
          :extra-class="'my-2'"
          popover />
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