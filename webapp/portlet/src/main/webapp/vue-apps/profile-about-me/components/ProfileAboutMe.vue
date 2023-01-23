<template>
  <v-app
    v-if="displayApp"
    :class="owner && 'profileAboutMe' || 'profileAboutMeOther'"
    class="white">
    <v-toolbar
      color="white"
      flat
      class="border-box-sizing">
      <div
        class="text-header-title text-sub-title">
        {{ title }}
      </div>
      <v-spacer />
      <v-btn
        v-if="owner"
        icon
        outlined
        small
        @click="editAboutMe">
        <i class="uiIconEdit uiIconLightBlue pb-2"></i>
      </v-btn>
    </v-toolbar>
    <v-card
      class="border-box-sizing"
      flat>
      <p
        v-autolinker="aboutMe"
        v-if="aboutMe || !owner"
        class="paragraph text-color pt-0 pb-6 px-4"></p>
      <p
        v-else
        class="paragraph text-color pt-0 pb-6 px-4"
        v-text="$t('profileAboutMe.emptyOwner')"></p>
    </v-card>
    <exo-drawer
      v-if="owner"
      ref="aboutMeDrawer"
      class="aboutMeDrawer"
      right>
      <template slot="title">
        {{ title }}
      </template>
      <template slot="content">
        <v-card flat>
          <v-card-text>
            <extended-textarea
              v-model="modifyingAboutMe"
              :max-length="aboutMeTextLength" />
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
          <v-card-text v-if="error">
            <v-alert type="error">
              {{ error }}
            </v-alert>
          </v-card-text>
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
    error: null,
    saving: null,
    modifyingAboutMe: null,
    aboutMeTextLength: 2000,
    initialized: false,
  }),
  computed: {
    valid() {
      return !this.modifyingAboutMe || this.modifyingAboutMe.length <= this.aboutMeTextLength;
    },
    title() {
      return this.owner && this.$t('profileAboutYouself.title') || this.$t('profileAboutMe.title');
    },
    displayApp() {
      return this.owner || !this.initialized || this.aboutMe?.trim().length;
    },
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
      this.error = null;
      this.saving = true;
      this.$refs.aboutMeDrawer.startLoading();
      return this.$userService.updateProfileField(eXo.env.portal.profileOwner, 'aboutMe', this.modifyingAboutMe)
        .then(() => this.refresh(this.modifyingAboutMe))
        .catch(error => {
          console.warn('Error saving about me section', error); // eslint-disable-line no-console
          this.error = this.$t('profileAboutMe.savingError');
        })
        .finally(() => {
          this.saving = false;
          this.$refs.aboutMeDrawer.endLoading();
        });
    },
  },
};
</script>