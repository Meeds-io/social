<template>
  <v-list class="pa-0">
    <v-list-item
      class="pt-0 pb-0"
      v-for="children in navigation"
      :key="children.id"
      :href="`${baseSiteUri}${children.uri}`"
      link>
      <v-menu
        rounded
        offset-x>
        <template #activator="{ attrs, on }">
          <v-list-item-title
            class="pt-5 pb-5 text-caption"
            v-bind="attrs"
            v-text="children.label" />
          <v-list-item-icon
            v-if="children.children.length"
            class="ms-0 me-n1 ma-auto">
            <v-btn
              v-on="children.children.length && on"
              icon
              @click.stop.prevent>
              <v-icon
                size="18">
                fa-angle-right
              </v-icon>
            </v-btn>
          </v-list-item-icon>
        </template>
        <navigation-menu-sub-item
          :navigation="children.children"
          :base-site-uri="baseSiteUri" />
      </v-menu>
    </v-list-item>
  </v-list>
</template>

<script>
export default {
  data() {
    return {
      showMenu: false,
    };
  },
  props: {
    navigation: {
      type: Object,
      default: null
    },
    baseSiteUri: {
      type: String,
      default: null
    }
  }
};
</script>