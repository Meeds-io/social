<template>
  <div id="activityComposer" class="activityComposer">
    <div v-if="!standalone" class="openLink">
      <a @click="openMessageComposer()">
        <i class="uiIconEdit"></i>
        {{ link.replace('{0}', postTarget) }}
      </a>
    </div>

    <v-app id="activityComposerApp" class="activityComposerApp VuetifyApp">
      <div :class="[showMessageComposer ? 'open' : '', activityId ? `editActivity editActivityDrawer${activityId}` : '']" class="drawer">
        <div class="header">
          <img src="/eXoSkin/skin/images/system/composer/composer.png">
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
                  <i class="uiIconAttach"></i>
                </v-progress-circular>
                <div class="attachedFiles">{{ $t('attachments.drawer.title') }} ({{ attachments.length }})</div>
              </div>
            </v-app>
          </div>

          <div v-show="activityId ? false : showMessageComposer" class="composerActions">
            <div
              v-for="action in activityComposerActions"
              :key="action.key"
              :class="`${action.appClass}Action`">
              <div class="actionItem" @click="executeAction(action, attachments)">
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
      activityComposerActions: [],
      activityComposerHintAction: null,
      attachments: [],
      percent: 100,
      actionsData: [],
      actionsEvents: [],
      templateParams: {},
      ckEditorId: 'activityContent',
      link: `${this.$t('activity.composer.link')}`,
    };
  },
  computed: {
    messageLength(){
      const pureText = this.message ? this.message.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, '').trim() : '';
      return pureText.length;
    },
    postDisabled: function() {
      const pureText = this.message ? this.message.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, '').trim() : '';
      return pureText.length === 0 && this.attachments.length === 0 || pureText.length > this.MESSAGE_MAX_LENGTH || this.uploading || this.loading || this.activityBodyEdited;
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
    displayHintMessage: function() {
      const pureText = this.message ? this.message.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, '').trim() : '';
      return this.activityComposerHintAction && pureText.length > this.MESSAGE_MAX_LENGTH;
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
    showErrorMessage: function(newVal) {
      if (newVal) {
        setTimeout(() => this.showErrorMessage = false, this.MESSAGE_TIMEOUT);
      }
    }
  },
  created() {
    document.addEventListener('activity-composer-edit-activity', this.editActivity);
    document.addEventListener('activity-composer-extension-updated', this.refreshExtensions);

    this.templateParams = this.activityParams || {};

    this.postTarget = eXo.env.portal.spaceDisplayName || '';
    if (!eXo.env.portal.spaceDisplayName){
      this.link = this.$t('activity.composer.post');
    }

    this.$nextTick().then(() => {
      this.$root.$emit('application-loaded');
    });
  },
  mounted() {
    this.$nextTick()
      .then(() => this.refreshExtensions())
      .then(() => {
        this.message = this.activityBody;
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
      }

      this.showMessageComposer = true;
      this.$nextTick(() => this.$refs[this.ckEditorId].setFocus());
    },
    openMessageComposer: function() {
      // If previously, the activity composer
      // was used to edit an activity, then purge its content
      if (this.activityId) {
        this.activityId = null;
        this.resetComposer();
      }

      this.composerAction = 'post';
      this.message = '';

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
    resetComposer() {
      this.activityComposerActions.forEach(action => {
        if (action.component) {
          action.component.model.value = action.component.model.default.slice();
          this.actionsData[action.key].value = action.component.model.value;
        }
      });
      this.attachments = [];
      this.showErrorMessage = false;
      this.message = '';
    },
    refreshActivityStream() {
      const refreshButton = document.querySelector('.activityStreamStatus #RefreshButton');
      if (refreshButton) {
        refreshButton.click();
      }
    },
    closeMessageComposer: function() {
      this.showMessageComposer = false;
      this.$refs[this.ckEditorId].unload();
    },
    executeAction(action, attachments) {
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
