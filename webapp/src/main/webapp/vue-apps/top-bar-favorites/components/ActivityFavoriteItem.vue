<template>
  <v-list-item
    v-if="activity"
    :href="activityUrl"
    @keydown.enter="setAsViewed"
    @auxclick="setAsViewed"
    @click="setAsViewed">
    <v-list-item-icon class="me-3 my-auto">
      <v-card
        :min-width="iconWidth"
        class="d-flex justify-center no-border-radius"
        color="transparent"
        flat>
        <v-img
          v-if="activityTypeExtension && activityTypeExtension.img"
          :src="activityTypeExtension.img"
          :max-height="maxHeight"
          :max-width="maxWidth" />
        <v-icon 
          v-else-if="activityTypeExtension && activityTypeExtension.icon"
          :size="iconSize" 
          :class="activityTypeExtension.class">
          {{ activityTypeExtension.icon }} 
        </v-icon> 
        <v-icon
          v-else
          :size="iconSize">
          fas fa-stream
        </v-icon>
      </v-card>
    </v-list-item-icon>

    <v-list-item-content>
      <v-list-item-title class="text-truncate">
        {{ activityTitleText }}
      </v-list-item-title>
      <v-list-item-subtitle v-if="expanded" class="d-flex align-center full-width overflow-hidden pt-2px">
        <template v-if="space">
          <favorite-space-avatar
            :space="space"
            :size="16"
            class="flex-grow-0 flex-shrink-1 text-truncate"
            link-style />
          <v-icon class="flex-grow-0 flex-shrink-0 mx-2" size="2">fa-circle</v-icon>
        </template>
        <date-format class="flex-grow-0 flex-shrink-0" :value="activityPostedTime" />
        <v-icon class="flex-grow-0 flex-shrink-0 mx-2" size="2">fa-circle</v-icon>
        <favorite-user-avatar
          :identity="posterIdentity"
          :size="16"
          class="flex-grow-1 flex-shrink-1 text-truncate" />
      </v-list-item-subtitle>
    </v-list-item-content>

    <v-list-item-action>
      <favorite-button
        :id="id"
        :favorite="isFavorite"
        :space-id="spaceId"
        :top="top"
        :right="right"
        type="activity"
        @removed="removed"
        @remove-error="removeError" />
    </v-list-item-action>
  </v-list-item>
</template>
<script>
export default {
  props: {
    id: {
      type: String,
      default: () => null,
    },
    activityExtensions: {
      type: Object,
      default: () => null,
    },
    clickCallback: {
      type: Function,
      default: null,
    },
    expanded: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    activity: null,
    activityUrl: '#',
    isFavorite: true,
    defaultIcon: 'fas fa-feather-alt',
    defaultClass: 'primary--text',
  }),
  computed: {
    iconWidth() {
      return this.expanded ? 40 : 30;
    },
    iconSize() {
      return this.expanded ? 34 : 24;
    },
    maxHeight() {
      return this.expanded ? 38 : 28;
    },
    maxWidth() {
      return this.expanded ? 35 : 25;
    },
    space() {
      return this.activity?.activityStream?.space;
    },
    spaceId() {
      return this.space?.id;
    },
    activityTypeExtension() {
      if (this.activity?.type) {
        return this.activityExtensions.find(extension => extension.type === this.activity.type);
      }
      return null;
    },
    activityTitle() {
      return this.activityTypeExtension?.title && this.activityTypeExtension?.title(this.activity) || this.favoriteTitle(this.activity?.title) || this.$t('UITopBarFavoritesPortlet.label.activity');
    },
    activityTitleText() {
      return this.activityTitle && this.$utils.htmlToText(this.activityTitle) || '';
    },
    activityPostedTime() {
      return this.activity?.updateDate || this.activity?.createDate;
    },
    posterIdentity() {
      return this.activity?.identity?.profile;
    },
  },
  async created() {
    this.activityUrl = `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/activity?id=${this.id}`;
    try {
      this.activity = await this.$activityService.getActivityById(this.id, 'identity');
    } catch {
      this.$root.$emit('favorite-removed', 'activity', this.id);
    }
  },
  methods: {
    favoriteTitle(title) {
      const regex = /(<([^>]+)>)/ig;
      return title && title.replace(regex, '') || '';
    },
    removed() {
      this.isFavorite = !this.isFavorite;
      this.displayAlert(this.$t('Favorite.tooltip.SuccessfullyDeletedFavorite', {0: this.$t('activity.label')}));
      this.$emit('removed');
      this.$root.$emit('refresh-favorite-list');
    },
    removeError() {
      this.displayAlert(this.$t('Favorite.tooltip.ErrorDeletingFavorite', {0: this.$t('activity.label')}), 'error');
    },
    displayAlert(message, type) {
      this.$root.$emit('alert-message', message, type || 'success');
    },
    setAsViewed(event) {
      if (event.which === 1 || event.which === 2) {
        this.clickCallback('activity', this.id);
      }
    },
  }
};
</script>
