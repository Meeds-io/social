<template>
  <v-dialog
    v-model="dialog"
    :width="width"
    :persistent="persistent"
    content-class="uiPopup"
    max-width="100vw"
    :aria-labelledby="titleId"
    role="dialog">
    <v-card class="elevation-12">
      <div class="ignore-vuetify-classes popupHeader ClearFix">
        <v-btn
          icon
          class="uiIconClose pull-right ignore-vuetify-classes"
          :aria-label="$t('label.close')"
          @click="close">
          <v-icon size="20">fa-times</v-icon>
        </v-btn>
        <!-- eslint-disable-next-line vue/no-v-html -->
        <span
          :id="titleId"
          class="ignore-vuetify-classes PopupTitle popupTitle text-truncate"
          v-html="title"></span>
      </div>
      <slot></slot>
      <v-card-actions v-if="!hideActions">
        <v-spacer />
        <button
          v-if="okLabel"
          :disabled="loading"
          :loading="loading"
          class="ignore-vuetify-classes btn btn-primary me-2"
          @click="close">
          {{ okLabel }}
        </button>
        <v-spacer />
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  props: {
    title: {
      type: String,
      default: ''
    },
    loading: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    okLabel: {
      type: String,
      default: function() {
        return 'ok';
      },
    },
    hideActions: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    persistent: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    width: {
      type: String,
      default: function() {
        return '500px';
      },
    },
  },
  data: () => ({
    dialog: false,
    previousFocus: null,
  }),
  computed: {
    titleId() {
      return `exo-modal-title-${this._uid}`;
    }
  },
  watch: {
    dialog() {
      if (this.dialog) {
        this.previousFocus = document.activeElement;
        this.$emit('dialog-opened');
        document.dispatchEvent(new CustomEvent('modalOpened'));
      } else {
        this.$emit('dialog-closed');
        document.dispatchEvent(new CustomEvent('modalClosed'));
      }
    }
  },
  mounted() {
    if (this.$el.closest('.layout-sticky-application')) {
      document.querySelector('#vuetify-apps').appendChild(this.$el);
    }
  },
  methods: {
    open() {
      this.dialog = true;
    },
    close() {
      this.dialog = false;
      if (this.previousFocus) {
        this.$nextTick(() => this.previousFocus?.focus());
        this.previousFocus = null;
      }
    },
  }
};
</script>
