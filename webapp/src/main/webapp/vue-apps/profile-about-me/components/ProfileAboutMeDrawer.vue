<template>
  <exo-drawer
    ref="drawer"
    v-model="drawer"
    :loading="saving"
    class="aboutMeDrawer"
    allow-expand
    right>
    <template #title>
      {{ $t('profileAboutYouself.title') }}
    </template>
    <template v-if="drawer" #content>
      <v-card flat>
        <v-card-text>
          <rich-editor
            id="aboutMeRichEditor"
            v-model="aboutMe"
            :placeholder="$t('profileAboutMe.placeholder')"
            :max-length="maxLength"
            :tag-enabled="false"
            ck-editor-type="abountMe" />
        </v-card-text>
        <v-card-actions class="px-4">
          <v-spacer />
          <v-btn
            :loading="saving"
            :disabled="saving || !valid"
            class="btn btn-primary"
            @click="saveAboutMe">
            {{ $t('profileAboutMe.save') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    value: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    aboutMe: null,
    saving: null,
    maxLength: 1300,
    drawer: false,
  }),
  computed: {
    valid() {
      return !this.aboutMe || this.$utils.htmlToText(this.aboutMe).length <= this.maxLength;
    },
  },
  methods: {
    open() {
      this.aboutMe = this.value;
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    saveAboutMe() {
      this.saving = true;
      return this.$userService.updateProfileField(eXo.env.portal.userName, 'aboutMe', this.aboutMe)
        .then(() => {
          this.$emit('input', this.aboutMe);
          this.close();
        })
        .catch(() => this.$root.$emit('alert-message', this.$t('profileAboutMe.savingError'), 'error'))
        .finally(() => this.saving = false);
    },
  },
};
</script>