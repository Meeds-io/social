<template>
  <v-container class="px-0 py-0">
    <v-list-item
      :href="uri"
      :target="target"
      class="d-flex px-0"
      :class="isNodeGroup && ' ' || 'clickable'"
      @mouseover="showAction = true"
      @mouseleave="showAction = false">
      <v-list-item-icon v-if="icon" class="flex align-center flex-grow-0">
        <v-icon
          size="20"
          class="icon-default-color mt-1 mx-2">
          {{ icon }}
        </v-icon>
      </v-list-item-icon>
      <v-list-item-content>
        <v-list-item-title class="body-2 mt-1">
          {{ navigation.label }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action v-if="!isNodeGroup && (isHomeLink || showAction)" class="my-auto">
        <v-btn
          v-bind="attrs"
          v-on="on"
          icon
          @click="selectHome($event)">
          <v-icon
            class="me-0 pa-2"
            :class="isHomeLink && 'primary--text' || 'icon-default-color'"
            small>
            fa-house-user
          </v-icon>
        </v-btn>
      </v-list-item-action>
    </v-list-item>
  </v-container>
</template>

<script>
export default {
  props: {
    navigation: {
      type: Object,
      default: null
    },
  },
  data: () => ({
    selectedNodeUri: eXo.env.portal.selectedNodeUri,
    selectedNavigation: null,
    homeLink: eXo.env.portal.homeLink,
    showAction: false,
  }),
  computed: {
    uri() {
      return this.isNodeGroup ? null : this.navigationUri(this.navigation);
    },
    target() {
      return this.navigation?.target === 'SAME_TAB' && '_self' || '_blank';
    },
    icon() {
      return this.navigation?.icon || 'fas fa-folder';
    },
    isNodeGroup() {
      return !this.navigation.pageKey;
    },
    isHomeLink() {
      return this.uri === this.homeLink;
    },
  },
  created() {
    document.addEventListener('homeLinkUpdated', () => this.homeLink = eXo.env.portal.homeLink);
  },
  methods: {
    navigationUri() {
      if (!this.navigation.pageKey) {
        return '';
      }
      let url = this.navigation.pageLink || `/portal/${this.navigation.siteKey.name}/${this.navigation.uri}`;
      if (!url.match(/^(https?:\/\/|javascript:|\/portal\/)/)) {
        url = `//${url}`;
      }
      return url;
    },
    selectHome(event) {
      event.preventDefault();
      event.stopPropagation();
      if (this.homeLink !== this.navigationUri()) {
        this.$root.$emit('update-home-link', this.navigation);
      }
    }
  }
};
</script>