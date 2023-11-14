<template>
  <div v-if="displayed">
    <v-card class="card-border-radius" flat>
      <v-list two-line class="transparent">
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="title text-color">
              {{ $t('SpaceSettings.general') }}
            </v-list-item-title>
          </v-list-item-content>
          <v-list-item-action>
            <v-btn
              small
              icon
              @click="openDrawer">
              <v-icon size="18" class="icon-default-color">fa-edit</v-icon>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
      </v-list>
      <space-setting-form-drawer
        ref="spaceFormDrawer"
        :max-upload-size="maxUploadSize" />
    </v-card>
  </div>
</template>

<script>
export default {
  props: {
    spaceId: {
      type: String,
      default: null,
    },
    maxUploadSize: {
      type: Number,
      default: 2,
    },
  },
  data: () => ({
    id: `Settings${parseInt(Math.random() * 10000)
      .toString()
      .toString()}`,
    displayed: true,
  }),
  created() {
    document.addEventListener('hideSettingsApps', (event) => {
      if (event && event.detail && this.id !== event.detail) {
        this.displayed = false;
      }
    });
    document.addEventListener('showSettingsApps', () => this.displayed = true);
  },
  methods: {
    openDrawer() {
      this.$refs.spaceFormDrawer.open(this.spaceId);
    },
  },
};
</script>
