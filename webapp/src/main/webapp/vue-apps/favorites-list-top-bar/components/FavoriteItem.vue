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
      default: () => null,
    },
    activityExtensions: {
      type: Object,
      default: () => null,
    }
  },
  data: () => ({
    component: {},
  }),
  computed: {
    favoriteType() {
      return this.favorite && this.favorite.objectType;
    },
    favoriteId() {
      return this.favorite && this.favorite.objectId;
    },
    params() {
      return {
        id: this.favoriteId,
        activityExtensions: this.activityExtensions
      };
    },
    componentsApp() {
      return `favorite-${this.favoriteType}`;
    }
  },
  created() {
    const registeredComponents = extensionRegistry.loadComponents(this.componentsApp);
    if (registeredComponents) {
      this.component = registeredComponents[0];
    }
  }
};
</script>
