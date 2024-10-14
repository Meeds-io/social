<template>
  <v-dialog
    v-model="dialog"
    :width="width"
    :persistent="persistent"
    content-class="uiPopup"
    max-width="100vw">
    <v-card class="elevation-12">
      <div class="ignore-vuetify-classes popupHeader ClearFix">
        <a
          class="uiIconClose pull-right"
          aria-hidden="true"
          @click="close"></a>
        <!-- eslint-disable-next-line vue/no-v-html -->
        <span class="ignore-vuetify-classes PopupTitle popupTitle text-truncate" v-html="title"></span>
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
  }),
  watch: {
    dialog() {
      if (this.dialog) {
        this.$emit('dialog-opened');
        document.dispatchEvent(new CustomEvent('modalOpened'));
      } else {
        this.$emit('dialog-closed');
        document.dispatchEvent(new CustomEvent('modalClosed'));
      }
    }
  },
  created() {
    document.addEventListener('keydown', this.closeOnEscape);
  },
  mounted() {
    if (this.$el.closest('.layout-sticky-application')) {
      document.querySelector('#vuetify-apps').appendChild(this.$el);
    }
  },
  beforeDestroy() {
    document.removeEventListener('keydown', this.closeOnEscape);
  },
  methods: {
    open() {
      this.dialog = true;
    },
    close() {
      this.dialog = false;
    },
    closeOnEscape(event) {
      if (event.key === 'Escape') {
        this.close();
      }
    },
  }
};
</script>
