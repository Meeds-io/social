<template>
  <v-card
    class="full-width d-flex flex-column align-center justify-center py-8 px-4 border-box-sizing"
    flat
    min-height="75vh">
    <v-icon
      color="primary"
      size="100">
      far fa-question-circle
    </v-icon>
    <div class="text-color headline mt-6">
      {{ $t('UIUserActivitiesDisplay.label.activityNotFound') }}
    </div>
    <v-card
      class="text-title mt-6 mx-2 d-none d-sm-inline"
      flat
      max-width="80%"
      width="80%">
      {{ $t('activity.notFound.message') }}
    </v-card>
    <v-btn
      class="btn btn-primary mt-6"
      :href="activityStreamUrl"
      large>
      <span class="text-none text-title">
        {{ $t('activity.notFound.button.exploreActivities') }}
      </span>
    </v-btn>
  </v-card>
</template>
<script>
  export default {
    data: () =>({
      activityStreamUrl: `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/activity`,
    }),
  };
</script>