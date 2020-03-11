<template>
  <div class="activityComposer">
    <div class="openLink">
      <a @click="openMessageComposer()">
        <i class="uiIconGoUp"></i>{{ $t('activity.composer.link') }}
      </a>
    </div>

    <v-app id="activityComposerApp" class="VuetifyApp">
      <div :class="showMessageComposer ? 'open' : ''" class="drawer">
        <div class="header">
          <img src="/eXoSkin/skin/images/system/composer/composer.png">
          <span> {{ $t('activity.composer.title') }}</span>
          <a class="closebtn" href="javascript:void(0)" @click="closeMessageComposer()">×</a>
        </div>
        <div class="content">
          <exo-activity-rich-editor ref="richEditor" v-model="message" :max-length="MESSAGE_MAX_LENGTH" :placeholder="$t('activity.composer.placeholder').replace('{0}', MESSAGE_MAX_LENGTH)"></exo-activity-rich-editor>
          <div class="composerActions">
            <button :disabled="postDisabled" type="button" class="btn btn-primary ignore-vuetify-classes" @click="postMessage()">{{ $t('activity.composer.post') }}</button>
          </div>
          <transition name="fade">
            <div v-show="showErrorMessage" class="alert alert-error">
              <i class="uiIconError"></i>{{ $t('activity.composer.post.error') }}
            </div>
          </transition>
          <div v-for="app in activityComposerApplications" :key="app.key">
            <div class="appsItem" @click="openApp(app)">
              <div class="appsItemIcon"><div :class="app.iconClass"></div></div>
              <div class="appsItemDescription">
                <div class="AppLabel">{{ app.labelKey }}</div>
                <div class="appDescription">
                  <p>{{ app.description }}</p>
                </div>
              </div>
            </div>
            <div :class="app.appClass" v-html="app.component"></div>
          </div>
        </div>
      </div>
    </v-app>
    <div v-show="showMessageComposer" class="drawer-backdrop" @click="closeMessageComposer()"></div>
  </div>
</template>

<script>
import * as composerServices from '../composerServices';
import {activityComposerApplications, installExtensions} from '../extension';

export default {
  data() {
    return {
      MESSAGE_MAX_LENGTH: 2000,
      MESSAGE_TIMEOUT: 5000,
      showMessageComposer: false,
      message: '',
      showErrorMessage: false,
      activityComposerApplications: []
    };
  },
  computed: {
    postDisabled: function() {
      const pureText = this.message ? this.message.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, '').trim() : '';
      return pureText.length === 0 || pureText.length > this.MESSAGE_MAX_LENGTH;
    }
  },
  watch: {
    showErrorMessage: function(newVal) {
      if(newVal) {
        setTimeout(() => this.showErrorMessage = false, this.MESSAGE_TIMEOUT);
      }
    },
    activityComposerApplications() {
      console.log('composerApps',this.activityComposerApplications);
    }
  },
  created() {
    installExtensions();
    this.activityComposerApplications = activityComposerApplications;
  },
  methods: {
    openMessageComposer: function() {
      this.$refs.richEditor.setFocus();
      this.showMessageComposer = true;
    },
    postMessage() {
      // Using a ref to the editor component and the getMessage method is mandatory to
      // be sure to get the most up to date value of the message
      const msg = this.$refs.richEditor.getMessage();
      if(eXo.env.portal.spaceId) {
        composerServices.postMessageInSpace(msg, eXo.env.portal.spaceId)
          .then(() => this.refreshActivityStream())
          .then(() => this.closeMessageComposer())
          .then(() => this.message = '')
          .catch(error => {
            console.error(`Error when posting message: ${error}`);
            this.showErrorMessage = true;
          });
      } else {
        composerServices.postMessageInUserStream(msg, eXo.env.portal.userName)
          .then(() => this.refreshActivityStream())
          .then(() => this.closeMessageComposer())
          .then(() => this.message = '')
          .catch(error => {
            console.error(`Error when posting message: ${error}`);
            this.showErrorMessage = true;
          });
      }
    },
    refreshActivityStream() {
      const refreshButton = document.querySelector('.uiActivitiesDisplay #RefreshButton');
      if(refreshButton) {
        refreshButton.click();
      }
    },
    closeMessageComposer: function() {
      this.showMessageComposer = false;
    },
    openApp(app) {
      app.init();
    }
  }
};
</script>