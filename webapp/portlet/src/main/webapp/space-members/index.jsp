<%@page import="org.exoplatform.social.core.space.model.Space"%>
<%@page import="org.exoplatform.social.core.space.SpaceUtils"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="org.exoplatform.social.core.space.spi.SpaceService"%>
<%
  Object filter = request.getAttribute("filter");
  if (filter == null) {
    filter = "";
  } else {
    filter = ((String[]) filter)[0];
  }

  boolean isManager = false;

  Space space = SpaceUtils.getSpaceByContext();
  if (space != null) {
    SpaceService spaceService = ExoContainerContext.getService(SpaceService.class);
    isManager = spaceService.isSuperManager(request.getRemoteUser()) || spaceService.isManager(space, request.getRemoteUser());
  }
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application transparent v-application--is-ltr theme--light singlePageApplication"
    id="peopleListApplication" flat="">
    <script type="text/javascript">
      require(['PORTLET/social-portlet/MembersPortlet'],
          app => app.init('<%=filter%>', <%=isManager%>)
      );
    </script>
    <div class="v-application--wrap">
      <header
        class="v-sheet v-sheet--tile theme--light v-toolbar v-toolbar--flat"
        id="peopleListToolbar" style="height: 64px;">
        <div class="v-toolbar__content" style="height: 64px;">
          <div class="v-toolbar__title">
            <button type="button" disabled="disabled"
              class="btn pr-2 pl-0 mr-4 inviteUserToSpaceButton v-btn v-btn--contained v-btn--disabled theme--light v-size--default skeleton-text skeleton-background">
              <span class="v-btn__content"><span class="mx-2">&nbsp;</span>
                <span class="d-none d-sm-inline"> &nbsp; </span></span>
            </button>
          </div>
          <div
            class="showingPeopleText text-sub-title d-none d-sm-flex skeleton-text skeleton-background skeleton-border-radius">
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>
          <div class="spacer d-none d-sm-flex"></div>
          <div
            class="v-input inputPeopleFilter pa-0 mr-3 my-auto v-input--is-disabled theme--light v-text-field v-text-field--is-booted v-text-field--placeholder skeleton-text">
            <div class="v-input__control">
              <div class="v-input__slot">
                <div class="v-input__prepend-inner">
                  <div
                    class="v-input__icon v-input__icon--prepend-inner">
                    <i aria-hidden="true"
                      class="v-icon notranslate v-icon--disabled fa fa-filter theme--light"></i>
                  </div>
                </div>
                <div class="v-text-field__slot">
                  <input disabled="disabled" id="input-126"
                    placeholder="&amp;nbsp;" type="text">
                </div>
              </div>
              <div class="v-text-field__details">
                <div class="v-messages theme--light">
                  <div class="v-messages__wrapper"></div>
                </div>
              </div>
            </div>
          </div>
          <select disabled="disabled"
            class="selectPeopleFilter my-auto mr-2 subtitle-1 ignore-vuetify-classes d-none d-sm-inline skeleton-background skeleton-text"><option
              value="member">Members</option>
            <option value="manager">Managers</option>
            <option value="invited">Invited</option>
            <option value="pending">Requested</option></select>
          <button type="button"
            class="v-icon notranslate d-sm-none v-icon--link fa fa-filter theme--light skeleton-text"></button>
          <div role="dialog" class="v-dialog__container pa-0">
            <!---->
          </div>
        </div>
      </header>
      <div class="v-card v-card--flat v-sheet theme--light"
        ignore-profile-extensions="">
        <div id="peopleListBody" class="v-card__text pb-0">
          <div class="v-item-group theme--light">
            <div class="container pa-0">
              <div class="row ma-0 border-box-sizing">
                <div class="pa-0 col-md-6 col-lg-4 col-xl-3 col-12">
                  <div class="flex peopleCardFlip">
                    <div class="peopleCardFront">
                      <div
                        class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                        id="userMenuParent" front="">
                        <div
                          class="v-responsive v-image white--text align-start d-block peopleBannerImg"
                          style="height: 80px;">
                          <div class="v-responsive__content"></div>
                        </div>
                        <div class="peopleToolbarIcons px-2">
                          <button type="button" disabled="disabled"
                            class="peopleInfoIcon d-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                          <!---->
                          <div class="spacer"></div>
                          <button type="button" disabled="disabled"
                            class="peopleMenuIcon d-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                        </div>
                        <div class="peopleAvatar">
                          <a href="#"><div
                              class="v-responsive v-image mx-auto skeleton-background"
                              style="height: 65px; max-height: 65px; max-width: 65px; width: 65px;">
                              <div class="v-responsive__content"></div>
                            </div></a>
                        </div>
                        <div
                          class="v-card__text peopleCardBody align-center pt-2 pb-1">
                          <a href="#"
                            class="userFullname text-truncate font-weight-bold d-block text-sub-title">
                            &nbsp; </a>
                          <div
                            class="v-card__subtitle userPositionLabel text-truncate py-0">
                            &nbsp;</div>
                        </div>
                        <div class="v-card__actions peopleCardActions">
                          <div role="dialog" class="v-dialog__container">
                            <!---->
                          </div>
                          <button type="button" disabled="disabled"
                            class="btn mx-auto peopleRelationshipButton connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"><i
                              class="uiIconSocConnectUser peopleRelationshipIcon d-inline"></i>
                              <span
                              class="d-inline peopleRelationshipButtonText">
                                &nbsp; </span> <i aria-hidden="true"
                              class="v-icon notranslate d-none relationshipButtonMinus mdi mdi-plus theme--light"></i></span>
                          </button>
                        </div>
                      </div>
                    </div>
                    <!---->
                  </div>
                </div>
                <div class="pa-0 col-md-6 col-lg-4 col-xl-3 col-12">
                  <div class="flex peopleCardFlip">
                    <div class="peopleCardFront">
                      <div
                        class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                        id="userMenuParent" front="">
                        <div
                          class="v-responsive v-image white--text align-start d-block peopleBannerImg"
                          style="height: 80px;">
                          <div class="v-responsive__content"></div>
                        </div>
                        <div class="peopleToolbarIcons px-2">
                          <button type="button" disabled="disabled"
                            class="peopleInfoIcon d-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                          <!---->
                          <div class="spacer"></div>
                          <button type="button" disabled="disabled"
                            class="peopleMenuIcon d-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                        </div>
                        <div class="peopleAvatar">
                          <a href="#"><div
                              class="v-responsive v-image mx-auto skeleton-background"
                              style="height: 65px; max-height: 65px; max-width: 65px; width: 65px;">
                              <div class="v-responsive__content"></div>
                            </div></a>
                        </div>
                        <div
                          class="v-card__text peopleCardBody align-center pt-2 pb-1">
                          <a href="#"
                            class="userFullname text-truncate font-weight-bold d-block text-sub-title">
                            &nbsp; </a>
                          <div
                            class="v-card__subtitle userPositionLabel text-truncate py-0">
                            &nbsp;</div>
                        </div>
                        <div class="v-card__actions peopleCardActions">
                          <div role="dialog" class="v-dialog__container">
                            <!---->
                          </div>
                          <button type="button" disabled="disabled"
                            class="btn mx-auto peopleRelationshipButton connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"><i
                              class="uiIconSocConnectUser peopleRelationshipIcon d-inline"></i>
                              <span
                              class="d-inline peopleRelationshipButtonText">
                                &nbsp; </span> <i aria-hidden="true"
                              class="v-icon notranslate d-none relationshipButtonMinus mdi mdi-plus theme--light"></i></span>
                          </button>
                        </div>
                      </div>
                    </div>
                    <!---->
                  </div>
                </div>
                <div class="pa-0 col-md-6 col-lg-4 col-xl-3 col-12">
                  <div class="flex peopleCardFlip">
                    <div class="peopleCardFront">
                      <div
                        class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                        id="userMenuParent" front="">
                        <div
                          class="v-responsive v-image white--text align-start d-block peopleBannerImg"
                          style="height: 80px;">
                          <div class="v-responsive__content"></div>
                        </div>
                        <div class="peopleToolbarIcons px-2">
                          <button type="button" disabled="disabled"
                            class="peopleInfoIcon d-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                          <!---->
                          <div class="spacer"></div>
                          <button type="button" disabled="disabled"
                            class="peopleMenuIcon d-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                        </div>
                        <div class="peopleAvatar">
                          <a href="#"><div
                              class="v-responsive v-image mx-auto skeleton-background"
                              style="height: 65px; max-height: 65px; max-width: 65px; width: 65px;">
                              <div class="v-responsive__content"></div>
                            </div></a>
                        </div>
                        <div
                          class="v-card__text peopleCardBody align-center pt-2 pb-1">
                          <a href="#"
                            class="userFullname text-truncate font-weight-bold d-block text-sub-title">
                            &nbsp; </a>
                          <div
                            class="v-card__subtitle userPositionLabel text-truncate py-0">
                            &nbsp;</div>
                        </div>
                        <div class="v-card__actions peopleCardActions">
                          <div role="dialog" class="v-dialog__container">
                            <!---->
                          </div>
                          <button type="button" disabled="disabled"
                            class="btn mx-auto peopleRelationshipButton connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"><i
                              class="uiIconSocConnectUser peopleRelationshipIcon d-inline"></i>
                              <span
                              class="d-inline peopleRelationshipButtonText">
                                &nbsp; </span> <i aria-hidden="true"
                              class="v-icon notranslate d-none relationshipButtonMinus mdi mdi-plus theme--light"></i></span>
                          </button>
                        </div>
                      </div>
                    </div>
                    <!---->
                  </div>
                </div>
                <div class="pa-0 col-md-6 col-lg-4 col-xl-3 col-12">
                  <div class="flex peopleCardFlip">
                    <div class="peopleCardFront">
                      <div
                        class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                        id="userMenuParent" front="">
                        <div
                          class="v-responsive v-image white--text align-start d-block peopleBannerImg"
                          style="height: 80px;">
                          <div class="v-responsive__content"></div>
                        </div>
                        <div class="peopleToolbarIcons px-2">
                          <button type="button" disabled="disabled"
                            class="peopleInfoIcon d-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                          <!---->
                          <div class="spacer"></div>
                          <button type="button" disabled="disabled"
                            class="peopleMenuIcon d-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                        </div>
                        <div class="peopleAvatar">
                          <a href="#"><div
                              class="v-responsive v-image mx-auto skeleton-background"
                              style="height: 65px; max-height: 65px; max-width: 65px; width: 65px;">
                              <div class="v-responsive__content"></div>
                            </div></a>
                        </div>
                        <div
                          class="v-card__text peopleCardBody align-center pt-2 pb-1">
                          <a href="#"
                            class="userFullname text-truncate font-weight-bold d-block text-sub-title">
                            &nbsp; </a>
                          <div
                            class="v-card__subtitle userPositionLabel text-truncate py-0">
                            &nbsp;</div>
                        </div>
                        <div class="v-card__actions peopleCardActions">
                          <div role="dialog" class="v-dialog__container">
                            <!---->
                          </div>
                          <button type="button" disabled="disabled"
                            class="btn mx-auto peopleRelationshipButton connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"><i
                              class="uiIconSocConnectUser peopleRelationshipIcon d-inline"></i>
                              <span
                              class="d-inline peopleRelationshipButtonText">
                                &nbsp; </span> <i aria-hidden="true"
                              class="v-icon notranslate d-none relationshipButtonMinus mdi mdi-plus theme--light"></i></span>
                          </button>
                        </div>
                      </div>
                    </div>
                    <!---->
                  </div>
                </div>
                <div class="pa-0 col-md-6 col-lg-4 col-xl-3 col-12">
                  <div class="flex peopleCardFlip">
                    <div class="peopleCardFront">
                      <div
                        class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                        id="userMenuParent" front="">
                        <div
                          class="v-responsive v-image white--text align-start d-block peopleBannerImg"
                          style="height: 80px;">
                          <div class="v-responsive__content"></div>
                        </div>
                        <div class="peopleToolbarIcons px-2">
                          <button type="button" disabled="disabled"
                            class="peopleInfoIcon d-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                          <!---->
                          <div class="spacer"></div>
                          <button type="button" disabled="disabled"
                            class="peopleMenuIcon d-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                        </div>
                        <div class="peopleAvatar">
                          <a href="#"><div
                              class="v-responsive v-image mx-auto skeleton-background"
                              style="height: 65px; max-height: 65px; max-width: 65px; width: 65px;">
                              <div class="v-responsive__content"></div>
                            </div></a>
                        </div>
                        <div
                          class="v-card__text peopleCardBody align-center pt-2 pb-1">
                          <a href="#"
                            class="userFullname text-truncate font-weight-bold d-block text-sub-title">
                            &nbsp; </a>
                          <div
                            class="v-card__subtitle userPositionLabel text-truncate py-0">
                            &nbsp;</div>
                        </div>
                        <div class="v-card__actions peopleCardActions">
                          <div role="dialog" class="v-dialog__container">
                            <!---->
                          </div>
                          <button type="button" disabled="disabled"
                            class="btn mx-auto peopleRelationshipButton connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"><i
                              class="uiIconSocConnectUser peopleRelationshipIcon d-inline"></i>
                              <span
                              class="d-inline peopleRelationshipButtonText">
                                &nbsp; </span> <i aria-hidden="true"
                              class="v-icon notranslate d-none relationshipButtonMinus mdi mdi-plus theme--light"></i></span>
                          </button>
                        </div>
                      </div>
                    </div>
                    <!---->
                  </div>
                </div>
                <div class="pa-0 col-md-6 col-lg-4 col-xl-3 col-12">
                  <div class="flex peopleCardFlip">
                    <div class="peopleCardFront">
                      <div
                        class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                        id="userMenuParent" front="">
                        <div
                          class="v-responsive v-image white--text align-start d-block peopleBannerImg"
                          style="height: 80px;">
                          <div class="v-responsive__content"></div>
                        </div>
                        <div class="peopleToolbarIcons px-2">
                          <button type="button" disabled="disabled"
                            class="peopleInfoIcon d-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                          <!---->
                          <div class="spacer"></div>
                          <button type="button" disabled="disabled"
                            class="peopleMenuIcon d-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                        </div>
                        <div class="peopleAvatar">
                          <a href="#"><div
                              class="v-responsive v-image mx-auto skeleton-background"
                              style="height: 65px; max-height: 65px; max-width: 65px; width: 65px;">
                              <div class="v-responsive__content"></div>
                            </div></a>
                        </div>
                        <div
                          class="v-card__text peopleCardBody align-center pt-2 pb-1">
                          <a href="#"
                            class="userFullname text-truncate font-weight-bold d-block text-sub-title">
                            &nbsp; </a>
                          <div
                            class="v-card__subtitle userPositionLabel text-truncate py-0">
                            &nbsp;</div>
                        </div>
                        <div class="v-card__actions peopleCardActions">
                          <div role="dialog" class="v-dialog__container">
                            <!---->
                          </div>
                          <button type="button" disabled="disabled"
                            class="btn mx-auto peopleRelationshipButton connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"><i
                              class="uiIconSocConnectUser peopleRelationshipIcon d-inline"></i>
                              <span
                              class="d-inline peopleRelationshipButtonText">
                                &nbsp; </span> <i aria-hidden="true"
                              class="v-icon notranslate d-none relationshipButtonMinus mdi mdi-plus theme--light"></i></span>
                          </button>
                        </div>
                      </div>
                    </div>
                    <!---->
                  </div>
                </div>
                <div class="pa-0 col-md-6 col-lg-4 col-xl-3 col-12">
                  <div class="flex peopleCardFlip">
                    <div class="peopleCardFront">
                      <div
                        class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                        id="userMenuParent" front="">
                        <div
                          class="v-responsive v-image white--text align-start d-block peopleBannerImg"
                          style="height: 80px;">
                          <div class="v-responsive__content"></div>
                        </div>
                        <div class="peopleToolbarIcons px-2">
                          <button type="button" disabled="disabled"
                            class="peopleInfoIcon d-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                          <!---->
                          <div class="spacer"></div>
                          <button type="button" disabled="disabled"
                            class="peopleMenuIcon d-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                        </div>
                        <div class="peopleAvatar">
                          <a href="#"><div
                              class="v-responsive v-image mx-auto skeleton-background"
                              style="height: 65px; max-height: 65px; max-width: 65px; width: 65px;">
                              <div class="v-responsive__content"></div>
                            </div></a>
                        </div>
                        <div
                          class="v-card__text peopleCardBody align-center pt-2 pb-1">
                          <a href="#"
                            class="userFullname text-truncate font-weight-bold d-block text-sub-title">
                            &nbsp; </a>
                          <div
                            class="v-card__subtitle userPositionLabel text-truncate py-0">
                            &nbsp;</div>
                        </div>
                        <div class="v-card__actions peopleCardActions">
                          <div role="dialog" class="v-dialog__container">
                            <!---->
                          </div>
                          <button type="button" disabled="disabled"
                            class="btn mx-auto peopleRelationshipButton connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"><i
                              class="uiIconSocConnectUser peopleRelationshipIcon d-inline"></i>
                              <span
                              class="d-inline peopleRelationshipButtonText">
                                &nbsp; </span> <i aria-hidden="true"
                              class="v-icon notranslate d-none relationshipButtonMinus mdi mdi-plus theme--light"></i></span>
                          </button>
                        </div>
                      </div>
                    </div>
                    <!---->
                  </div>
                </div>
                <div class="pa-0 col-md-6 col-lg-4 col-xl-3 col-12">
                  <div class="flex peopleCardFlip">
                    <div class="peopleCardFront">
                      <div
                        class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                        id="userMenuParent" front="">
                        <div
                          class="v-responsive v-image white--text align-start d-block peopleBannerImg"
                          style="height: 80px;">
                          <div class="v-responsive__content"></div>
                        </div>
                        <div class="peopleToolbarIcons px-2">
                          <button type="button" disabled="disabled"
                            class="peopleInfoIcon d-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                          <!---->
                          <div class="spacer"></div>
                          <button type="button" disabled="disabled"
                            class="peopleMenuIcon d-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                        </div>
                        <div class="peopleAvatar">
                          <a href="#"><div
                              class="v-responsive v-image mx-auto skeleton-background"
                              style="height: 65px; max-height: 65px; max-width: 65px; width: 65px;">
                              <div class="v-responsive__content"></div>
                            </div></a>
                        </div>
                        <div
                          class="v-card__text peopleCardBody align-center pt-2 pb-1">
                          <a href="#"
                            class="userFullname text-truncate font-weight-bold d-block text-sub-title">
                            &nbsp; </a>
                          <div
                            class="v-card__subtitle userPositionLabel text-truncate py-0">
                            &nbsp;</div>
                        </div>
                        <div class="v-card__actions peopleCardActions">
                          <div role="dialog" class="v-dialog__container">
                            <!---->
                          </div>
                          <button type="button" disabled="disabled"
                            class="btn mx-auto peopleRelationshipButton connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"><i
                              class="uiIconSocConnectUser peopleRelationshipIcon d-inline"></i>
                              <span
                              class="d-inline peopleRelationshipButtonText">
                                &nbsp; </span> <i aria-hidden="true"
                              class="v-icon notranslate d-none relationshipButtonMinus mdi mdi-plus theme--light"></i></span>
                          </button>
                        </div>
                      </div>
                    </div>
                    <!---->
                  </div>
                </div>
                <div class="pa-0 col-md-6 col-lg-4 col-xl-3 col-12">
                  <div class="flex peopleCardFlip">
                    <div class="peopleCardFront">
                      <div
                        class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                        id="userMenuParent" front="">
                        <div
                          class="v-responsive v-image white--text align-start d-block peopleBannerImg"
                          style="height: 80px;">
                          <div class="v-responsive__content"></div>
                        </div>
                        <div class="peopleToolbarIcons px-2">
                          <button type="button" disabled="disabled"
                            class="peopleInfoIcon d-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                          <!---->
                          <div class="spacer"></div>
                          <button type="button" disabled="disabled"
                            class="peopleMenuIcon d-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                        </div>
                        <div class="peopleAvatar">
                          <a href="#"><div
                              class="v-responsive v-image mx-auto skeleton-background"
                              style="height: 65px; max-height: 65px; max-width: 65px; width: 65px;">
                              <div class="v-responsive__content"></div>
                            </div></a>
                        </div>
                        <div
                          class="v-card__text peopleCardBody align-center pt-2 pb-1">
                          <a href="#"
                            class="userFullname text-truncate font-weight-bold d-block text-sub-title">
                            &nbsp; </a>
                          <div
                            class="v-card__subtitle userPositionLabel text-truncate py-0">
                            &nbsp;</div>
                        </div>
                        <div class="v-card__actions peopleCardActions">
                          <div role="dialog" class="v-dialog__container">
                            <!---->
                          </div>
                          <button type="button" disabled="disabled"
                            class="btn mx-auto peopleRelationshipButton connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"><i
                              class="uiIconSocConnectUser peopleRelationshipIcon d-inline"></i>
                              <span
                              class="d-inline peopleRelationshipButtonText">
                                &nbsp; </span> <i aria-hidden="true"
                              class="v-icon notranslate d-none relationshipButtonMinus mdi mdi-plus theme--light"></i></span>
                          </button>
                        </div>
                      </div>
                    </div>
                    <!---->
                  </div>
                </div>
                <div class="pa-0 col-md-6 col-lg-4 col-xl-3 col-12">
                  <div class="flex peopleCardFlip">
                    <div class="peopleCardFront">
                      <div
                        class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                        id="userMenuParent" front="">
                        <div
                          class="v-responsive v-image white--text align-start d-block peopleBannerImg"
                          style="height: 80px;">
                          <div class="v-responsive__content"></div>
                        </div>
                        <div class="peopleToolbarIcons px-2">
                          <button type="button" disabled="disabled"
                            class="peopleInfoIcon d-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                          <!---->
                          <div class="spacer"></div>
                          <button type="button" disabled="disabled"
                            class="peopleMenuIcon d-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                        </div>
                        <div class="peopleAvatar">
                          <a href="#"><div
                              class="v-responsive v-image mx-auto skeleton-background"
                              style="height: 65px; max-height: 65px; max-width: 65px; width: 65px;">
                              <div class="v-responsive__content"></div>
                            </div></a>
                        </div>
                        <div
                          class="v-card__text peopleCardBody align-center pt-2 pb-1">
                          <a href="#"
                            class="userFullname text-truncate font-weight-bold d-block text-sub-title">
                            &nbsp; </a>
                          <div
                            class="v-card__subtitle userPositionLabel text-truncate py-0">
                            &nbsp;</div>
                        </div>
                        <div class="v-card__actions peopleCardActions">
                          <div role="dialog" class="v-dialog__container">
                            <!---->
                          </div>
                          <button type="button" disabled="disabled"
                            class="btn mx-auto peopleRelationshipButton connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"><i
                              class="uiIconSocConnectUser peopleRelationshipIcon d-inline"></i>
                              <span
                              class="d-inline peopleRelationshipButtonText">
                                &nbsp; </span> <i aria-hidden="true"
                              class="v-icon notranslate d-none relationshipButtonMinus mdi mdi-plus theme--light"></i></span>
                          </button>
                        </div>
                      </div>
                    </div>
                    <!---->
                  </div>
                </div>
                <div class="pa-0 col-md-6 col-lg-4 col-xl-3 col-12">
                  <div class="flex peopleCardFlip">
                    <div class="peopleCardFront">
                      <div
                        class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                        id="userMenuParent" front="">
                        <div
                          class="v-responsive v-image white--text align-start d-block peopleBannerImg"
                          style="height: 80px;">
                          <div class="v-responsive__content"></div>
                        </div>
                        <div class="peopleToolbarIcons px-2">
                          <button type="button" disabled="disabled"
                            class="peopleInfoIcon d-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                          <!---->
                          <div class="spacer"></div>
                          <button type="button" disabled="disabled"
                            class="peopleMenuIcon d-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                        </div>
                        <div class="peopleAvatar">
                          <a href="#"><div
                              class="v-responsive v-image mx-auto skeleton-background"
                              style="height: 65px; max-height: 65px; max-width: 65px; width: 65px;">
                              <div class="v-responsive__content"></div>
                            </div></a>
                        </div>
                        <div
                          class="v-card__text peopleCardBody align-center pt-2 pb-1">
                          <a href="#"
                            class="userFullname text-truncate font-weight-bold d-block text-sub-title">
                            &nbsp; </a>
                          <div
                            class="v-card__subtitle userPositionLabel text-truncate py-0">
                            &nbsp;</div>
                        </div>
                        <div class="v-card__actions peopleCardActions">
                          <div role="dialog" class="v-dialog__container">
                            <!---->
                          </div>
                          <button type="button" disabled="disabled"
                            class="btn mx-auto peopleRelationshipButton connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"><i
                              class="uiIconSocConnectUser peopleRelationshipIcon d-inline"></i>
                              <span
                              class="d-inline peopleRelationshipButtonText">
                                &nbsp; </span> <i aria-hidden="true"
                              class="v-icon notranslate d-none relationshipButtonMinus mdi mdi-plus theme--light"></i></span>
                          </button>
                        </div>
                      </div>
                    </div>
                    <!---->
                  </div>
                </div>
                <div class="pa-0 col-md-6 col-lg-4 col-xl-3 col-12">
                  <div class="flex peopleCardFlip">
                    <div class="peopleCardFront">
                      <div
                        class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                        id="userMenuParent" front="">
                        <div
                          class="v-responsive v-image white--text align-start d-block peopleBannerImg"
                          style="height: 80px;">
                          <div class="v-responsive__content"></div>
                        </div>
                        <div class="peopleToolbarIcons px-2">
                          <button type="button" disabled="disabled"
                            class="peopleInfoIcon d-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                          <!---->
                          <div class="spacer"></div>
                          <button type="button" disabled="disabled"
                            class="peopleMenuIcon d-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                        </div>
                        <div class="peopleAvatar">
                          <a href="#"><div
                              class="v-responsive v-image mx-auto skeleton-background"
                              style="height: 65px; max-height: 65px; max-width: 65px; width: 65px;">
                              <div class="v-responsive__content"></div>
                            </div></a>
                        </div>
                        <div
                          class="v-card__text peopleCardBody align-center pt-2 pb-1">
                          <a href="#"
                            class="userFullname text-truncate font-weight-bold d-block text-sub-title">
                            &nbsp; </a>
                          <div
                            class="v-card__subtitle userPositionLabel text-truncate py-0">
                            &nbsp;</div>
                        </div>
                        <div class="v-card__actions peopleCardActions">
                          <div role="dialog" class="v-dialog__container">
                            <!---->
                          </div>
                          <button type="button" disabled="disabled"
                            class="btn mx-auto peopleRelationshipButton connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"><i
                              class="uiIconSocConnectUser peopleRelationshipIcon d-inline"></i>
                              <span
                              class="d-inline peopleRelationshipButtonText">
                                &nbsp; </span> <i aria-hidden="true"
                              class="v-icon notranslate d-none relationshipButtonMinus mdi mdi-plus theme--light"></i></span>
                          </button>
                        </div>
                      </div>
                    </div>
                    <!---->
                  </div>
                </div>
                <div class="pa-0 col-md-6 col-lg-4 col-xl-3 col-12">
                  <div class="flex peopleCardFlip">
                    <div class="peopleCardFront">
                      <div
                        class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                        id="userMenuParent" front="">
                        <div
                          class="v-responsive v-image white--text align-start d-block peopleBannerImg"
                          style="height: 80px;">
                          <div class="v-responsive__content"></div>
                        </div>
                        <div class="peopleToolbarIcons px-2">
                          <button type="button" disabled="disabled"
                            class="peopleInfoIcon d-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                          <!---->
                          <div class="spacer"></div>
                          <button type="button" disabled="disabled"
                            class="peopleMenuIcon d-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                        </div>
                        <div class="peopleAvatar">
                          <a href="#"><div
                              class="v-responsive v-image mx-auto skeleton-background"
                              style="height: 65px; max-height: 65px; max-width: 65px; width: 65px;">
                              <div class="v-responsive__content"></div>
                            </div></a>
                        </div>
                        <div
                          class="v-card__text peopleCardBody align-center pt-2 pb-1">
                          <a href="#"
                            class="userFullname text-truncate font-weight-bold d-block text-sub-title">
                            &nbsp; </a>
                          <div
                            class="v-card__subtitle userPositionLabel text-truncate py-0">
                            &nbsp;</div>
                        </div>
                        <div class="v-card__actions peopleCardActions">
                          <div role="dialog" class="v-dialog__container">
                            <!---->
                          </div>
                          <button type="button" disabled="disabled"
                            class="btn mx-auto peopleRelationshipButton connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"><i
                              class="uiIconSocConnectUser peopleRelationshipIcon d-inline"></i>
                              <span
                              class="d-inline peopleRelationshipButtonText">
                                &nbsp; </span> <i aria-hidden="true"
                              class="v-icon notranslate d-none relationshipButtonMinus mdi mdi-plus theme--light"></i></span>
                          </button>
                        </div>
                      </div>
                    </div>
                    <!---->
                  </div>
                </div>
                <div class="pa-0 col-md-6 col-lg-4 col-xl-3 col-12">
                  <div class="flex peopleCardFlip">
                    <div class="peopleCardFront">
                      <div
                        class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                        id="userMenuParent" front="">
                        <div
                          class="v-responsive v-image white--text align-start d-block peopleBannerImg"
                          style="height: 80px;">
                          <div class="v-responsive__content"></div>
                        </div>
                        <div class="peopleToolbarIcons px-2">
                          <button type="button" disabled="disabled"
                            class="peopleInfoIcon d-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                          <!---->
                          <div class="spacer"></div>
                          <button type="button" disabled="disabled"
                            class="peopleMenuIcon d-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                        </div>
                        <div class="peopleAvatar">
                          <a href="#"><div
                              class="v-responsive v-image mx-auto skeleton-background"
                              style="height: 65px; max-height: 65px; max-width: 65px; width: 65px;">
                              <div class="v-responsive__content"></div>
                            </div></a>
                        </div>
                        <div
                          class="v-card__text peopleCardBody align-center pt-2 pb-1">
                          <a href="#"
                            class="userFullname text-truncate font-weight-bold d-block text-sub-title">
                            &nbsp; </a>
                          <div
                            class="v-card__subtitle userPositionLabel text-truncate py-0">
                            &nbsp;</div>
                        </div>
                        <div class="v-card__actions peopleCardActions">
                          <div role="dialog" class="v-dialog__container">
                            <!---->
                          </div>
                          <button type="button" disabled="disabled"
                            class="btn mx-auto peopleRelationshipButton connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"><i
                              class="uiIconSocConnectUser peopleRelationshipIcon d-inline"></i>
                              <span
                              class="d-inline peopleRelationshipButtonText">
                                &nbsp; </span> <i aria-hidden="true"
                              class="v-icon notranslate d-none relationshipButtonMinus mdi mdi-plus theme--light"></i></span>
                          </button>
                        </div>
                      </div>
                    </div>
                    <!---->
                  </div>
                </div>
                <div class="pa-0 col-md-6 col-lg-4 col-xl-3 col-12">
                  <div class="flex peopleCardFlip">
                    <div class="peopleCardFront">
                      <div
                        class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                        id="userMenuParent" front="">
                        <div
                          class="v-responsive v-image white--text align-start d-block peopleBannerImg"
                          style="height: 80px;">
                          <div class="v-responsive__content"></div>
                        </div>
                        <div class="peopleToolbarIcons px-2">
                          <button type="button" disabled="disabled"
                            class="peopleInfoIcon d-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                          <!---->
                          <div class="spacer"></div>
                          <button type="button" disabled="disabled"
                            class="peopleMenuIcon d-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                        </div>
                        <div class="peopleAvatar">
                          <a href="#"><div
                              class="v-responsive v-image mx-auto skeleton-background"
                              style="height: 65px; max-height: 65px; max-width: 65px; width: 65px;">
                              <div class="v-responsive__content"></div>
                            </div></a>
                        </div>
                        <div
                          class="v-card__text peopleCardBody align-center pt-2 pb-1">
                          <a href="#"
                            class="userFullname text-truncate font-weight-bold d-block text-sub-title">
                            &nbsp; </a>
                          <div
                            class="v-card__subtitle userPositionLabel text-truncate py-0">
                            &nbsp;</div>
                        </div>
                        <div class="v-card__actions peopleCardActions">
                          <div role="dialog" class="v-dialog__container">
                            <!---->
                          </div>
                          <button type="button" disabled="disabled"
                            class="btn mx-auto peopleRelationshipButton connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"><i
                              class="uiIconSocConnectUser peopleRelationshipIcon d-inline"></i>
                              <span
                              class="d-inline peopleRelationshipButtonText">
                                &nbsp; </span> <i aria-hidden="true"
                              class="v-icon notranslate d-none relationshipButtonMinus mdi mdi-plus theme--light"></i></span>
                          </button>
                        </div>
                      </div>
                    </div>
                    <!---->
                  </div>
                </div>
                <div class="pa-0 col-md-6 col-lg-4 col-xl-3 col-12">
                  <div class="flex peopleCardFlip">
                    <div class="peopleCardFront">
                      <div
                        class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                        id="userMenuParent" front="">
                        <div
                          class="v-responsive v-image white--text align-start d-block peopleBannerImg"
                          style="height: 80px;">
                          <div class="v-responsive__content"></div>
                        </div>
                        <div class="peopleToolbarIcons px-2">
                          <button type="button" disabled="disabled"
                            class="peopleInfoIcon d-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                          <!---->
                          <div class="spacer"></div>
                          <button type="button" disabled="disabled"
                            class="peopleMenuIcon d-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                        </div>
                        <div class="peopleAvatar">
                          <a href="#"><div
                              class="v-responsive v-image mx-auto skeleton-background"
                              style="height: 65px; max-height: 65px; max-width: 65px; width: 65px;">
                              <div class="v-responsive__content"></div>
                            </div></a>
                        </div>
                        <div
                          class="v-card__text peopleCardBody align-center pt-2 pb-1">
                          <a href="#"
                            class="userFullname text-truncate font-weight-bold d-block text-sub-title">
                            &nbsp; </a>
                          <div
                            class="v-card__subtitle userPositionLabel text-truncate py-0">
                            &nbsp;</div>
                        </div>
                        <div class="v-card__actions peopleCardActions">
                          <div role="dialog" class="v-dialog__container">
                            <!---->
                          </div>
                          <button type="button" disabled="disabled"
                            class="btn mx-auto peopleRelationshipButton connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"><i
                              class="uiIconSocConnectUser peopleRelationshipIcon d-inline"></i>
                              <span
                              class="d-inline peopleRelationshipButtonText">
                                &nbsp; </span> <i aria-hidden="true"
                              class="v-icon notranslate d-none relationshipButtonMinus mdi mdi-plus theme--light"></i></span>
                          </button>
                        </div>
                      </div>
                    </div>
                    <!---->
                  </div>
                </div>
                <div class="pa-0 col-md-6 col-lg-4 col-xl-3 col-12">
                  <div class="flex peopleCardFlip">
                    <div class="peopleCardFront">
                      <div
                        class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                        id="userMenuParent" front="">
                        <div
                          class="v-responsive v-image white--text align-start d-block peopleBannerImg"
                          style="height: 80px;">
                          <div class="v-responsive__content"></div>
                        </div>
                        <div class="peopleToolbarIcons px-2">
                          <button type="button" disabled="disabled"
                            class="peopleInfoIcon d-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                          <!---->
                          <div class="spacer"></div>
                          <button type="button" disabled="disabled"
                            class="peopleMenuIcon d-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                        </div>
                        <div class="peopleAvatar">
                          <a href="#"><div
                              class="v-responsive v-image mx-auto skeleton-background"
                              style="height: 65px; max-height: 65px; max-width: 65px; width: 65px;">
                              <div class="v-responsive__content"></div>
                            </div></a>
                        </div>
                        <div
                          class="v-card__text peopleCardBody align-center pt-2 pb-1">
                          <a href="#"
                            class="userFullname text-truncate font-weight-bold d-block text-sub-title">
                            &nbsp; </a>
                          <div
                            class="v-card__subtitle userPositionLabel text-truncate py-0">
                            &nbsp;</div>
                        </div>
                        <div class="v-card__actions peopleCardActions">
                          <div role="dialog" class="v-dialog__container">
                            <!---->
                          </div>
                          <button type="button" disabled="disabled"
                            class="btn mx-auto peopleRelationshipButton connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"><i
                              class="uiIconSocConnectUser peopleRelationshipIcon d-inline"></i>
                              <span
                              class="d-inline peopleRelationshipButtonText">
                                &nbsp; </span> <i aria-hidden="true"
                              class="v-icon notranslate d-none relationshipButtonMinus mdi mdi-plus theme--light"></i></span>
                          </button>
                        </div>
                      </div>
                    </div>
                    <!---->
                  </div>
                </div>
                <div class="pa-0 col-md-6 col-lg-4 col-xl-3 col-12">
                  <div class="flex peopleCardFlip">
                    <div class="peopleCardFront">
                      <div
                        class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                        id="userMenuParent" front="">
                        <div
                          class="v-responsive v-image white--text align-start d-block peopleBannerImg"
                          style="height: 80px;">
                          <div class="v-responsive__content"></div>
                        </div>
                        <div class="peopleToolbarIcons px-2">
                          <button type="button" disabled="disabled"
                            class="peopleInfoIcon d-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                          <!---->
                          <div class="spacer"></div>
                          <button type="button" disabled="disabled"
                            class="peopleMenuIcon d-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                        </div>
                        <div class="peopleAvatar">
                          <a href="#"><div
                              class="v-responsive v-image mx-auto skeleton-background"
                              style="height: 65px; max-height: 65px; max-width: 65px; width: 65px;">
                              <div class="v-responsive__content"></div>
                            </div></a>
                        </div>
                        <div
                          class="v-card__text peopleCardBody align-center pt-2 pb-1">
                          <a href="#"
                            class="userFullname text-truncate font-weight-bold d-block text-sub-title">
                            &nbsp; </a>
                          <div
                            class="v-card__subtitle userPositionLabel text-truncate py-0">
                            &nbsp;</div>
                        </div>
                        <div class="v-card__actions peopleCardActions">
                          <div role="dialog" class="v-dialog__container">
                            <!---->
                          </div>
                          <button type="button" disabled="disabled"
                            class="btn mx-auto peopleRelationshipButton connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"><i
                              class="uiIconSocConnectUser peopleRelationshipIcon d-inline"></i>
                              <span
                              class="d-inline peopleRelationshipButtonText">
                                &nbsp; </span> <i aria-hidden="true"
                              class="v-icon notranslate d-none relationshipButtonMinus mdi mdi-plus theme--light"></i></span>
                          </button>
                        </div>
                      </div>
                    </div>
                    <!---->
                  </div>
                </div>
                <div class="pa-0 col-md-6 col-lg-4 col-xl-3 col-12">
                  <div class="flex peopleCardFlip">
                    <div class="peopleCardFront">
                      <div
                        class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                        id="userMenuParent" front="">
                        <div
                          class="v-responsive v-image white--text align-start d-block peopleBannerImg"
                          style="height: 80px;">
                          <div class="v-responsive__content"></div>
                        </div>
                        <div class="peopleToolbarIcons px-2">
                          <button type="button" disabled="disabled"
                            class="peopleInfoIcon d-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                          <!---->
                          <div class="spacer"></div>
                          <button type="button" disabled="disabled"
                            class="peopleMenuIcon d-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                        </div>
                        <div class="peopleAvatar">
                          <a href="#"><div
                              class="v-responsive v-image mx-auto skeleton-background"
                              style="height: 65px; max-height: 65px; max-width: 65px; width: 65px;">
                              <div class="v-responsive__content"></div>
                            </div></a>
                        </div>
                        <div
                          class="v-card__text peopleCardBody align-center pt-2 pb-1">
                          <a href="#"
                            class="userFullname text-truncate font-weight-bold d-block text-sub-title">
                            &nbsp; </a>
                          <div
                            class="v-card__subtitle userPositionLabel text-truncate py-0">
                            &nbsp;</div>
                        </div>
                        <div class="v-card__actions peopleCardActions">
                          <div role="dialog" class="v-dialog__container">
                            <!---->
                          </div>
                          <button type="button" disabled="disabled"
                            class="btn mx-auto peopleRelationshipButton connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"><i
                              class="uiIconSocConnectUser peopleRelationshipIcon d-inline"></i>
                              <span
                              class="d-inline peopleRelationshipButtonText">
                                &nbsp; </span> <i aria-hidden="true"
                              class="v-icon notranslate d-none relationshipButtonMinus mdi mdi-plus theme--light"></i></span>
                          </button>
                        </div>
                      </div>
                    </div>
                    <!---->
                  </div>
                </div>
                <div class="pa-0 col-md-6 col-lg-4 col-xl-3 col-12">
                  <div class="flex peopleCardFlip">
                    <div class="peopleCardFront">
                      <div
                        class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                        id="userMenuParent" front="">
                        <div
                          class="v-responsive v-image white--text align-start d-block peopleBannerImg"
                          style="height: 80px;">
                          <div class="v-responsive__content"></div>
                        </div>
                        <div class="peopleToolbarIcons px-2">
                          <button type="button" disabled="disabled"
                            class="peopleInfoIcon d-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                          <!---->
                          <div class="spacer"></div>
                          <button type="button" disabled="disabled"
                            class="peopleMenuIcon d-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"></span>
                          </button>
                          <!---->
                        </div>
                        <div class="peopleAvatar">
                          <a href="#"><div
                              class="v-responsive v-image mx-auto skeleton-background"
                              style="height: 65px; max-height: 65px; max-width: 65px; width: 65px;">
                              <div class="v-responsive__content"></div>
                            </div></a>
                        </div>
                        <div
                          class="v-card__text peopleCardBody align-center pt-2 pb-1">
                          <a href="#"
                            class="userFullname text-truncate font-weight-bold d-block text-sub-title">
                            &nbsp; </a>
                          <div
                            class="v-card__subtitle userPositionLabel text-truncate py-0">
                            &nbsp;</div>
                        </div>
                        <div class="v-card__actions peopleCardActions">
                          <div role="dialog" class="v-dialog__container">
                            <!---->
                          </div>
                          <button type="button" disabled="disabled"
                            class="btn mx-auto peopleRelationshipButton connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content"><i
                              class="uiIconSocConnectUser peopleRelationshipIcon d-inline"></i>
                              <span
                              class="d-inline peopleRelationshipButtonText">
                                &nbsp; </span> <i aria-hidden="true"
                              class="v-icon notranslate d-none relationshipButtonMinus mdi mdi-plus theme--light"></i></span>
                          </button>
                        </div>
                      </div>
                    </div>
                    <!---->
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div id="peopleListFooter"
          class="v-card__actions pt-0 px-5 border-box-sizing">
          <button type="button" disabled="disabled"
            class="loadMoreButton ma-auto btn v-btn v-btn--block v-btn--contained v-btn--disabled v-btn--loading theme--light v-size--default skeleton-background skeleton-text">
            <span class="v-btn__content"> Show more </span><span
              class="v-btn__loader"><div role="progressbar"
                aria-valuemin="0" aria-valuemax="100"
                class="v-progress-circular v-progress-circular--indeterminate"
                style="height: 23px; width: 23px;">
                <svg xmlns="http://www.w3.org/2000/svg"
                  viewBox="21.904761904761905 21.904761904761905 43.80952380952381 43.80952380952381"
                  style="transform: rotate(0deg);">
                  <circle fill="transparent" cx="43.80952380952381"
                    cy="43.80952380952381" r="20"
                    stroke-width="3.8095238095238093"
                    stroke-dasharray="125.664"
                    stroke-dashoffset="125.66370614359172px"
                    class="v-progress-circular__overlay"></circle></svg>
                <div class="v-progress-circular__info"></div>
              </div></span>
          </button>
        </div>
      </div>
      <aside
        class="drawerParent spaceInvitationDrawer v-navigation-drawer v-navigation-drawer--absolute v-navigation-drawer--close v-navigation-drawer--right v-navigation-drawer--temporary theme--light d-none d-sm-flex"
        max-height="100vh" max-width="100vw" data-booted="true"
        style="height: 100vh; top: 0px; transform: translateX(100%); width: 420px;">
        <div class="v-navigation-drawer__content">
          <div class="container pa-0 fill-height">
            <div class="layout column">
              <div class="flex mx-0 drawerHeader flex-grow-0">
                <div tabindex="-1" class="pr-0 v-list-item theme--light">
                  <div
                    class="v-list-item__content drawerTitle align-start text-header-title text-truncate">
                    Invite users</div>
                  <div
                    class="v-list-item__action drawerIcons align-end d-flex flex-row">
                    <button type="button"
                      class="v-icon notranslate my-auto v-icon--link mdi mdi-close theme--light"></button>
                  </div>
                </div>
              </div>
              <hr role="separator" aria-orientation="horizontal"
                class="my-0 v-divider theme--light">
              <div
                class="flex drawerContent flex-grow-1 overflow-auto border-box-sizing">
                <form>
                  <div class="flex ma-4" id="AutoComplete1705"
                    name="inviteMembers">
                    <div
                      class="v-input identitySuggester v-input--dense theme--light v-text-field v-text-field--solo-flat v-text-field--is-booted v-text-field--placeholder v-select v-select--chips v-select--is-multi v-autocomplete">
                      <div class="v-input__control">
                        <div role="combobox" aria-haspopup="listbox"
                          aria-expanded="false" aria-owns="list-364"
                          class="v-input__slot">
                          <div class="v-select__slot">
                            <div class="v-select__selections">
                              <input
                                content-class="identitySuggesterContent"
                                width="100%" max-width="100%"
                                id="input-364"
                                placeholder="Invite members" type="text"
                                autocomplete="off">
                            </div>
                            <input type="hidden">
                          </div>
                          <div class="v-menu">
                            <div
                              class="v-menu__content theme--light v-autocomplete__content"
                              style="max-height: 304px; min-width: 0px; top: 12px; left: 0px; transform-origin: left top; z-index: 0; display: none;">
                              <div role="listbox" tabindex="-1"
                                class="v-list v-select-list v-sheet v-sheet--tile theme--light v-list--dense theme--light"
                                id="list-364">
                                <div tabindex="-1" id="list-item-369"
                                  class="pa-0 v-list-item theme--light">
                                  <div class="v-list-item__title px-2">
                                    No data</div>
                                </div>
                              </div>
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
                  </div>
                  <!---->
                </form>
              </div>
              <hr role="separator" aria-orientation="horizontal"
                class="my-0 v-divider theme--light">
              <div
                class="flex drawerFooter border-box-sizing flex-grow-0 px-4 py-3">
                <div class="d-flex">
                  <div class="spacer"></div>
                  <button type="button"
                    class="btn mr-2 v-btn v-btn--contained theme--light v-size--default">
                    <span class="v-btn__content"> Cancel </span>
                  </button>
                  <button type="button" disabled="disabled"
                    class="btn btn-primary v-btn v-btn--contained v-btn--disabled theme--light v-size--default">
                    <span class="v-btn__content"> Invite </span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="v-navigation-drawer__border"></div>
      </aside>
    </div>
  </div>
</div>