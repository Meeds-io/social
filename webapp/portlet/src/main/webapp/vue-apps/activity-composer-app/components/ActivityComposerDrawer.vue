<template>
  <exo-drawer
    ref="activityComposerDrawer"
    v-model="drawer"
    disable-pull-to-refresh
    right
    fixed>
    <template #title>
      {{ $t('activity.composer.title') }}
    </template>
    <template #content>
      <v-card flat>
        <v-card-text>
          <exo-activity-rich-editor
            v-if="drawer"
            :ref="CK_EDITOR_ID"
            v-model="message"
            :max-length="MESSAGE_MAX_LENGTH"
            :template-params="templateParams"
            :placeholder="composerPlaceholder"
            ck-editor-type="activityContent"
            autofocus />
        </v-card-text>
        <v-card-actions class="d-flex px-4">
          <extension-registry-components
            :params="extensionParams"
            name="ActivityComposerAction"
            type="activity-composer-action" />
          <v-spacer />
          <v-btn
            :disabled="postDisabled"
            :loading="loading"
            :aria-label="$t(`activity.composer.${composerAction}`)"
            type="button"
            class="primary btn no-box-shadow ms-auto"
            @click="postMessage()">
            {{ composerActionLabel }}
          </v-btn>
        </v-card-actions>
        <v-card-text class="composerActions">
          <extension-registry-components
            v-if="activityId === null"
            :params="extensionParams"
            name="ActivityComposerFooterAction"
            type="activity-composer-footer-action">
            <template #header>
              <v-divider class="mb-6" />
            </template>
          </extension-registry-components>
        </v-card-text>
      </v-card>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data() {
    return {
      MESSAGE_MAX_LENGTH: 1300,
      MESSAGE_TIMEOUT: 5000,
      CK_EDITOR_ID: 'activityContent',
      activityId: null,
      spaceId: null,
      message: '',
      files: null,
      templateParams: {},
      drawer: false,
      activityBodyEdited: false,
      originalBody: '',
      messageEdited: false,
      activityType: null
    };
  },
  computed: {
    composerPlaceholder() {
      return this.$t('activity.composer.placeholder', {0: this.MESSAGE_MAX_LENGTH});
    },
    composerAction() {
      return this.activityId && 'update' || 'post';
    },
    composerActionLabel() {
      return this.$t(`activity.composer.${this.composerAction}`);
    },
    composerButtonLabel() {
      if (eXo.env.portal.spaceDisplayName){
        return this.$t('activity.composer.link', {0: eXo.env.portal.spaceDisplayName});
      } else {
        return this.$t('activity.composer.post');
      }
    },
    extensionParams() {
      return {
        activityId: this.activityId,
        spaceId: this.spaceId,
        files: this.files,
        templateParams: this.templateParams,
        message: this.message,
        maxMessageLength: this.MESSAGE_MAX_LENGTH,
        activityType: this.activityType
      };
    },
    messageLength(){
      return this.message && this.message.length && this.$utils.htmlToText(this.message).length || 0;
    },
    ckEditorInstance() {
      return this.drawer && this.$refs[this.CK_EDITOR_ID] || null;
    },
    postDisabled() {
      return (!this.messageLength && !this.activityBodyEdited) || this.messageLength > this.MESSAGE_MAX_LENGTH || this.loading || (!!this.activityId && !this.activityBodyEdited);
    },
  },
  watch: {
    message(newVal, oldVal) {
      // Do not compute again this.activityBodyEdited if it's made true
      // once, else, this can lead to performances issue when editing
      // An activity
      if (this.activityId && !this.activityBodyEdited) {
        this.activityBodyEdited = this.$utils.htmlToText(newVal) !== this.$utils.htmlToText(oldVal);
        this.messageEdited = this.$utils.htmlToText(newVal) !== this.$utils.htmlToText(this.originalBody);
      }
    },
  },
  created() {
    document.addEventListener('activity-composer-edit-activity', this.open);
    document.addEventListener('activity-composer-edited', this.isActivityBodyEdited);
    document.addEventListener('activity-composer-closed', this.close);
  },
  methods: {
    isActivityBodyEdited(event) {
      this.activityBodyEdited = (this.messageEdited && this.messageLength) || event.detail !== 0 || (event.detail === 0 && this.messageLength);
    },
    open(params) {
      params = params && params.detail;
      if (params) {
        this.message = params.activityBody;
        this.originalBody = params.activityBody;
        this.activityId = params.activityId;
        this.spaceId = params.spaceId;
        this.templateParams = params.activityParams || params.templateParams || {};
        this.files = params.files || [];
        this.activityType = params.activityType;
      } else {
        this.activityId = null;
        this.spaceId = null;
        this.message = '';
        this.templateParams = {};
        this.files = [];
        this.activityType = [];
      }
      this.$nextTick().then(() => {
        this.activityBodyEdited = false;
        this.messageEdited = false;
        this.$refs.activityComposerDrawer.open();
        document.dispatchEvent(new CustomEvent('message-composer-opened'));
      });
    },
    close() {
      if (this.ckEditorInstance) {
        this.ckEditorInstance.unload();
      }
      this.$nextTick().then(() => {
        this.$refs.activityComposerDrawer.close();
        this.$root.$emit('message-composer-closed');
      });
    },
    postMessage() {
      // Using a ref to the editor component and the getMessage method is mandatory to
      // be sure to get the most up to date value of the message
      const message = this.ckEditorInstance.getMessage();
      if (this.activityId) {
        let activityType = this.activityType;
        if (this.templateParams && this.templateParams.link && !this.activityType) {
          activityType = 'LINK_ACTIVITY';
        }
        this.loading = true;
        this.$activityService.updateActivity(this.activityId, message, activityType, this.files, this.templateParams)
          .then(() => {
            document.dispatchEvent(new CustomEvent('activity-updated', {detail: this.activityId}));
            this.close();
          })
          .catch(error => {
            // eslint-disable-next-line no-console
            console.error(`Error when updating the activity: ${error}`);
            // TODO Display error Message
          })
          .finally(() => this.loading = false);
      } else {
        let activityType = this.activityType;
        if (this.templateParams && this.templateParams.link && !this.activityType) {
          activityType = 'LINK_ACTIVITY';
        }
        if (this.activityType && this.activityType.length !== 0) {
          document.dispatchEvent(new CustomEvent('post-activity', {detail: message}));
        }
        else {
          this.loading = true;
          this.$activityService.createActivity(message, activityType, this.files, eXo.env.portal.spaceId, this.templateParams)
            .then(() => {
              document.dispatchEvent(new CustomEvent('activity-created', {detail: this.activityId}));
              this.close();
            })
            .catch(error => {
              // eslint-disable-next-line no-console
              console.error(`Error when posting message: ${error}`);
              // TODO Display error Message
            })
            .finally(() => this.loading = false);
        }
      }
    },
  },
};
</script>