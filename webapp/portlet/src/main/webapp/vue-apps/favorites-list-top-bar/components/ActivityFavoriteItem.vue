<template>
  <v-list-item class="clickable" :href="activityUrl">
    <v-list-item-icon class="me-3 my-auto">
      <v-icon size="22" class="icon-default-color"> fas fa-file-alt </v-icon>
    </v-list-item-icon>

    <v-list-item-content>
      <v-list-item-title class="text-color body-2">{{ activityTitle }}</v-list-item-title>
    </v-list-item-content>

    <v-list-item-action>
      <v-btn icon>
        <v-icon class="yellow--text text--darken-2" size="18">fa-star</v-icon>
      </v-btn>
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
  },
  data: () => ({
    activityTitle: '',
    activityUrl: '#'
  }),
  created() {
    this.$activityService.getActivityById(this.id)
      .then(fullActivity => {
        this.activityUrl = fullActivity.href;
        if (fullActivity && fullActivity.title ) {
          this.activityTitle = this.favoriteTitle(fullActivity.title);
        } else {
          this.activityTitle = this.$t('UITopBarFavoritesPortlet.label.activity');
        }
      });
  },
  methods: {
    favoriteTitle(title) {
      const regex = /(<([^>]+)>)/ig;
      const activityTitle = title.replace(regex, '').split(' ').slice(0, 5).join(' ');
      return activityTitle;
    }
  }
};
</script>
