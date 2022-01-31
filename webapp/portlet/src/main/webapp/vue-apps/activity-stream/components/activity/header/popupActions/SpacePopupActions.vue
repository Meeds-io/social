<template>
  <div class="d-flex justify-end">
    <v-btn
      v-for="(extension, i) in enabledSpacePopupExtensions"
      :key="i"
      :ripple="false"
      x-small
      icon
      color="primary"
      class="pa-2"
      @click="extension.click(space.id)">
      <v-icon :class="extension.class ? `${extension.class} mt-n1` : ''">{{ extension.icon }} </v-icon>
    </v-btn>
    <div
      v-for="extension in enabledWebConferencingComponents"
      :key="extension.key"
      class="py-2 ps-2 pe-0"
      :class="`${extension.appClass} ${extension.typeClass}`"
      :ref="extension.key">
    </div>
  </div>
</template>
<script>
export default {
  props: {
    space: {
      type: Object,
      default: null,
    },
    spacePopupExtensions: {
      type: Array,
      default: () => [],
    }
  },
  data() {
    return {
      webConferencingExtensions: [],
    };
  },
  computed: {
    enabledWebConferencingComponents() {
      return this.webConferencingExtensions.filter(extension => extension.enabled);
    },
    enabledSpacePopupExtensions() {
      if (!this.spacePopupExtensions || !this.space || !this.space.isMember) {
        return [];
      }
      return this.spacePopupExtensions.slice().filter(extension => extension.enabled(this.space));
    },
  },
  created() {
    this.refreshSpacePopupExtensions();
  },
  mounted() {
    this.webConferencingExtensions.map(extension => this.initWebConferencingActionComponent(extension));
  },
  methods: {
    refreshSpacePopupExtensions() {
      if (this.spacePopupExtensions ) {
        this.webConferencingExtensions = this.spacePopupExtensions.slice().filter((extension => extension.key));
        this.spacePopupExtensions = this.spacePopupExtensions.slice().filter((extension => !extension.key));
      }
    },
    initWebConferencingActionComponent(extension) {
      if (extension.enabled) {
        let container = this.$refs[extension.key];
        if (container && container.length > 0) {
          container = container[0];
          extension.init(container, this.space.prettyName);
        } else {
          // eslint-disable-next-line no-console
          console.error(
            `Error initialization of the ${extension.key} action component: empty container`
          );
        }
      }
    }
  }
};
</script>


