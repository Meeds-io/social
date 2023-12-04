<template>
  <v-card
    min-height="75vh"
    class="full-width d-flex flex-column align-center justify-center py-8 px-4 border-box-sizing"
    flat>
    <v-icon size="100" color="primary">
      far fa-question-circle
    </v-icon>
    <div class="text-color headline mt-6">
      {{ $t('UIUserActivitiesDisplay.label.activityNotFound') }}
    </div>
    <v-card
      class="text-color mt-6 mx-2 d-none d-sm-inline title"
      max-width="80%"
      width="80%"
      flat>
      {{ $t('activity.notFound.message') }}
    </v-card>
    <v-btn
      :href="activityStreamUrl"
      class="btn btn-primary mt-6"
      large>
      <span class="text-none title">
        {{ $t('activity.notFound.button.exploreActivities') }}
      </span>
    </v-btn>
  </v-card>
</template>
<script>
export default {
  data: () =>({
    activityStreamUrl: eXo.env.portal.isExternal === 'true' ?
      `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/external-stream`
      : `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/stream`,
  }),
};
</script>