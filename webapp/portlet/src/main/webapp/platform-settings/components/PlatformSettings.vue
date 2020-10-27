<template>
  <v-app id="PlatformSettings">

    <div class="d-flex">
      <div class="align-start flex-grow-1 text-no-wrap text-left font-weight-bold d-flex align-center">
        <span>
          {{ $t('Platform.settings.title') }}
        </span>
      </div>
    </div>
    <v-flex>
      <v-card flat>
        <users-visibility/>
        <document-sharing-permissions/>
      </v-card>
    </v-flex>
  </v-app>
</template>

<script>
export default {
  mounted() {
    document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
  }
};
</script>
