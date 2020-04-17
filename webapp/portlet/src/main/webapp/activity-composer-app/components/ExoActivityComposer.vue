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
          <div class="composerButtons">
            <button :disabled="postDisabled" type="button" class="btn btn-primary ignore-vuetify-classes" @click="postMessage()">{{ $t('activity.composer.post') }}</button>
          </div>
          <transition name="fade">
            <div v-show="showErrorMessage" class="alert alert-error">
              <i class="uiIconError"></i>{{ $t('activity.composer.post.error') }}
            </div>
          </transition>
          <div class="VuetifyApp">
            <v-app>
              <div v-if="attachments.length" class="attachmentsList">
                <v-progress-circular
                  :class="uploading ? 'uploading' : ''"
                  :indeterminate="false"
                  :value="attachmentsProgress">
                  <i class="uiIconAttach"></i>
                </v-progress-circular>
                <div class="attachedFiles">{{ $t('attachments.drawer.title') }} ({{ attachments.length }})</div>
              </div>
            </v-app>
          </div>

          <div class="composerActions">
            <div v-for="action in activityComposerActions" :key="action.key" :class="`${action.appClass}Action`">
              <div class="actionItem" @click="executeAction(action)">
                <div class="actionItemIcon"><div :class="action.iconClass"></div></div>
                <div class="actionItemDescription">
                  <div class="actionLabel">{{ getLabel(action.labelKey) }}</div>
                  <div class="actionDescription">
                    <p>{{ getLabel(action.description) }}</p>
                  </div>
                </div>
              </div>
              <component v-dynamic-events="actionsEvents[action.key]" v-if="action.component"
                         v-show="showMessageComposer" :ref="action.key" v-bind="action.component.props"
                         v-model="actionsData[action.key].value" :is="action.component.name"></component>
            </div>
          </div>
        </div>
      </div>
    </v-app>
    <div v-show="showMessageComposer" class="drawer-backdrop" @click="closeMessageComposer()"></div>
  </div>
</template>

<script>
import * as composerServices from '../composerServices';
import {getActivityComposerExtensions, executeExtensionAction} from '../extension';

export default {
  directives: {
    DynamicEvents: {
      bind: function (el, binding, vnode) {
        const allEvents = binding.value;
        if (allEvents) {
          allEvents.forEach((event) => {
            // register handler in the dynamic component
            vnode.componentInstance.$on(event.event, (eventData) => {
              const param = eventData ? eventData : event.listenerParam;
              // when the event is fired, the eventListener function is going to be called
              vnode.context[event.listener](param);
            });
          });
        }
      },
      unbind: function (el, binding, vnode) {
        vnode.componentInstance.$off();
      },
    }
  },
  data() {
    return {
      MESSAGE_MAX_LENGTH: 2000,
      MESSAGE_TIMEOUT: 5000,
      showMessageComposer: false,
      message: '',
      showErrorMessage: false,
      activityComposerActions: [],
      attachments: [],
      uploadingCount: 0,
      percent: 100,
      actionsData: [],
      actionsEvents: []
    };
  },
  computed: {
    postDisabled: function() {
      const pureText = this.message ? this.message.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, '').trim() : '';
      return pureText.length === 0 && this.attachments.length === 0 || pureText.length > this.MESSAGE_MAX_LENGTH || this.uploading;
    },
    activityType: function() {
      return this.attachments.length ? 'files:spaces' : '';
    },
    attachmentsProgress: function () {
      return this.attachments.length !== 0 ? this.uploadingProgress / this.attachments.length : 0;
    },
    uploading: function() {
      return this.attachments.length * this.percent !== this.uploadingProgress;
    },
    uploadingProgress: function() {
      return this.attachments.map(attachment => {
        return typeof attachment.uploadProgress !== 'undefined' ? attachment.uploadProgress : this.percent;
      }).reduce((a, b) => a + b, 0);
    }
  },
  watch: {
    showErrorMessage: function(newVal) {
      if(newVal) {
        setTimeout(() => this.showErrorMessage = false, this.MESSAGE_TIMEOUT);
      }
    },
    uploadingProgress() {
      console.log(this.uploadingProgress);
    }
  },
  created() {
    this.activityComposerActions = getActivityComposerExtensions();
    this.activityComposerActions.forEach(action => {
      if (action.component) {
        this.actionsData[action.key] = action.component.model;
        this.actionsEvents[action.key] = action.component.events;
      }
    });
  },
  methods: {
    openMessageComposer: function() {
      this.$refs.richEditor.setFocus();
      this.showMessageComposer = true;
    },
    getLabel: function(labelKey) {
      const label = this.$t(labelKey);
      return label ? label : labelKey;
    },
    postMessage() {
      // Using a ref to the editor component and the getMessage method is mandatory to
      // be sure to get the most up to date value of the message
      const msg = this.$refs.richEditor.value;
      if(eXo.env.portal.spaceId) {
        composerServices.postMessageInSpace(msg, this.activityType, this.attachments, eXo.env.portal.spaceId)
          .then(() => this.refreshActivityStream())
          .then(() => this.closeMessageComposer())
          .then(() => {
            this.resetComposer();
          })
          .catch(error => {
            console.error(`Error when posting message: ${error}`);
            this.showErrorMessage = true;
          });
      } else {
        composerServices.postMessageInUserStream(msg, this.activityType, this.attachments, eXo.env.portal.userName)
          .then(() => this.refreshActivityStream())
          .then(() => this.closeMessageComposer())
          .then(() => {
            this.resetComposer();
          })
          .catch(error => {
            console.error(`Error when posting message: ${error}`);
            this.showErrorMessage = true;
          });
      }
    },
    resetComposer() {
      this.message = '';
      this.activityComposerActions.forEach(action => {
        if (action.component) {
          this.actionsData[action.key].value = action.component.model.default;
        }
      });
      this.attachments = [];
      this.uploadingCount = 0;
      this.showErrorMessage = false;
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
    executeAction(action) {
      executeExtensionAction(action, this.$refs[action.key]);
    },
    setUploadingCount: function(action) {
      if (action === 'add') {
        this.uploadingCount++;
      } else if (action === 'addExisting') {
        this.uploadingCount = this.attachments
          .filter(attachment => attachment.uploadProgress == null || attachment.uploadProgress === this.percent).length;
      } else if (action === 'remove' && this.uploadingCount !== 0) {
        this.uploadingCount--;
      }
    },
    updateAttachments(attachments) {
      this.attachments = attachments;
    }
  }
};
</script>
