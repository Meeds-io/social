<template>
  <v-list-item class="suggestions-list-item pa-0">
    <v-list-item-avatar
      :size="avatarSize">
      <v-img
        :lazy-src="avatarUrl || ''"
        :src="avatarUrl || ''"
        alt=""
        transition="none"
        eager />
    </v-list-item-avatar>
    <v-list-item-content class="pb-3">
      <v-list-item-title class="suggestions-list-item-title">
        <a :href="url" class="text-color">
          {{ people.suggestionName }}
        </a>
      </v-list-item-title>
      <v-list-item-subtitle class="suggestions-list-item-subtitle">
        {{ people.number }} {{ $t('connection.label') }}
      </v-list-item-subtitle>
    </v-list-item-content>
    <v-list-item-action class="suggestions-list-item-actions">
      <v-btn-toggle class="transparent">
        <button
          :aria-label="$t('suggestions.people.connect', {0: people.suggestionName})"
          type="button"
          class="btn-reset suggestions-btn-action connexion-accept-btn"
          @click="connectionRequest(people)">
          <i class="uiIconInviteUser" aria-hidden="true"></i>
        </button>
        <button
          :aria-label="$t('suggestions.people.ignore', {0: people.suggestionName})"
          type="button"
          class="btn-reset suggestions-btn-action connexion-refuse-btn"
          @click="ignoredConnectionUser(people)">
          <i class="uiIconCloseCircled" aria-hidden="true"></i>
        </button>
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