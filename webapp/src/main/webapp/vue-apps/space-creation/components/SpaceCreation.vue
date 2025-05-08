<template>
  <v-app>
    <v-btn
      color="primary"
      @click="addNewSpace">
      {{ $t('create.button') }}
    </v-btn>
  </v-app>    
</template>

<script>
export default {
  data: () => ({
  }),
  methods: {
    addNewSpace() {
      window.require(['SHARED/spaceForm'], drawer => drawer.open(false, true));
    },
  }
};
</script>
