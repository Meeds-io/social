<template>
  <div class="activityComposer">
    <div :style="{display: activityId ? 'none' : 'block'}" class="openLink">
      <a @click="openMessageComposer()">
        <i class="uiIconEdit"></i>{{ link.replace('{0}', postTarget) }}
      </a>
    </div>

    <v-app :id="activityId ? `editActivityComposer${activityId}` : 'activityComposerApp'" class="activityComposerApp VuetifyApp">
      <div :class="[showMessageComposer ? 'open' : '', activityId ? `editActivity editActivityDrawer${activityId}` : '']" class="drawer">
        <div class="header">
          <img src="/eXoSkin/skin/images/system/composer/composer.png">
          <span> {{ $t('activity.composer.title') }}</span>
          <a class="closebtn" href="javascript:void(0)" @click="closeMessageComposer()">×</a>
        </div>
        <div class="content">
          <exo-activity-rich-editor :ref="ckEditorType" v-model="message" :ck-editor-type="ckEditorType" :max-length="MESSAGE_MAX_LENGTH" :placeholder="$t('activity.composer.placeholder').replace('{0}', MESSAGE_MAX_LENGTH)"></exo-activity-rich-editor>
          <div class="composerButtons">
            <div v-if="activityComposerHintAction" class="action">
              <i class="fas fa-pencil-alt fa-sm	colorIcon" @click="activityComposerHintAction.onExecute(attachments)"></i>
              <a class="message" href="javascript:void(0)" @click="activityComposerHintAction.onExecute(attachments)" >{{ getLabel(activityComposerHintAction.labelKey) }} </a>
            </div>
            <div v-if="activityComposerHintAction === null || typeof activityComposerHintAction === 'undefined' || messageLength < MESSAGE_MAX_LENGTH" class="emptyMessage">
            </div>
            <div><button :disabled="postDisabled" type="button" class="btn btn-primary ignore-vuetify-classes btnStyle" @click="postMessage()">{{ $t(`activity.composer.${composerAction}`) }}</button></div>
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

          <div v-show="activityId ? false : showMessageComposer" class="composerActions">
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
                         :ref="action.key" v-bind="action.component.props"
                         v-model="actionsData[action.key].value" :is="action.component.name"></component>
            </div>
          </div>
        </div>
      </div>
    </v-app>
    <div v-show="showMessageComposer" :class="`drawer-backdrop-activity${activityId}`" class="drawer-backdrop" @click="closeMessageComposer()"></div>
  </div>
</template>

<script>
import * as composerServices from '../composerServices';
import {getActivityComposerActionExtensions, getActivityComposerHintActionExtensions, executeExtensionAction} from '../extension';

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
  props: {
    activityBody: {
      type: String,
      default: ''
    },
    activityId: {
      type: String,
      default: ''
    },
    composerAction: {
      type: String,
      default: 'post'
    },
    ckEditorType: {
      type: String,
      default: 'activityContent'
    }
  },
  data() {
    return {
      MESSAGE_MAX_LENGTH: 1300,
      MESSAGE_TIMEOUT: 5000,
      postTarget: '',
      showMessageComposer: false,
      message: '',
      showErrorMessage: false,
      activityComposerActions: [],
      activityComposerHintAction: null,
      attachments: [],
      percent: 100,
      actionsData: [],
      actionsEvents: [],
      link : `${this.$t('activity.composer.link')}`,
    };
  },
  computed: {
    messageLength(){
      const pureText = this.message ? this.message.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, '').trim() : '';
      return pureText.length;
    },
    postDisabled: function() {
      const pureText = this.message ? this.message.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, '').trim() : '';
      return pureText.length === 0 && this.attachments.length === 0 || pureText.length > this.MESSAGE_MAX_LENGTH || this.uploading || this.activityBodyEdited;
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
    },
    activityBodyEdited: function() {
      return this.activityId ? this.escapeHTML(this.message.split('<oembed>')[0]) === this.escapeHTML(this.cleanInitialActivityBody(this.activityBody)) : false;
    }
  },
  watch: {
    message() {
      const pureText = this.message ? this.message.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, '').trim() : '';
      if (pureText.length > this.MESSAGE_MAX_LENGTH) {
        if (this.activityComposerHintAction === null) {
          this.activityComposerHintAction = getActivityComposerHintActionExtensions();
        }
      } else {
        this.activityComposerHintAction = null;
      }
    },
    showErrorMessage: function(newVal) {
      if(newVal) {
        setTimeout(() => this.showErrorMessage = false, this.MESSAGE_TIMEOUT);
      }
    }
  },
  mounted() {
    this.message = this.activityBody;
    this.postTarget = eXo.env.portal.spaceDisplayName;
    if(!this.postTarget) {
      this.link = this.$t('activity.composer.post');
    }
  },
  created() {
    document.addEventListener('activity-composer-extension-updated', this.refreshExtensions);
    this.refreshExtensions();
  },
  methods: {
    refreshExtensions: function() {
      this.activityComposerActions = getActivityComposerActionExtensions();
      this.activityComposerActions.forEach(action => {
        if (action.component) {
          this.actionsData[action.key] = action.component.model.value;
          this.actionsEvents[action.key] = action.component.events;
        }
      });
    },
    openMessageComposer: function() {
      this.$refs[this.ckEditorType].setFocus();
      this.showMessageComposer = true;

      // Send metric
      let url = '/portal/rest/v1/social/metrics/composer/click?composer=new';
      if(eXo.env.portal.spaceId) {
        url += `&spaceId=${eXo.env.portal.spaceId}`;
      }
      fetch(url, {
        credentials: 'include',
        method: 'POST'
      });
    },
    getLabel: function(labelKey) {
      const label = this.$t(labelKey);
      return label ? label : labelKey;
    },
    postMessage() {
      // Using a ref to the editor component and the getMessage method is mandatory to
      // be sure to get the most up to date value of the message
      const msg = this.$refs[this.ckEditorType].getMessage();
      if(this.composerAction === 'update') {
        composerServices.updateActivityInUserStream(msg, this.activityId, this.activityType, this.attachments)
          .then(() => this.closeMessageComposer())
          .then(() => this.refreshCurrentActivity())
          .catch(error => {
            console.error(`Error when updating the activity: ${error}`);
            this.showErrorMessage = true;
          });
      } else {
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
      }
    },
    resetComposer() {
      this.message = '';
      this.activityComposerActions.forEach(action => {
        if (action.component) {
          action.component.model.value = action.component.model.default.slice();
          this.actionsData[action.key].value = action.component.model.value;
        }
      });
      this.attachments = [];
      this.showErrorMessage = false;
    },
    refreshActivityStream() {
      const refreshButton = document.querySelector('.activityStreamStatus #RefreshButton');
      if(refreshButton) {
        refreshButton.click();
      }
    },
    closeMessageComposer: function() {
      if (this.activityId) {
        document.querySelector(`#editActivityComposer${this.activityId} .drawer`).classList.remove('open');
        document.querySelector(`#editActivityComposer${this.activityId} .composerActions`).style.display = 'none';
        document.querySelector(`.activityComposer .drawer-backdrop-activity${this.activityId}`).style.display = 'none';
      } else {
        this.showMessageComposer = false;
      }
    },
    executeAction(action) {
      executeExtensionAction(action, this.$refs[action.key]);
    },
    updateAttachments(attachments) {
      this.attachments = attachments;
    },
    refreshCurrentActivity() {
      const refreshButton = document.querySelector(`#activityContainer${this.activityId} #RefreshActivity${this.activityId}`);
      if(refreshButton) {
        refreshButton.click();
      }
    },
    escapeHTML: function(html) {
      const text = document.createElement('textarea');
      text.innerHTML = html;
      return text.value.replace(/>\s+</g, '><').trim();
    },
    cleanInitialActivityBody: function()  {
      const div = document.createElement('div');
      div.innerHTML = this.activityBody;
      const elements = div.querySelectorAll('#editActivityLinkPreview');
      Array.prototype.forEach.call( elements, function( node ) {
        node.parentNode.remove();
      });
      return div.innerHTML;
    }
  }
};
</script>
