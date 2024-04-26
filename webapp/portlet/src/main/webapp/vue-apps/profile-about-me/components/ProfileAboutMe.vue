<template>
  <v-app
    v-if="displayApp"
    :class="owner && 'profileAboutMe' || 'profileAboutMeOther'"
    class="white">
    <widget-wrapper :title="title">
      <template #action>
        <v-btn
          v-if="owner"
          id="aboutMeEditButton"
          icon
          outlined
          small
          @click="editAboutMe">
          <v-icon size="18">fas fa-edit</v-icon>
        </v-btn>
      </template>
      <div
        v-if="hasAboutMe || !owner"
        v-sanitized-html="aboutMe"
        id="aboutMeParagraph"
        class="text-color"></div>
      <div
        v-else
        id="aboutMeParagraph"
        class="text-color">
        {{ $t('profileAboutMe.emptyOwner') }}
      </div>
    </widget-wrapper> 
    <exo-drawer
      v-if="owner && initialized"
      ref="aboutMeDrawer"
      v-model="drawer"
      class="aboutMeDrawer"
      allow-expand
      right>
      <template slot="title">
        {{ title }}
      </template>
      <template v-if="drawer" slot="content">
        <v-card flat>
          <v-card-text>
            <rich-editor
              id="aboutMeRichEditor"
              v-model="modifyingAboutMe"
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
  </v-app>
</template>
<script>
export default {
  data: () => ({
    owner: eXo.env.portal.profileOwner === eXo.env.portal.userName,
    aboutMe: null,
    saving: null,
    modifyingAboutMe: null,
    maxLength: 1300,
    initialized: false,
    drawer: false,
  }),
  computed: {
    valid() {
      return !this.modifyingAboutMe || this.$utils.htmlToText(this.modifyingAboutMe).length <= this.maxLength;
    },
    title() {
      return this.owner && this.$t('profileAboutYouself.title') || this.$t('profileAboutMe.title');
    },
    aboutMeText() {
      return this.$utils.htmlToText(this.aboutMe);
    },
    hasAboutMe() {
      return this.aboutMeText?.trim?.()?.length;
    },
    displayApp() {
      return this.owner || !this.initialized || this.hasAboutMe;
    },
  },
  watch: {
    displayApp() {
      this.$root.$updateApplicationVisibility(this.displayApp);
    }
  },
  created() {
    this.$userService.getUser(eXo.env.portal.profileOwner)
      .then(user => this.refresh(user && user.aboutMe || ''))
      .finally(() => {
        this.$root.$applicationLoaded();
        this.initialized = true;
      });
  },
  mounted() {
    if (this.aboutMe) {
      this.$root.$emit('application-loaded');
    }
  },
  methods: {
    refresh(aboutMe) {
      this.aboutMe = aboutMe;
      if (this.$refs.aboutMeDrawer) {
        this.$refs.aboutMeDrawer.close();
      }
      return this.$nextTick().then(() => this.$root.$emit('application-loaded'));
    },
    editAboutMe() {
      this.modifyingAboutMe = this.aboutMe;
      this.$refs.aboutMeDrawer.open();
    },
    saveAboutMe() {
      this.saving = true;
      this.$refs.aboutMeDrawer.startLoading();
      return this.$userService.updateProfileField(eXo.env.portal.profileOwner, 'aboutMe', this.modifyingAboutMe)
        .then(() => this.refresh(this.modifyingAboutMe))
        .catch(() => this.$root.$emit('alert-message', this.$t('profileAboutMe.savingError'), 'error'))
        .finally(() => {
          this.saving = false;
          this.$refs.aboutMeDrawer.endLoading();
        });
    },
  },
};
</script>