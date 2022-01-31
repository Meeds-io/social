<template>
  <v-menu
    v-if="!isMobile" 
    rounded="rounded"
    v-model="menu"
    open-on-hover
    :close-on-content-click="false"
    :nudge-width="200"
    max-width="270"
    min-width="270"
    offset-y>
    <template v-slot:activator="{ on, attrs }">
      <a
        v-on="on"
        v-bind="attrs"
        :id="id"
        :href="url"
        :class="!space.isMember && 'not-clickable-link hidden-space'"
        class="text-none space-avatar activity-head-space-link">
        <v-avatar
          size="20"
          rounded
          class="ma-0">
          <img
            :src="avatarUrl"
            class="object-fit-cover my-auto"
            loading="lazy"
            role="presentation">
        </v-avatar>
        <span class="primary--text">{{ displayName }}</span>
      </a>
    </template>
    <v-card elevation="0" class="pa-2">
      <v-list-item class="px-2">
        <v-list-item-content class="py-0">
          <v-list-item-title>
            <exo-space-avatar
              :space="space"
              :size="40"
              :tiptip="false"
              avatar-class="border-color"
              class="activity-share-space d-inline-block my-auto"
              bold-title
              link-style
              subtitle-new-line>
              <template slot="subTitle">
                <span v-if="spaceMembersCount" class="caption text-bold">
                  {{ spaceMembersCount }} {{ $t('UIActivity.label.Members') }}
                </span>
              </template>
            </exo-space-avatar>
          </v-list-item-title>
          <p v-if="spaceDescription" class="text-truncate-3 text-caption text--primary pt-3 font-weight-medium">
            {{ spaceDescription }}
          </p>
        </v-list-item-content>
      </v-list-item>
      <space-popup-actions :space="space" :space-popup-extensions="spacePopupExtensions" />
    </v-card>
  </v-menu>
  <a
    v-else
    v-on="on"
    v-bind="attrs"
    :id="id"
    :href="url"
    :class="!space.isMember && 'not-clickable-link hidden-space'"
    class="text-none space-avatar activity-head-space-link">
    <v-avatar
      size="20"
      rounded
      class="ma-0">
      <img
        :src="avatarUrl"
        class="object-fit-cover my-auto"
        loading="lazy"
        role="presentation">
    </v-avatar>
    <span class="primary--text">{{ displayName }}</span>
  </a>
</template>

<script>
const randomMax = 10000;
export default {
  props: {
    space: {
      type: Object,
      default: null,
    },
    size: {
      type: Number,
      default: () => 20,
    },
  },
  data: () => ({
    id: `spaceAvatar${parseInt(Math.random() * randomMax)}`,
    spacePopupExtensions: []
  }),
  computed: {
    spaceId() {
      return this.space && this.space.id;
    },
    displayName() {
      return this.space && this.space.isMember ? this.space.displayName : this.$t('spacesList.label.hiddenSpace');
    },
    groupId() {
      return this.space && this.space.groupId;
    },
    avatarUrl() {
      return this.space && !this.space.isMember ? this.defaultAvatarUrl : this.space.avatarUrl || `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${this.prettyName}/avatar`;
    },
    defaultAvatarUrl() {
      return `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/default-image/avatar`;
    },
    spaceMembersCount() {
      return this.space && this.space.membersCount;
    },
    spaceDescription() {
      return this.space && this.space.description;
    },
    url() {
      if (!this.groupId) {
        return '#';
      }
      const uri = this.groupId.replace(/\//g, ':');
      return `${eXo.env.portal.context}/g/${uri}/`;
    },
    isMobile() {
      return this.$vuetify && this.$vuetify.breakpoint && this.$vuetify.breakpoint.name === 'xs';
    },
  },
  created () {
    this.refreshExtensions();
  },
  methods: {
    refreshExtensions() {
      this.spacePopupExtensions = extensionRegistry.loadExtensions('space-popup', 'space-popup-action') || [];
    },
    
  },
};
</script>