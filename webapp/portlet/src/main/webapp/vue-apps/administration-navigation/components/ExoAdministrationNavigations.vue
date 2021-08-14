<template>
  <v-container 
    id="AdministrationHamburgerNavigation"
    px-0
    py-0
    class="white">
    <v-progress-linear
      v-if="loading"
      color="primary"
      height="2" />
    <v-row v-else-if="navigations && navigations.length" class="mx-0">
      <v-list 
        shaped 
        dense 
        min-width="90%"
        class="pb-0">
        <v-list-item-group>
          <template v-for="nav in navigations">
            <exo-administration-menu-item :key="nav.key" :menu-item="nav" />
          </template>
        </v-list-item-group>
      </v-list>
    </v-row>
  </v-container>
</template>

<script>
export default {
  props: {
    loading: {
      type: Boolean,
      default: false
    },
    navigations: {
      type: Object,
      default: null
    },
  },
};
</script>

