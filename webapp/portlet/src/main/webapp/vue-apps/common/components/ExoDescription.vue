<template>
  <div>
    <span v-if="useCustomAddDescriptionContent && showAddDescription">
      <slot name="add-description"></slot>
    </span>
    <div  v-if="showAddDescription && !useCustomAddDescriptionContent" class="d-flex justify-center">
      <div class="mb-6">
        <div class="d-flex justify-center">
          <v-icon color="blue-grey lighten-4" large>mdi-message-text-outline</v-icon>
        </div>
        <div class="ml-2 font-weight-thin blue-grey--text lighten-6" >
          {{noDescriptionText}}
          </div>
        <a class="primary--text text-decoration-underline"
          @click="showAddDescription = false; showDescriptionEditor = true">
          {{addDescriptionText}}</a>
      </div>
    </div>
    <div v-if="!showAddDescription">
      <div v-if="!showDescriptionEditor" class="d-flex justify-center mb-3">
        <div
          class="text-center ml-8 mr-8"
          :data-text="placeholder"
          contentEditable="true"
          @click="showDescriptionEditor = true"
          v-sanitized-html="value">
          {{ value }}
        </div>
      </div>
      <div 
        v-else
        class="ml-5 mr-5">
        <exo-activity-rich-editor
          ref="descriptionEditor"
          v-model="value"
          :max-length="descriptionLength"
          ck-editor-type="activityContent"
          autofocus>
        </exo-activity-rich-editor>
        <v-btn
            class="btn mt-2 ml-auto mb-3 d-flex px-2 btn-primary v-btn v-btn--contained theme--light v-size--default"
            @click="saveDescription">
            {{applyButtonText}}
        </v-btn>
      </div>
    </div>   
  </div>
</template>

<script>
export default {
  props: {
    noDescriptionText: {
      type: String,
      default: function() {
        return this.$t('exoDescription.NoDescription');
      },
    },
    addDescriptionText: {
      type: String,
      default: function() {
        return this.$t('exoDescription.addDescription');
      },
    },
    applyButtonText: {
      type: String,
      default: function() {
        return this.$t('exoDescription.applyButtonText');
      },
    },
    description: {
      type: String,
      default: function() {
        return '';
      },
    },
    descriptionLength: {
      type: Number,
      default: function() {
        return 2000;
      },
    },
    useCustomAddDescriptionContent: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
  },
  data: () => ({
    value: null,
    showDescriptionEditor: false,
    showAddDescription: false,
  }),
  created(){
    this.value = this.description;
    if (this.useCustomAddDescriptionContent){
      this.$root.$on('add-new-description-click',()=>{this.showAddDescription = false; this.showDescriptionEditor = true;});
    }
    if (!this.description){
      this.showAddDescription = true;
    }
  },
  methods: {
    saveDescription(){
      this.showDescriptionEditor = false;
      if (!this.value){
        this.showAddDescription = true ;
      }
      this.$emit('applyDescription',this.value);
    }
  }
};
</script>