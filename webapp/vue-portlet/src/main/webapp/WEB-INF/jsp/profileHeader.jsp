<%@ page import="org.exoplatform.social.core.manager.IdentityManager"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%
  IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
  int maxUploadSize = identityManager.getImageUploadLimit();
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr theme--light profileHeaderOther"
    id="ProfileHeader">
    <script type="text/javascript">
      require(['PORTLET/social-vue-portlet/ProfileHeader'], app => app.init(<%=maxUploadSize%>));
    </script>
    <div class="v-application--wrap">
      <div class="v-responsive v-image profileBannerImg d-flex white"
        style="height: 240px; min-height: 240px; max-height: 240px;">
        <div class="v-responsive__content">
          <div class="flex fill-height">
            <div class="layout">
              <div class="flex d-flex profileHeaderTitle">
                <div role="dialog" class="v-dialog__container">
                  <!---->
                </div>
                <div
                  class="v-avatar align-start flex-grow-0 ml-3 my-3 profileHeaderAvatar skeleton-background"
                  style="height: 165px; min-width: 165px; width: 165px;">
                  <div class="v-responsive v-image">
                    <div class="v-responsive__content"></div>
                  </div>
                  <!---->
                </div>
                <div
                  class="profileHeaderText align-start d-flex flex-grow-0">
                  <div class="flex ma-auto pb-10 skeleton-text">
                    <div
                      class="v-card__title headline white--text skeleton-background skeleton-text skeleton-text-width skeleton-text-height pa-0 my-3">
                      &nbsp;</div>
                    <div dark=""
                      class="v-card__subtitle subtitle white--text skeleton-background skeleton-text skeleton-text-width skeleton-text-height pa-0 my-3">
                      &nbsp;</div>
                  </div>
                </div>
                <div class="flex-grow-1"></div>
                <div class="d-flex flex-grow-0 justify-end pr-4">
                  <div
                    class="v-input changeBannerButton mr-4 theme--light v-text-field v-text-field--is-booted v-file-input skeleton-background skeleton-text"
                    user="[object Object]">
                    <div class="v-input__prepend-outer">
                      <div class="v-input__icon v-input__icon--prepend">
                        <button type="button" aria-label="prepend icon"
                          class="v-icon notranslate v-icon--link fa fa-file-image theme--light"></button>
                      </div>
                    </div>
                    <div class="v-input__control">
                      <div class="v-input__slot">
                        <div class="v-text-field__slot">
                          <div class="v-file-input__text"></div>
                          <input accept="image/*" id="input-66"
                            type="file">
                        </div>
                        <div class="v-input__append-inner">
                          <div
                            class="v-input__icon v-input__icon--clear">
                            <button disabled="disabled" type="button"
                              aria-label="clear icon"
                              class="v-icon notranslate v-icon--disabled v-icon--link mdi mdi-close theme--light"></button>
                          </div>
                        </div>
                      </div>
                      <div class="v-text-field__details">
                        <div class="v-messages theme--light">
                          <div class="v-messages__wrapper"></div>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div id="profileHeaderActions"
                    class="mt-auto mr-3 profileHeaderOtherActions">
                    <!---->
                    <button type="button" disabled="disabled"
                      class="btn mx-auto connectUserButton v-btn v-btn--contained v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                      <span class="v-btn__content">
                        <!----> <span class="buttonText">
                          &nbsp;&nbsp;&nbsp;&nbsp; </span>
                      </span>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
