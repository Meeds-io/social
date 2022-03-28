<template>
  <extension-registry-component
    :key="favoriteId"
    :component="component"
    :element="div"
    :params="params" />
</template>
<script>
export default {
  props: {
    favorite: {
      type: Object,
      default: function() {
        return null;
      },
    },
  },
  data: () => ({
    component: {}
  }),
  computed: {
    favoriteType () {
      return this.favorite && this.favorite.objectType;
    },
    favoriteId () {
      return this.favorite && this.favorite.objectId;
    },
    params () {
      return {
        id: this.favoriteId,
      };
    },
    componentsApp () {
      return `favorite-${this.favoriteType}`;
    }
  },
  created() {
    const registeredComponents = extensionRegistry.loadComponents(this.componentsApp);
    if ( registeredComponents ) {
      this.component = registeredComponents[0];
    }
  }
};
</script>
