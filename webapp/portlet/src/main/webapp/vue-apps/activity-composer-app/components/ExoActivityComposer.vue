<template>
  <div id="activityComposer" class="activityComposer pa-0">
    <div v-if="!standalone" class="openLink mb-4 text-truncate">
      <a @click="openComposerDrawer(true)" class="primary--text">
        <i class="uiIconEdit"></i>
        {{ composerButtonLabel }}
      </a>
    </div>
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
            <v-spacer />
            <v-btn
              :disabled="postDisabled"
              :loading="loading"
              :aria-label="$t(`activity.composer.${composerAction}`)"
              type="button"
              class="primary btn no-box-shadow ms-auto"
              @click="postMessage()">
              {{ $t(`activity.composer.${composerAction}`) }}
            </v-btn>
          </v-card-actions>
        </v-card>
      </template>
    </exo-drawer>
  </div>
</template>

<script>
export default {
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
      CK_EDITOR_ID: 'activityContent',
      message: '',
      templateParams: {},
      loading: false,
      drawer: false,
      activityBodyEdited: false,
    };
  },
  computed: {
    composerPlaceholder() {
      return this.$t('activity.composer.placeholder', {0: this.MESSAGE_MAX_LENGTH});
    },
    composerButtonLabel() {
      if (eXo.env.portal.spaceDisplayName){
        return this.$t('activity.composer.link', {0: eXo.env.portal.spaceDisplayName});
      } else {
        return this.$t('activity.composer.post');
      }
    },
    messageLength(){
      return this.message && this.message.length && this.$utils.htmlToText(this.message).length || 0;
    },
    ckEditorInstance() {
      return this.$refs[this.CK_EDITOR_ID];
    },
    postDisabled() {
      return !this.messageLength || this.messageLength > this.MESSAGE_MAX_LENGTH || this.loading || (!!this.activityId && !this.activityBodyEdited);
    },
  },
  watch: {
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
  },
  methods: {
    openComposerDrawer(isNew) {
      if (isNew) {
        this.resetComposer();
      }
      this.$refs.activityComposerDrawer.open();
    },
    closeComposerDrawer() {
      this.resetComposer();
      if (this.ckEditorInstance) {
        this.ckEditorInstance.unload();
      }
      this.$nextTick().then(() => {
        this.$refs.activityComposerDrawer.close();
        this.$root.$emit('message-composer-closed');
      });
    },
    resetComposer() {
      this.activityId = null;
      this.message = '';
      this.templateParams = {};
      this.loading = false;
    },
    editActivity(params) {
      this.resetComposer();

      params = params && params.detail;
      if (params) {
        this.message = params.activityBody;
        this.activityId = params.activityId;
        this.templateParams = params.activityParams || {};
        this.activityBodyEdited = false;
      }
      this.$nextTick(() => this.ckEditorInstance.setFocus());
      this.openComposerDrawer();
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
        this.$activityService.updateActivity(this.activityId, message, activityType, this.attachments, this.templateParams)
          .then(() => {
            document.dispatchEvent(new CustomEvent('activity-updated', {detail: this.activityId}));
            this.closeComposerDrawer();
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
        this.loading = true;
        this.$activityService.createActivity(message, activityType, this.attachments, eXo.env.portal.spaceId, this.templateParams)
          .then(() => {
            document.dispatchEvent(new CustomEvent('activity-created', {detail: this.activityId}));
            this.closeComposerDrawer();
          })
          .catch(error => {
            // eslint-disable-next-line no-console
            console.error(`Error when posting message: ${error}`);
            // TODO Display error Message
          })
          .finally(() => this.loading = false);
      }
    },
  },
};
</script>