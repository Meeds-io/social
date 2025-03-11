<template>
  <v-list-item
    class="clickable"
    :href="activityUrl">
    <v-list-item-icon class="me-3 my-auto">
      <v-img
        v-if="activityTypeExtension && activityTypeExtension.img"
        max-height="28"
        max-width="25"
        :src="activityTypeExtension.img" />
      <v-icon 
        v-else-if="activityTypeExtension && activityTypeExtension.icon"
        :class="activityTypeExtension.class" 
        size="24">
        {{ activityTypeExtension.icon }} 
      </v-icon> 
      <v-icon
        v-else
        class="primary--text" 
        size="24">
        fas fa-stream
      </v-icon> 
    </v-list-item-icon>

    <v-list-item-content>
      <v-list-item-title>
        <p
          v-sanitized-html="activityTitle"
          class="ma-auto text-truncate"></p>
      </v-list-item-title>
    </v-list-item-content>

    <v-list-item-action>
      <favorite-button
        :id="id"
        :favorite="isFavorite"
        :right="right"
        :space-id="spaceId"
        :top="top"
        type="activity"
        @remove-error="removeError"
        @removed="removed" />
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
    },
    data: () => ({
      activity: null,
      activityUrl: '#',
      isFavorite: true,
      defaultIcon: 'fas fa-feather-alt',
      defaultClass: 'primary--text',
    }),
    computed: {
      spaceId () {
        return this.activity?.activityStream?.space?.id;
      },
      activityTypeExtension () {
        if (this.activity?.type) {
          return this.activityExtensions.find(extension => extension.type === this.activity.type);
        }
        return null;
      },
      activityTitle () {
        return this.activityTypeExtension?.title && this.activityTypeExtension?.title(this.activity) || this.favoriteTitle(this.activity?.title) || this.$t('UITopBarFavoritesPortlet.label.activity');
      },
    },
    created () {
      this.activityUrl = `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/activity?id=${this.id}`;
      this.$activityService.getActivityById(this.id)
        .then(fullActivity => {
          this.activity = fullActivity;
        });
    },
    methods: {
      favoriteTitle (title) {
        const regex = /(<([^>]+)>)/ig;
        return title && title.replace(regex, '') || '';
      },
      removed () {
        this.isFavorite = !this.isFavorite;
        this.displayAlert(this.$t('Favorite.tooltip.SuccessfullyDeletedFavorite', { 0: this.$t('activity.label') }));
        this.$emit('removed');
        this.$root.$emit('refresh-favorite-list');
      },
      removeError () {
        this.displayAlert(this.$t('Favorite.tooltip.ErrorDeletingFavorite', { 0: this.$t('activity.label') }), 'error');
      },
      displayAlert (message, type) {
        this.$root.$emit('alert-message', message, type || 'success');
      },
    },
  };
</script>
