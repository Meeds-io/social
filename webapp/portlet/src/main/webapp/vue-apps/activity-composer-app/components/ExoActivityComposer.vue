<template>
  <div id="activityComposer" class="activityComposer pa-0">
    <div v-if="!standalone" class="openLink mb-4 text-truncate">
      <a @click="openMessageComposer()" class="primary--text">
        <i class="uiIconEdit"></i>
        {{ link.replace('{0}', postTarget) }}
      </a>
    </div>

    <v-app id="activityComposerApp" class="activityComposerApp VuetifyApp">
      <div :class="[showMessageComposer ? 'open' : '', activityId ? `editActivity editActivityDrawer${activityId}` : '']" class="drawer">
        <div class="header">
          <img src="/eXoSkin/skin/images/system/composer/composer.png" loading="lazy">
          <span> {{ $t('activity.composer.title') }}</span>
          <a
            class="closebtn"
            href="javascript:void(0)"
            @click="closeMessageComposer()">×</a>
        </div>
        <div class="content">
          <exo-activity-rich-editor
            v-if="showMessageComposer"
            :ref="ckEditorId"
            v-model="message"
            :ck-editor-type="ckEditorId"
            :max-length="MESSAGE_MAX_LENGTH"
            :template-params="templateParams"
            :placeholder="$t('activity.composer.placeholder').replace('{0}', MESSAGE_MAX_LENGTH)"
            autofocus />
          <div class="composerButtons">
            <div v-if="displayHintMessage" class="action">
              <i class="fas fa-pencil-alt fa-sm colorIcon" @click="activityComposerHintAction.onExecute(attachments)"></i>
              <a
                class="message"
                href="javascript:void(0)"
                @click="activityComposerHintAction.onExecute(attachments)">{{ getLabel(activityComposerHintAction.labelKey) }} </a>
            </div>
            <div v-else class="emptyMessage">
            </div>
            <div>
              <v-btn
                :disabled="postDisabled"
                :loading="loading"
                :aria-label="$t(`activity.composer.${composerAction}`)"
                type="button"
                class="primary btn no-box-shadow"
                @click="postMessage()">
                {{ $t(`activity.composer.${composerAction}`) }}
              </v-btn>
            </div>
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
                  <v-icon size="18">
                    fa-paperclip
                  </v-icon>
                </v-progress-circular>
                <div class="attachedFiles">{{ $t('attachments.drawer.title') }} ({{ attachments.length }})</div>
              </div>
            </v-app>
          </div>

          <div class="composerActions">
            <div
              v-for="action in activityComposerActions"
              :key="action.key"
              :class="`${action.appClass}Action`">
              <div
                v-if="action.key === 'file' || !activityId"
                class="actionItem"
                @click="executeAction(action, attachments)">
                <div class="actionItemIcon"><div :class="action.iconClass"></div></div>
                <div class="actionItemDescription">
                  <div class="actionLabel">{{ getLabel(action.labelKey) }}</div>
                  <div class="actionDescription">
                    <p>{{ getLabel(action.description) }}</p>
                  </div>
                </div>
              </div>
              <component
                v-dynamic-events="actionsEvents[action.key]"
                v-if="action.component"
                :ref="action.key"
                v-bind="action.component.props"
                v-model="actionsData[action.key].value"
                :is="action.component.name" />
            </div>
          </div>
        </div>
      </div>
    </v-app>
    <div
      v-show="showMessageComposer"
      :class="`drawer-backdrop-activity${activityId}`"
      class="drawer-backdrop"
      @click="closeMessageComposer()"></div>
  </div>
</template>

<script>
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
    activityParams: {
      type: Object,
      default: null
    },
    composerAction: {
      type: String,
      default: 'post'
    },
    standalone: {
      type: Boolean,
      default: false
    },
  },
  data() {
    return {
      MESSAGE_MAX_LENGTH: 1300,
      MESSAGE_TIMEOUT: 5000,
      postTarget: '',
      showMessageComposer: false,
      message: '',
      loading: false,
      showErrorMessage: false,
      activityBodyEdited: false,
      activityComposerActions: [],
      activityComposerHintAction: null,
      attachments: [],
      percent: 100,
      actionsData: [],
      actionsEvents: [],
      templateParams: {},
      ckEditorId: 'activityContent',
      link: `${this.$t('activity.composer.link')}`,
      filesToDetach: [],
      filesToAttach: []
    };
  },
  computed: {
    messageLength(){
      return this.message && this.message.length && this.$utils.htmlToText(this.message).length || 0;
    },
    postDisabled() {
      return (!this.attachments.length && !this.messageLength) || this.messageLength > this.MESSAGE_MAX_LENGTH || this.uploading || this.loading || (!!this.activityId && !this.activityBodyEdited);
    },
    activityType() {
      return this.attachments && this.attachments.length ? 'files:spaces' : '';
    },
    attachmentsProgress() {
      return this.attachments.length !== 0 ? this.uploadingProgress / this.attachments.length : 0;
    },
    uploading() {
      return this.attachments.length * this.percent !== this.uploadingProgress;
    },
    displayHintMessage() {
      return this.activityComposerHintAction && this.activityComposerHintAction.enabled && this.messageLength > this.MESSAGE_MAX_LENGTH;
    },
    uploadingProgress() {
      return this.attachments.map(attachment => {
        return typeof attachment.uploadProgress !== 'undefined' ? attachment.uploadProgress : this.percent;
      }).reduce((a, b) => a + b, 0);
    },
  },
  watch: {
    showErrorMessage: function(newVal) {
      if (newVal) {
        setTimeout(() => this.showErrorMessage = false, this.MESSAGE_TIMEOUT);
      }
    },
    message(newVal, oldVal) {
      // Do not compute again this.activityBodyEdited if it's made true
      // once, else, this can lead to performances issue when editing
      // An activity
      if (this.activityId && !this.activityBodyEdited) {
        this.activityBodyEdited = this.$utils.htmlToText(newVal) !== this.$utils.htmlToText(oldVal);
      }
    },
  },
  created() {
    document.addEventListener('activity-composer-edit-activity', this.editActivity);
    document.addEventListener('activity-composer-extension-updated', this.refreshExtensions);
    this.$root.$on('entity-attachments-updated', (attachments) => {
      this.attachments = attachments;
      this.activityBodyEdited = true;
    });
    this.$root.$on('remove-composer-attachment-item', attachment => {
      this.filesToDetach.push(attachment);
      this.activityBodyEdited = true;
    });
    this.$root.$on('abort-attachments-new-upload', (attachments) => {
      this.attachments = attachments;
    });
    this.$root.$on('add-composer-attachment-item', attachment => {
      if (this.activityId) {
        this.filesToAttach.push(attachment);
      }
    });
    this.$root.$on('add-new-created-document', (attachment) =>{
      if (this.activityId) {
        this.filesToAttach.push(attachment);
        this.activityBodyEdited = true;
      }
    });
    this.$root.$on('attachments-changed-from-drives', (attachments) => {
      if (this.activityId) {
        for (const attachment of attachments) {
          this.filesToAttach.push(attachment);
        }
      }
    });

    this.templateParams = this.activityParams || {};

    this.postTarget = eXo.env.portal.spaceDisplayName || '';
    if (!eXo.env.portal.spaceDisplayName){
      this.link = this.$t('activity.composer.post');
    }
  },
  mounted() {
    this.$nextTick()
      .then(() => this.refreshExtensions())
      .then(() => {
        this.message = this.activityBody;
        this.resetEdited();
        if (this.activityId) {
          this.editActivity();
        }
      });
  },
  methods: {
    refreshExtensions: function() {
      const urls = [];
      this.activityComposerActions = getActivityComposerActionExtensions();
      this.activityComposerHintAction = getActivityComposerHintActionExtensions();
      this.activityComposerActions.forEach(action => {
        if (action.component) {
          this.actionsData[action.key] = action.component.model.value;
          this.actionsEvents[action.key] = action.component.events;
        }
        if (action.resourceBundle) {
          urls.push(`/portal/rest/i18n/bundle/${action.resourceBundle}-${eXo.env.portal.language}.json`);
        }
      });
      return exoi18n.loadLanguageAsync(eXo.env.portal.language, urls)
        .then(i18n => {
          if (i18n) {
            // inject downloaded resource bundles in Vue context
            new Vue({i18n});
          }
        });
    },
    editActivity(params) {
      params = params && params.detail;
      if (params) {
        this.resetComposer();

        this.message = params.activityBody;
        this.activityId = params.activityId;
        this.composerAction = params.composerAction;
        this.templateParams = params.activityParams || {};
        this.resetEdited();
        this.initEntityAttachmentsList();
        this.$activityService.getActivityById(this.activityId)
          .then(activity => {
            if (activity.files) {
              activity.files.forEach(file => {
                if (!this.attachments.some(attachment => attachment.id === file.id)) {
                  this.$attachmentService.linkUploadedAttachmentToEntity(this.activityId, 'activity', file.id);
                }
              });
            }
          });
      }

      this.showMessageComposer = true;
      this.$nextTick(() => this.$refs[this.ckEditorId].setFocus());
    },
    openMessageComposer: function() {
      this.activityId = null;
      this.resetComposer();
      this.composerAction = 'post';
      this.message = '';
      this.resetEdited();
      this.templateParams = {};
      this.filesToDetach = [];
      this.filesToAttach = [];

      this.showMessageComposer = true;
      this.$nextTick(() => this.$refs[this.ckEditorId].setFocus());
    },
    getLabel: function(labelKey) {
      const label = this.$t(labelKey);
      return label ? label : labelKey;
    },
    postMessage() {
      // Using a ref to the editor component and the getMessage method is mandatory to
      // be sure to get the most up to date value of the message
      const msg = this.$refs[this.ckEditorId].getMessage();
      if (this.composerAction === 'update') {
        let activityType = this.activityType;
        if (this.templateParams && this.templateParams.link && !this.activityType) {
          activityType = 'LINK_ACTIVITY';
        }
        if (this.filesToDetach) {
          for (const index in this.filesToDetach) {
            this.$root.$emit('remove-attachment-item', this.filesToDetach[index]);
          }
          this.filesToDetach = [];
        }
        if (this.filesToAttach) {
          for (const index in this.filesToAttach) {
            this.$root.$emit('attach-composer-item', this.filesToAttach[index]);
          }
          this.filesToAttach = [];
        }
        this.loading = true;
        this.$activityService.updateActivity(this.activityId, msg, activityType, this.attachments, this.templateParams)
          .then(() => {
            document.dispatchEvent(new CustomEvent('activity-updated', {detail: this.activityId}));
            this.closeMessageComposer();
          })
          .then(() => this.refreshCurrentActivity())
          .then(() => {
            this.resetComposer();
          })
          .catch(error => {
            // eslint-disable-next-line no-console
            console.error(`Error when updating the activity: ${error}`);
            this.showErrorMessage = true;
          })
          .finally(() => this.loading = false);
      } else {
        let activityType = this.activityType;
        if (this.templateParams && this.templateParams.link && !this.activityType) {
          activityType = 'LINK_ACTIVITY';
        }
        this.loading = true;
        this.$activityService.createActivity(msg, activityType, this.attachments, eXo.env.portal.spaceId, this.templateParams)
          .then(() => {
            document.dispatchEvent(new CustomEvent('activity-created', {detail: this.activityId}));
            this.refreshActivityStream();
          })
          .then(() => this.closeMessageComposer())
          .then(() => {
            this.resetComposer();
          })
          .catch(error => {
            // eslint-disable-next-line no-console
            console.error(`Error when posting message: ${error}`);
            this.showErrorMessage = true;
          })
          .finally(() => this.loading = false);
      }
    },
    resetEdited() {
      this.$nextTick().then(() => this.activityBodyEdited = false);
    },
    resetComposer() {
      this.activityComposerActions.forEach(action => {
        if (action.component) {
          action.component.model.value = action.component.model.default.slice();
          this.actionsData[action.key].value = action.component.model.value;
          action.component.props.attachmentAppConfiguration = {};
          action.component.props.attachments = [];
        }
      });
      this.attachments = [];
      this.showErrorMessage = false;
      this.message = '';
      this.resetEdited();
    },
    refreshActivityStream() {
      const refreshButton = document.querySelector('.activityStreamStatus #RefreshButton');
      if (refreshButton) {
        refreshButton.click();
      }
    },
    closeMessageComposer: function () {
      if (eXo.openedDrawers.length === 0) {
        this.showMessageComposer = false;
        this.attachments = [];
        this.activityId = null;
        this.$refs[this.ckEditorId].unload();
      } else {
        this.$root.$emit('message-composer-closed');
      }
    },
    executeAction(action, attachments) {
      if (action.key === 'file') {
        if (eXo.env.portal.spaceId) {
          action.component.props.attachmentAppConfiguration = {
            'defaultDrive': {
              isSelected: true,
              name: `.spaces.${eXo.env.portal.spaceGroup}`,
              title: eXo.env.portal.spaceDisplayName,
            },
            'defaultFolder': 'Activity Stream Documents',
          };
        } else {
          action.component.props.attachmentAppConfiguration = {
            'defaultDrive': {
              isSelected: true,
              name: eXo.env.portal.userName,
              title: eXo.env.portal.spaceDisplayName,
            },
            'defaultFolder': 'Activity Stream Documents',
          };
        }
        if (this.activityId) {
          action.component.props.attachmentAppConfiguration.entityId = parseInt(this.activityId);
          action.component.props.attachmentAppConfiguration.entityType = 'activity';
          action.component.props.attachments = this.attachments;
        }
      }
      executeExtensionAction(action, this.$refs[action.key], attachments);
    },
    updateAttachments(attachments) {
      this.attachments = attachments;
    },
    refreshCurrentActivity() {
      const refreshButton = document.querySelector(`#activityContainer${this.activityId} #RefreshActivity${this.activityId}`);
      if (refreshButton) {
        refreshButton.click();
      }
    },
    escapeHTML: function(html) {
      const text = document.createElement('textarea');
      text.innerHTML = html;
      return text.value.replace(/>\s+</g, '><').trim();
    },
    initEntityAttachmentsList() {
      if (this.activityId) {
        return this.$attachmentService.getEntityAttachments('activity', this.activityId).then(attachments => {
          attachments.forEach(attachments => {
            attachments.name = attachments.title;
          });
          this.attachments = attachments;
        });
      }
    },
  }
};
</script>
