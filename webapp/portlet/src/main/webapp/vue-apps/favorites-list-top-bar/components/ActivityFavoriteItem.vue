<template>
  <v-list-item class="clickable" :href="activityUrl">
    <v-list-item-icon v-if="activityTypeExtension && activityTypeExtension.img" class="me-3 my-auto">
      <v-img
        :src="activityTypeExtension.img"
        max-height="28"
        max-width="25" />
    </v-list-item-icon>
    <v-list-item-icon v-else class="me-3 my-auto">
      <v-icon size="24" :class="activityTypeExtension.class"> {{ activityTypeExtension.icon }} </v-icon> 
    </v-list-item-icon>

    <v-list-item-content>
      <v-list-item-title class="text-color body-2">
        <p
          class="ma-auto"
          v-sanitized-html="activityTypeExtension.title"></p>
      </v-list-item-title>
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
    }
  },
  data: () => ({
    activity: null,
    activityTitle: '',
    activityUrl: '#',
    isFavorite: true,
    defaultIcon: 'fas fa-feather-alt',
    defaultClass: 'primary--text',
    activityTypeExtension: {},
  }),
  watch: {
    activity() {
      if (this.activity?.type) {
        this.activityExtensions.forEach(extension => {
          if (extension.type === this.activity.type) {
            this.activityTypeExtension.icon = extension?.icon ? extension.icon : '';
            this.activityTypeExtension.img = extension?.img ? extension.img : '';  
            this.activityTypeExtension.class = extension?.class ? extension.class : '';
            this.activityTypeExtension.title = extension?.title ? extension.title(this.activity) : this.$t('UITopBarFavoritesPortlet.label.activity');
          }
        });
      } else {
        this.activityTypeExtension.icon = this.defaultIcon;
        this.activityTypeExtension.class = this.defaultClass;
        this.activityTypeExtension.title = this.activity?.title ? this.favoriteTitle(this.activity.title) : this.$t('UITopBarFavoritesPortlet.label.activity');
      }
    }
  },
  computed: {
    spaceId() {
      return this.activity?.activityStream?.space?.id;
    },
  },
  created() {
    this.activityUrl = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity?id=${this.id}`;
    this.$activityService.getActivityById(this.id)
      .then(fullActivity => {
        this.activity = fullActivity;
      });
  },
  methods: {
    favoriteTitle(title) {
      const regex = /(<([^>]+)>)/ig;
      return title.replace(regex, '').split(' ').slice(0, 5).join(' ');
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
      this.$root.$emit('activity-notification-alert', {
        activityId: this.id,
        message,
        type: type || 'success',
      });
    },
  }
};
</script>
