<template>
  <v-app
    :class="owner && 'profileAboutMe' || 'profileAboutMeOther'"
    class="white">
    <v-toolbar color="white" flat>
      <div class="text-header-title text-sub-title">
        {{ $t('profileAboutMe.title') }}
      </div>
      <v-spacer />
      <v-btn
        icon
        outlined
        @click="editAboutMe">
        <i class="uiIconEdit uiIconLightBlue" />
      </v-btn>
    </v-toolbar>
    <v-card flat>
      <p class="paragraph text-color pt-0 pb-6 px-4" v-text="aboutMe">
      </p>
    </v-card>
    <exo-drawer
      v-if="owner"
      ref="aboutMeDrawer"
      class="aboutMeDrawer"
      right>
      <template slot="title">
        {{ $t('profileAboutMe.title') }}
      </template>
      <template slot="content">
        <v-card flat>
          <v-card-text>
            <v-textarea
              v-model="modifyingAboutMe"
              :rules="rules"
              :counter-value="counterValue"
              counter
              class="aboutMeTextArea">
            </v-textarea>
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
import * as userService from '../../common/js/UserService.js'; 

const MAX_TEXT = 2000;

export default {
  props: {
    aboutMe: {
      type: String,
      default: () => '',
    },
  },
  data: () => ({
    owner: eXo.env.portal.profileOwner === eXo.env.portal.userName,
    rules: [v => !v || v.length <= MAX_TEXT || ' '],
    error: null,
    saving: null,
    modifyingAboutMe: null,
  }),
  computed: {
    valid() {
      return !this.modifyingAboutMe || this.modifyingAboutMe.length <= MAX_TEXT;
    }
  },
  mounted() {
    document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
  },
  methods: {
    refresh(aboutMe) {
      this.aboutMe = aboutMe;
      this.$refs.aboutMeDrawer.close();
    },
    editAboutMe() {
      this.modifyingAboutMe = this.aboutMe;
      this.$refs.aboutMeDrawer.open();
    },
    counterValue(value) {
      return `${value && value.length || 0} / ${MAX_TEXT}`;
    },
    saveAboutMe() {
      this.error = null;
      this.saving = true;
      this.$refs.aboutMeDrawer.startLoading();
      return userService.updateProfileField('aboutMe', this.modifyingAboutMe)
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