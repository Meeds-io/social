<template>
  <v-list-item class="suggestions-list-item pa-0">
    <v-list-item-avatar
      :size="avatarSize">
      <v-img
        :lazy-src="avatarUrl || ''"
        :src="avatarUrl || ''"
        transition="none"
        eager />
    </v-list-item-avatar>
    <v-list-item-content class="pb-3">
      <v-list-item-title class="body-2 font-weight-bold text-color suggestions-list-item-title">
        <a :href="url" class="text-color">
          {{ people.suggestionName }}
        </a>
      </v-list-item-title>
      <v-list-item-subtitle class="caption text-sub-title suggestions-list-item-subtitle">
        {{ people.number }} {{ $t('connection.label') }}
      </v-list-item-subtitle>
    </v-list-item-content>
    <v-list-item-action class="suggestions-list-item-actions">
      <v-btn-toggle class="transparent">
        <a
          text
          icon
          small
          min-width="auto"
          class="px-0 suggestions-btn-action connexion-accept-btn"
          @click="connectionRequest(people)">
          <i class="uiIconInviteUser"></i>
        </a>
        <a
          text
          small
          min-width="auto"
          class="px-0 suggestions-btn-action connexion-refuse-btn"
          @click="ignoredConnectionUser(people)">
          <i class="uiIconCloseCircled"></i>
        </a>
      </v-btn-toggle>
    </v-list-item-action>
  </v-list-item>
</template>
<script>
export default {
  props: {
    people: {
      type: Object,
      default: () => null,
    },
    avatarSize: {
      type: Number,
      default: () => 37,
    },
    peopleSuggestionsList: {
      type: Array,
      default: () => []
    }
  },
  computed: {
    avatarUrl() {
      return this.people && this.people.avatar || `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${this.people.username}/avatar`;
    },
    url() {
      if (!this.people || !this.people.suggestionId) {
        return '#';
      }
      return `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile/${this.people.username}`;
    },
  },
  methods: {
    connectionRequest(item) {
      this.$userService.connect(item.username).then(
        ()=> {
          this.peopleSuggestionsList.splice(this.peopleSuggestionsList.indexOf(item),1);
        }
      );
    },
    ignoredConnectionUser(receiverItem) {
      this.$userService.ignore(receiverItem.username).then(
        () => {
          this.peopleSuggestionsList.splice(this.peopleSuggestionsList.indexOf(receiverItem),1);
        }
      );
    },
  },
};
</script>