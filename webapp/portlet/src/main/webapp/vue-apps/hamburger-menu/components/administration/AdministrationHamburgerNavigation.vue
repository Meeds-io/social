<template>
  <v-container
    id="AdministrationHamburgerNavigation"
    px-0
    py-0
    class="white d-none d-sm-block">
    <v-row class="mx-0 administrationTitle">
      <v-list-item
        @mouseover="showItemActions = true"
        @mouseleave="showItemActions = false">
        <v-list-item-icon class="mb-2 mt-3 mr-6 titleIcon">
          <v-icon class="fa fa-cog" />
        </v-list-item-icon>
        <v-list-item-content class="subtitle-2">
          {{ this.$t('menu.administration.title') }}
        </v-list-item-content>
        <v-list-item-action v-if="toggleArrow" class="my-0">
          <v-btn icon @click="$root.$emit('change-administration-menu')">
            <v-icon class="me-0 pa-2 icon-default-color clickable" small>
              {{ arrowIconClass }} 
            </v-icon>
          </v-btn>
        </v-list-item-action>
      </v-list-item>
    </v-row>
  </v-container>
</template>
<script>
export default {
  props: {
    openedMenu: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      showItemActions: false,
    };
  },
  computed: {
    arrowIconClass() {
      return this.openedMenu && 'fa-arrow-left' || 'fa-arrow-right';
    },
    toggleArrow() {
      return this.secondeLevel || this.showItemActions;
    }
  },
};
</script>
