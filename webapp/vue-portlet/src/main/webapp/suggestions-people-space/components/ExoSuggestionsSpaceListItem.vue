<template>
  <v-list-item class="suggestions-list-item pa-0">
    <v-list-item-avatar
      :size="avatarSize"
      class="spaceAvatar">
      <v-img :src="avatarUrl"></v-img>
    </v-list-item-avatar>
    <v-list-item-content class="pb-3">
      <v-list-item-title class="body-2 font-weight-bold suggestions-list-item-title">
        <a :href="url" class="text-color">{{ space.displayName }}</a>
      </v-list-item-title>
      <v-list-item-subtitle
        class="caption text-sub-title suggestions-list-item-subtitle">
        {{ space.members }} {{ $t('spacemember.Label') }}
      </v-list-item-subtitle>
    </v-list-item-content>
    <v-list-item-action class="suggestions-list-item-actions">
      <v-btn-toggle class="transparent" dark>
        <a text
           icon
           small
           min-width="auto"
           class="px-0 suggestions-btn-action connexion-accept-btn"
           @click="joinSpace(space)">
          <i class="uiIconPlusLight"></i>
        </a>
        <a text
           small
           min-width="auto"
           class="px-0 suggestions-btn-action connexion-refuse-btn"
           @click="ignoredSuggestionSpace(space)">
          <i class="uiIconCloseCircled"></i>
        </a>
      </v-btn-toggle>
    </v-list-item-action>
  </v-list-item>
</template>

<script>
import * as spaceService from '../../common/js/SpaceService.js';
export default {
  props: {
    space: {
      type: Object,
      default: () => null,
    },
    avatarSize: {
      type: Number,
      default: () => 37,
    },
  },
  computed: {
    avatarUrl() {
      return this.space && this.space.spaceAvatarUrl || `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${this.space.spaceUrl}/avatar`;
    },
    url() {
      if (!this.space || !this.space.spaceId) {
        return '#';
      }
      return `${eXo.env.portal.context}/g/${this.space.spaceUrl}/`;
    },
  },
  methods: {
    joinSpace(item) {
      spaceService.requestJoin(item.spaceId).then(
        () => {
          this.spacesSuggestionsList.splice(this.spacesSuggestionsList.indexOf(item),1);
        }
      );
    },
    ignoredSuggestionSpace(item) {
      spaceService.ignoreSuggestion(item).then(
        () => {
          this.spacesSuggestionsList.splice(this.spacesSuggestionsList.indexOf(item),1);
        }
      );
    },
  },
};
</script>