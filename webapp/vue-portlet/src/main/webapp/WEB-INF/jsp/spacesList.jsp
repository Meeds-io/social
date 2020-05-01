<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="org.exoplatform.social.core.space.SpacesAdministrationService"%>
<%
  Object filter = request.getAttribute("filter");
  if (filter == null) {
    filter = "";
  } else {
    filter = ((String[]) filter)[0];
  }
  SpacesAdministrationService spacesAdministrationService = ExoContainerContext.getService(SpacesAdministrationService.class);
  boolean canCreateSpace = spacesAdministrationService.canCreateSpace(request.getRemoteUser());
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application transparent v-application--is-ltr theme--light"
    id="spacesListApplication" flat="">
    <script type="text/javascript">
      require(['PORTLET/social-vue-portlet/SpacesList'],
          app => app.init('<%=filter%>', <%=canCreateSpace%>)
      );
    </script>
    <div class="v-application--wrap">
      <header
        class="v-sheet v-sheet--tile theme--light v-toolbar v-toolbar--flat"
        id="spacesListToolbar" style="height: 64px;">
        <div class="v-toolbar__content" style="height: 64px;">
          <div class="v-toolbar__title">
            <button type="button" disabled="disabled"
              class="btn pr-2 pl-0 addNewSpaceButton v-btn v-btn--contained v-btn--disabled theme--light v-size--default skeleton-text skeleton-background">
              <span class="v-btn__content"><span class="mx-2"></span>
                <span class="d-none d-sm-inline"> Add space </span></span>
            </button>
          </div>
          <div
            class="text-sub-title ml-3 d-none d-sm-flex skeleton-text skeleton-background skeleton-border-radius">
            Showing X Spaces</div>
          <div class="spacer"></div>
          <div
            class="v-input inputSpacesFilter pa-0 mr-3 my-auto v-input--is-disabled theme--light v-text-field v-text-field--is-booted v-text-field--placeholder skeleton-text">
            <div class="v-input__control">
              <div class="v-input__slot">
                <div class="v-input__prepend-inner">
                  <div
                    class="v-input__icon v-input__icon--prepend-inner">
                    <i aria-hidden="true"
                      class="v-icon notranslate fa fa-filter theme--light"></i>
                  </div>
                </div>
                <div class="v-text-field__slot">
                  <input disabled="disabled" id="input-117"
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
            class="selectSpacesFilter my-auto mr-2 subtitle-1 ignore-vuetify-classes d-none d-sm-inline skeleton-background skeleton-text"><option
              value="all">All spaces</option>
            <option value="member">My spaces</option></select>
          <button type="button"
            class="v-icon notranslate d-sm-none v-icon--link fa fa-filter skeleton-text theme--light"></button>
          <div role="dialog" class="v-dialog__container pa-0">
            <!---->
          </div>
        </div>
      </header>
      <div class="v-card v-card--flat v-sheet theme--light">
        <div id="spacesListBody" class="v-card__text pb-0">
          <div class="v-item-group theme--light">
            <div class="container pa-0">
              <div class="row ma-0 border-box-sizing">
                <div class="col-md-3 col-12 spaceCardFlip">
                  <div class="spaceCardFront">
                    <div
                      class="spaceCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="spaceMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block spaceBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="spaceToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="spaceInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <!---->
                        <button type="button" disabled="disabled"
                          class="spaceMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
                        </button>
                        <!---->
                      </div>
                      <div class="spaceAvatar">
                        <a href="#"><div
                            class="v-responsive v-image mx-auto skeleton-background"
                            style="height: 75px; max-height: 75px; max-width: 75px; width: 75px;">
                            <div class="v-responsive__content"></div>
                          </div></a>
                      </div>
                      <div
                        class="v-card__text spaceCardBody align-center pt-2 pb-1">
                        <a href="#"
                          class="spaceDisplayName text-truncate d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle spaceMembersLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions spaceCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <div title="This space is closed"
                          class="joinSpaceDisabledLabel">
                          <button type="button" disabled="disabled"
                            class="btn mx-auto joinSpaceButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content">
                              <!----> <span class="d-none d-sm-inline">
                                &nbsp; </span>
                            </span>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 spaceCardFlip">
                  <div class="spaceCardFront">
                    <div
                      class="spaceCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="spaceMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block spaceBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="spaceToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="spaceInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <!---->
                        <button type="button" disabled="disabled"
                          class="spaceMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
                        </button>
                        <!---->
                      </div>
                      <div class="spaceAvatar">
                        <a href="#"><div
                            class="v-responsive v-image mx-auto skeleton-background"
                            style="height: 75px; max-height: 75px; max-width: 75px; width: 75px;">
                            <div class="v-responsive__content"></div>
                          </div></a>
                      </div>
                      <div
                        class="v-card__text spaceCardBody align-center pt-2 pb-1">
                        <a href="#"
                          class="spaceDisplayName text-truncate d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle spaceMembersLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions spaceCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <div title="This space is closed"
                          class="joinSpaceDisabledLabel">
                          <button type="button" disabled="disabled"
                            class="btn mx-auto joinSpaceButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content">
                              <!----> <span class="d-none d-sm-inline">
                                &nbsp; </span>
                            </span>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 spaceCardFlip">
                  <div class="spaceCardFront">
                    <div
                      class="spaceCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="spaceMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block spaceBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="spaceToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="spaceInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <!---->
                        <button type="button" disabled="disabled"
                          class="spaceMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
                        </button>
                        <!---->
                      </div>
                      <div class="spaceAvatar">
                        <a href="#"><div
                            class="v-responsive v-image mx-auto skeleton-background"
                            style="height: 75px; max-height: 75px; max-width: 75px; width: 75px;">
                            <div class="v-responsive__content"></div>
                          </div></a>
                      </div>
                      <div
                        class="v-card__text spaceCardBody align-center pt-2 pb-1">
                        <a href="#"
                          class="spaceDisplayName text-truncate d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle spaceMembersLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions spaceCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <div title="This space is closed"
                          class="joinSpaceDisabledLabel">
                          <button type="button" disabled="disabled"
                            class="btn mx-auto joinSpaceButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content">
                              <!----> <span class="d-none d-sm-inline">
                                &nbsp; </span>
                            </span>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 spaceCardFlip">
                  <div class="spaceCardFront">
                    <div
                      class="spaceCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="spaceMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block spaceBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="spaceToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="spaceInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <!---->
                        <button type="button" disabled="disabled"
                          class="spaceMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
                        </button>
                        <!---->
                      </div>
                      <div class="spaceAvatar">
                        <a href="#"><div
                            class="v-responsive v-image mx-auto skeleton-background"
                            style="height: 75px; max-height: 75px; max-width: 75px; width: 75px;">
                            <div class="v-responsive__content"></div>
                          </div></a>
                      </div>
                      <div
                        class="v-card__text spaceCardBody align-center pt-2 pb-1">
                        <a href="#"
                          class="spaceDisplayName text-truncate d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle spaceMembersLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions spaceCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <div title="This space is closed"
                          class="joinSpaceDisabledLabel">
                          <button type="button" disabled="disabled"
                            class="btn mx-auto joinSpaceButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content">
                              <!----> <span class="d-none d-sm-inline">
                                &nbsp; </span>
                            </span>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 spaceCardFlip">
                  <div class="spaceCardFront">
                    <div
                      class="spaceCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="spaceMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block spaceBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="spaceToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="spaceInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <!---->
                        <button type="button" disabled="disabled"
                          class="spaceMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
                        </button>
                        <!---->
                      </div>
                      <div class="spaceAvatar">
                        <a href="#"><div
                            class="v-responsive v-image mx-auto skeleton-background"
                            style="height: 75px; max-height: 75px; max-width: 75px; width: 75px;">
                            <div class="v-responsive__content"></div>
                          </div></a>
                      </div>
                      <div
                        class="v-card__text spaceCardBody align-center pt-2 pb-1">
                        <a href="#"
                          class="spaceDisplayName text-truncate d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle spaceMembersLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions spaceCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <div title="This space is closed"
                          class="joinSpaceDisabledLabel">
                          <button type="button" disabled="disabled"
                            class="btn mx-auto joinSpaceButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content">
                              <!----> <span class="d-none d-sm-inline">
                                &nbsp; </span>
                            </span>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 spaceCardFlip">
                  <div class="spaceCardFront">
                    <div
                      class="spaceCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="spaceMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block spaceBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="spaceToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="spaceInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <!---->
                        <button type="button" disabled="disabled"
                          class="spaceMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
                        </button>
                        <!---->
                      </div>
                      <div class="spaceAvatar">
                        <a href="#"><div
                            class="v-responsive v-image mx-auto skeleton-background"
                            style="height: 75px; max-height: 75px; max-width: 75px; width: 75px;">
                            <div class="v-responsive__content"></div>
                          </div></a>
                      </div>
                      <div
                        class="v-card__text spaceCardBody align-center pt-2 pb-1">
                        <a href="#"
                          class="spaceDisplayName text-truncate d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle spaceMembersLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions spaceCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <div title="This space is closed"
                          class="joinSpaceDisabledLabel">
                          <button type="button" disabled="disabled"
                            class="btn mx-auto joinSpaceButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content">
                              <!----> <span class="d-none d-sm-inline">
                                &nbsp; </span>
                            </span>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 spaceCardFlip">
                  <div class="spaceCardFront">
                    <div
                      class="spaceCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="spaceMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block spaceBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="spaceToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="spaceInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <!---->
                        <button type="button" disabled="disabled"
                          class="spaceMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
                        </button>
                        <!---->
                      </div>
                      <div class="spaceAvatar">
                        <a href="#"><div
                            class="v-responsive v-image mx-auto skeleton-background"
                            style="height: 75px; max-height: 75px; max-width: 75px; width: 75px;">
                            <div class="v-responsive__content"></div>
                          </div></a>
                      </div>
                      <div
                        class="v-card__text spaceCardBody align-center pt-2 pb-1">
                        <a href="#"
                          class="spaceDisplayName text-truncate d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle spaceMembersLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions spaceCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <div title="This space is closed"
                          class="joinSpaceDisabledLabel">
                          <button type="button" disabled="disabled"
                            class="btn mx-auto joinSpaceButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content">
                              <!----> <span class="d-none d-sm-inline">
                                &nbsp; </span>
                            </span>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 spaceCardFlip">
                  <div class="spaceCardFront">
                    <div
                      class="spaceCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="spaceMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block spaceBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="spaceToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="spaceInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <!---->
                        <button type="button" disabled="disabled"
                          class="spaceMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
                        </button>
                        <!---->
                      </div>
                      <div class="spaceAvatar">
                        <a href="#"><div
                            class="v-responsive v-image mx-auto skeleton-background"
                            style="height: 75px; max-height: 75px; max-width: 75px; width: 75px;">
                            <div class="v-responsive__content"></div>
                          </div></a>
                      </div>
                      <div
                        class="v-card__text spaceCardBody align-center pt-2 pb-1">
                        <a href="#"
                          class="spaceDisplayName text-truncate d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle spaceMembersLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions spaceCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <div title="This space is closed"
                          class="joinSpaceDisabledLabel">
                          <button type="button" disabled="disabled"
                            class="btn mx-auto joinSpaceButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content">
                              <!----> <span class="d-none d-sm-inline">
                                &nbsp; </span>
                            </span>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 spaceCardFlip">
                  <div class="spaceCardFront">
                    <div
                      class="spaceCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="spaceMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block spaceBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="spaceToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="spaceInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <!---->
                        <button type="button" disabled="disabled"
                          class="spaceMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
                        </button>
                        <!---->
                      </div>
                      <div class="spaceAvatar">
                        <a href="#"><div
                            class="v-responsive v-image mx-auto skeleton-background"
                            style="height: 75px; max-height: 75px; max-width: 75px; width: 75px;">
                            <div class="v-responsive__content"></div>
                          </div></a>
                      </div>
                      <div
                        class="v-card__text spaceCardBody align-center pt-2 pb-1">
                        <a href="#"
                          class="spaceDisplayName text-truncate d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle spaceMembersLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions spaceCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <div title="This space is closed"
                          class="joinSpaceDisabledLabel">
                          <button type="button" disabled="disabled"
                            class="btn mx-auto joinSpaceButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content">
                              <!----> <span class="d-none d-sm-inline">
                                &nbsp; </span>
                            </span>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 spaceCardFlip">
                  <div class="spaceCardFront">
                    <div
                      class="spaceCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="spaceMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block spaceBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="spaceToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="spaceInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <!---->
                        <button type="button" disabled="disabled"
                          class="spaceMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
                        </button>
                        <!---->
                      </div>
                      <div class="spaceAvatar">
                        <a href="#"><div
                            class="v-responsive v-image mx-auto skeleton-background"
                            style="height: 75px; max-height: 75px; max-width: 75px; width: 75px;">
                            <div class="v-responsive__content"></div>
                          </div></a>
                      </div>
                      <div
                        class="v-card__text spaceCardBody align-center pt-2 pb-1">
                        <a href="#"
                          class="spaceDisplayName text-truncate d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle spaceMembersLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions spaceCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <div title="This space is closed"
                          class="joinSpaceDisabledLabel">
                          <button type="button" disabled="disabled"
                            class="btn mx-auto joinSpaceButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content">
                              <!----> <span class="d-none d-sm-inline">
                                &nbsp; </span>
                            </span>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 spaceCardFlip">
                  <div class="spaceCardFront">
                    <div
                      class="spaceCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="spaceMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block spaceBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="spaceToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="spaceInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <!---->
                        <button type="button" disabled="disabled"
                          class="spaceMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
                        </button>
                        <!---->
                      </div>
                      <div class="spaceAvatar">
                        <a href="#"><div
                            class="v-responsive v-image mx-auto skeleton-background"
                            style="height: 75px; max-height: 75px; max-width: 75px; width: 75px;">
                            <div class="v-responsive__content"></div>
                          </div></a>
                      </div>
                      <div
                        class="v-card__text spaceCardBody align-center pt-2 pb-1">
                        <a href="#"
                          class="spaceDisplayName text-truncate d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle spaceMembersLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions spaceCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <div title="This space is closed"
                          class="joinSpaceDisabledLabel">
                          <button type="button" disabled="disabled"
                            class="btn mx-auto joinSpaceButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content">
                              <!----> <span class="d-none d-sm-inline">
                                &nbsp; </span>
                            </span>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 spaceCardFlip">
                  <div class="spaceCardFront">
                    <div
                      class="spaceCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="spaceMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block spaceBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="spaceToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="spaceInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <!---->
                        <button type="button" disabled="disabled"
                          class="spaceMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
                        </button>
                        <!---->
                      </div>
                      <div class="spaceAvatar">
                        <a href="#"><div
                            class="v-responsive v-image mx-auto skeleton-background"
                            style="height: 75px; max-height: 75px; max-width: 75px; width: 75px;">
                            <div class="v-responsive__content"></div>
                          </div></a>
                      </div>
                      <div
                        class="v-card__text spaceCardBody align-center pt-2 pb-1">
                        <a href="#"
                          class="spaceDisplayName text-truncate d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle spaceMembersLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions spaceCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <div title="This space is closed"
                          class="joinSpaceDisabledLabel">
                          <button type="button" disabled="disabled"
                            class="btn mx-auto joinSpaceButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content">
                              <!----> <span class="d-none d-sm-inline">
                                &nbsp; </span>
                            </span>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 spaceCardFlip">
                  <div class="spaceCardFront">
                    <div
                      class="spaceCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="spaceMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block spaceBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="spaceToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="spaceInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <!---->
                        <button type="button" disabled="disabled"
                          class="spaceMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
                        </button>
                        <!---->
                      </div>
                      <div class="spaceAvatar">
                        <a href="#"><div
                            class="v-responsive v-image mx-auto skeleton-background"
                            style="height: 75px; max-height: 75px; max-width: 75px; width: 75px;">
                            <div class="v-responsive__content"></div>
                          </div></a>
                      </div>
                      <div
                        class="v-card__text spaceCardBody align-center pt-2 pb-1">
                        <a href="#"
                          class="spaceDisplayName text-truncate d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle spaceMembersLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions spaceCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <div title="This space is closed"
                          class="joinSpaceDisabledLabel">
                          <button type="button" disabled="disabled"
                            class="btn mx-auto joinSpaceButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content">
                              <!----> <span class="d-none d-sm-inline">
                                &nbsp; </span>
                            </span>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 spaceCardFlip">
                  <div class="spaceCardFront">
                    <div
                      class="spaceCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="spaceMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block spaceBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="spaceToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="spaceInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <!---->
                        <button type="button" disabled="disabled"
                          class="spaceMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
                        </button>
                        <!---->
                      </div>
                      <div class="spaceAvatar">
                        <a href="#"><div
                            class="v-responsive v-image mx-auto skeleton-background"
                            style="height: 75px; max-height: 75px; max-width: 75px; width: 75px;">
                            <div class="v-responsive__content"></div>
                          </div></a>
                      </div>
                      <div
                        class="v-card__text spaceCardBody align-center pt-2 pb-1">
                        <a href="#"
                          class="spaceDisplayName text-truncate d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle spaceMembersLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions spaceCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <div title="This space is closed"
                          class="joinSpaceDisabledLabel">
                          <button type="button" disabled="disabled"
                            class="btn mx-auto joinSpaceButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content">
                              <!----> <span class="d-none d-sm-inline">
                                &nbsp; </span>
                            </span>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 spaceCardFlip">
                  <div class="spaceCardFront">
                    <div
                      class="spaceCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="spaceMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block spaceBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="spaceToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="spaceInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <!---->
                        <button type="button" disabled="disabled"
                          class="spaceMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
                        </button>
                        <!---->
                      </div>
                      <div class="spaceAvatar">
                        <a href="#"><div
                            class="v-responsive v-image mx-auto skeleton-background"
                            style="height: 75px; max-height: 75px; max-width: 75px; width: 75px;">
                            <div class="v-responsive__content"></div>
                          </div></a>
                      </div>
                      <div
                        class="v-card__text spaceCardBody align-center pt-2 pb-1">
                        <a href="#"
                          class="spaceDisplayName text-truncate d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle spaceMembersLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions spaceCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <div title="This space is closed"
                          class="joinSpaceDisabledLabel">
                          <button type="button" disabled="disabled"
                            class="btn mx-auto joinSpaceButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content">
                              <!----> <span class="d-none d-sm-inline">
                                &nbsp; </span>
                            </span>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 spaceCardFlip">
                  <div class="spaceCardFront">
                    <div
                      class="spaceCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="spaceMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block spaceBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="spaceToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="spaceInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <!---->
                        <button type="button" disabled="disabled"
                          class="spaceMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
                        </button>
                        <!---->
                      </div>
                      <div class="spaceAvatar">
                        <a href="#"><div
                            class="v-responsive v-image mx-auto skeleton-background"
                            style="height: 75px; max-height: 75px; max-width: 75px; width: 75px;">
                            <div class="v-responsive__content"></div>
                          </div></a>
                      </div>
                      <div
                        class="v-card__text spaceCardBody align-center pt-2 pb-1">
                        <a href="#"
                          class="spaceDisplayName text-truncate d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle spaceMembersLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions spaceCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <div title="This space is closed"
                          class="joinSpaceDisabledLabel">
                          <button type="button" disabled="disabled"
                            class="btn mx-auto joinSpaceButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content">
                              <!----> <span class="d-none d-sm-inline">
                                &nbsp; </span>
                            </span>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 spaceCardFlip">
                  <div class="spaceCardFront">
                    <div
                      class="spaceCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="spaceMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block spaceBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="spaceToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="spaceInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <!---->
                        <button type="button" disabled="disabled"
                          class="spaceMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
                        </button>
                        <!---->
                      </div>
                      <div class="spaceAvatar">
                        <a href="#"><div
                            class="v-responsive v-image mx-auto skeleton-background"
                            style="height: 75px; max-height: 75px; max-width: 75px; width: 75px;">
                            <div class="v-responsive__content"></div>
                          </div></a>
                      </div>
                      <div
                        class="v-card__text spaceCardBody align-center pt-2 pb-1">
                        <a href="#"
                          class="spaceDisplayName text-truncate d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle spaceMembersLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions spaceCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <div title="This space is closed"
                          class="joinSpaceDisabledLabel">
                          <button type="button" disabled="disabled"
                            class="btn mx-auto joinSpaceButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content">
                              <!----> <span class="d-none d-sm-inline">
                                &nbsp; </span>
                            </span>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 spaceCardFlip">
                  <div class="spaceCardFront">
                    <div
                      class="spaceCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="spaceMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block spaceBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="spaceToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="spaceInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <!---->
                        <button type="button" disabled="disabled"
                          class="spaceMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
                        </button>
                        <!---->
                      </div>
                      <div class="spaceAvatar">
                        <a href="#"><div
                            class="v-responsive v-image mx-auto skeleton-background"
                            style="height: 75px; max-height: 75px; max-width: 75px; width: 75px;">
                            <div class="v-responsive__content"></div>
                          </div></a>
                      </div>
                      <div
                        class="v-card__text spaceCardBody align-center pt-2 pb-1">
                        <a href="#"
                          class="spaceDisplayName text-truncate d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle spaceMembersLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions spaceCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <div title="This space is closed"
                          class="joinSpaceDisabledLabel">
                          <button type="button" disabled="disabled"
                            class="btn mx-auto joinSpaceButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content">
                              <!----> <span class="d-none d-sm-inline">
                                &nbsp; </span>
                            </span>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 spaceCardFlip">
                  <div class="spaceCardFront">
                    <div
                      class="spaceCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="spaceMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block spaceBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="spaceToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="spaceInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <!---->
                        <button type="button" disabled="disabled"
                          class="spaceMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
                        </button>
                        <!---->
                      </div>
                      <div class="spaceAvatar">
                        <a href="#"><div
                            class="v-responsive v-image mx-auto skeleton-background"
                            style="height: 75px; max-height: 75px; max-width: 75px; width: 75px;">
                            <div class="v-responsive__content"></div>
                          </div></a>
                      </div>
                      <div
                        class="v-card__text spaceCardBody align-center pt-2 pb-1">
                        <a href="#"
                          class="spaceDisplayName text-truncate d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle spaceMembersLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions spaceCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <div title="This space is closed"
                          class="joinSpaceDisabledLabel">
                          <button type="button" disabled="disabled"
                            class="btn mx-auto joinSpaceButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content">
                              <!----> <span class="d-none d-sm-inline">
                                &nbsp; </span>
                            </span>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 spaceCardFlip">
                  <div class="spaceCardFront">
                    <div
                      class="spaceCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="spaceMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block spaceBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="spaceToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="spaceInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <!---->
                        <button type="button" disabled="disabled"
                          class="spaceMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
                        </button>
                        <!---->
                      </div>
                      <div class="spaceAvatar">
                        <a href="#"><div
                            class="v-responsive v-image mx-auto skeleton-background"
                            style="height: 75px; max-height: 75px; max-width: 75px; width: 75px;">
                            <div class="v-responsive__content"></div>
                          </div></a>
                      </div>
                      <div
                        class="v-card__text spaceCardBody align-center pt-2 pb-1">
                        <a href="#"
                          class="spaceDisplayName text-truncate d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle spaceMembersLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions spaceCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <div title="This space is closed"
                          class="joinSpaceDisabledLabel">
                          <button type="button" disabled="disabled"
                            class="btn mx-auto joinSpaceButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                            <span class="v-btn__content">
                              <!----> <span class="d-none d-sm-inline">
                                &nbsp; </span>
                            </span>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
              </div>
            </div>
          </div>
        </div>
        <div id="spacesListFooter"
          class="v-card__actions pt-0 px-5 border-box-sizing">
          <button type="button" disabled="disabled"
            class="loadMoreButton ma-auto btn v-btn v-btn--block v-btn--contained v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
            <span class="v-btn__content"> Show more </span>
          </button>
        </div>
      </div>
      <aside
        class="drawerParent v-navigation-drawer v-navigation-drawer--absolute v-navigation-drawer--close v-navigation-drawer--right v-navigation-drawer--temporary theme--light d-none d-sm-flex"
        max-height="100vh" max-width="100vw"
        style="height: 100vh; top: 0px; transform: translateX(100%); width: 420px;"
        data-booted="true">
        <div class="v-navigation-drawer__content">
          <div class="container pa-0 fill-height">
            <div class="layout column">
              <div class="flex mx-0 drawerHeader flex-grow-0">
                <div tabindex="-1" class="pr-0 v-list-item theme--light">
                  <div
                    class="v-list-item__content drawerTitle align-start text-header-title text-truncate">
                    Managers</div>
                  <div class="v-list-item__action drawerIcons align-end">
                    <button type="button"
                      class="v-icon notranslate v-icon--link mdi mdi-close theme--light"></button>
                  </div>
                </div>
              </div>
              <hr role="separator" aria-orientation="horizontal"
                class="my-0 v-divider theme--light">
              <div
                class="flex drawerContent flex-grow-1 overflow-auto border-box-sizing"></div>
              <!---->
            </div>
          </div>
        </div>
        <div class="v-navigation-drawer__border"></div>
      </aside>
      <aside
        class="drawerParent spaceFormDrawer v-navigation-drawer v-navigation-drawer--absolute v-navigation-drawer--close v-navigation-drawer--right v-navigation-drawer--temporary theme--light d-none d-sm-flex"
        max-height="100vh" max-width="100vw"
        style="height: 100vh; top: 0px; transform: translateX(100%); width: 420px;"
        data-booted="true">
        <div class="v-navigation-drawer__content">
          <div class="container pa-0 fill-height">
            <div class="layout column">
              <div class="flex mx-0 drawerHeader flex-grow-0">
                <div tabindex="-1" class="pr-0 v-list-item theme--light">
                  <div
                    class="v-list-item__content drawerTitle align-start text-header-title text-truncate">
                  </div>
                  <div class="v-list-item__action drawerIcons align-end">
                    <button type="button"
                      class="v-icon notranslate v-icon--link mdi mdi-close theme--light"></button>
                  </div>
                </div>
              </div>
              <hr role="separator" aria-orientation="horizontal"
                class="my-0 v-divider theme--light">
              <div
                class="flex drawerContent flex-grow-1 overflow-auto border-box-sizing">
                <div></div>
                <div>
                  <div
                    class="v-stepper ma-0 pb-0 v-stepper--vertical theme--light"
                    flat="">
                    <div
                      class="v-stepper__step ma-0 v-stepper__step--inactive">
                      <span class="v-stepper__step__step">1</span>
                      <div class="v-stepper__label">Space details
                      </div>
                    </div>
                    <div class="v-stepper__content">
                      <div class="v-stepper__wrapper"
                        style="height: 0px;">
                        <form>
                          <label for="name" class="v-label theme--light"
                            style="left: 0px; right: auto; position: relative;">
                            Name </label> <input placeholder="Display name"
                            type="text" name="name" maxlength="200"
                            required="required"
                            class="input-block-level ignore-vuetify-classes my-3">
                          <label for="description"
                            class="v-label theme--light"
                            style="left: 0px; right: auto; position: relative;">
                            Description </label>
                          <textarea placeholder="Description"
                            name="description" rows="20"
                            maxlength="2000" noresize="noresize"
                            class="input-block-level ignore-vuetify-classes my-3"></textarea>
                          <label for="spaceTemplate"
                            class="v-label theme--light"
                            style="left: 0px; right: auto; position: relative;">
                            Template </label> <select name="spaceTemplate"
                            required="required"
                            class="input-block-level ignore-vuetify-classes my-3"><option
                              value="crowdFunding">
                              Crowdfunding</option></select>
                          <div
                            class="caption font-italic font-weight-light pl-1 muted">Space
                            that contains a wallet to encourage
                            crowdfunding initiatives.</div>
                          <div class="v-card__actions px-0">
                            <div class="spacer"></div>
                            <button type="button"
                              class="btn btn-primary v-btn v-btn--depressed v-btn--flat v-btn--outlined theme--light v-size--default">
                              <span class="v-btn__content">
                                Continue <i aria-hidden="true"
                                class="v-icon notranslate ml-2 fa fa-caret-right theme--light"
                                style="font-size: 18px;"></i>
                              </span>
                            </button>
                          </div>
                        </form>
                      </div>
                    </div>
                    <div
                      class="v-stepper__step ma-0 v-stepper__step--inactive">
                      <span class="v-stepper__step__step">2</span>
                      <div class="v-stepper__label">Space access</div>
                    </div>
                    <div class="v-stepper__content">
                      <div class="v-stepper__wrapper"
                        style="height: 0px;">
                        <form>
                          <div class="d-flex flex-wrap pt-2">
                            <label for="hidden"
                              class="v-label theme--light my-auto float-left">
                              Hidden </label>
                            <div
                              class="v-input float-left my-0 ml-4 theme--light v-input--selection-controls v-input--switch">
                              <div class="v-input__control">
                                <div class="v-input__slot">
                                  <div
                                    class="v-input--selection-controls__input">
                                    <input aria-checked="false"
                                      id="input-389" role="switch"
                                      type="checkbox"
                                      aria-disabled="false" value="">
                                    <div
                                      class="v-input--selection-controls__ripple"></div>
                                    <div
                                      class="v-input--switch__track theme--light"></div>
                                    <div
                                      class="v-input--switch__thumb theme--light">
                                      <!---->
                                    </div>
                                  </div>
                                </div>
                                <div class="v-messages theme--light">
                                  <div class="v-messages__wrapper"></div>
                                </div>
                              </div>
                            </div>
                          </div>
                          <div
                            class="caption font-italic font-weight-light pl-1 muted mb-2 mt-1">
                            The space will be listed in the space
                            directory.</div>
                          <div class="d-flex flex-wrap pt-2">
                            <label for="hidden"
                              class="v-label theme--light">
                              Registration </label>
                            <div
                              class="v-input mt-2 ml-2 v-input--is-label-active v-input--is-dirty theme--light v-input--selection-controls v-input--radio-group v-input--radio-group--row">
                              <div class="v-input__control">
                                <div class="v-input__slot"
                                  style="height: auto;">
                                  <div role="radiogroup"
                                    aria-labelledby="input-393"
                                    class="v-input--radio-group__input">
                                    <div
                                      class="v-radio my-0 theme--light v-item--active">
                                      <div
                                        class="v-input--selection-controls__input">
                                        <i aria-hidden="true"
                                          class="v-icon notranslate mdi mdi-radiobox-marked theme--light primary--text"></i><input
                                          aria-checked="true"
                                          id="input-394" role="radio"
                                          type="radio" name="radio-393"
                                          value="open">
                                        <div
                                          class="v-input--selection-controls__ripple primary--text"></div>
                                      </div>
                                      <label for="input-394"
                                        class="v-label theme--light"
                                        style="left: 0px; right: auto; position: relative;">Open</label>
                                    </div>
                                    <div
                                      class="v-radio my-0 theme--light">
                                      <div
                                        class="v-input--selection-controls__input">
                                        <i aria-hidden="true"
                                          class="v-icon notranslate mdi mdi-radiobox-blank theme--light"></i><input
                                          aria-checked="false"
                                          id="input-396" role="radio"
                                          type="radio" name="radio-393"
                                          value="validation">
                                        <div
                                          class="v-input--selection-controls__ripple primary--text"></div>
                                      </div>
                                      <label for="input-396"
                                        class="v-label theme--light"
                                        style="left: 0px; right: auto; position: relative;">Validation</label>
                                    </div>
                                    <div
                                      class="v-radio my-0 theme--light">
                                      <div
                                        class="v-input--selection-controls__input">
                                        <i aria-hidden="true"
                                          class="v-icon notranslate mdi mdi-radiobox-blank theme--light"></i><input
                                          aria-checked="false"
                                          id="input-398" role="radio"
                                          type="radio" name="radio-393"
                                          value="closed">
                                        <div
                                          class="v-input--selection-controls__ripple primary--text"></div>
                                      </div>
                                      <label for="input-398"
                                        class="v-label theme--light"
                                        style="left: 0px; right: auto; position: relative;">Closed</label>
                                    </div>
                                  </div>
                                </div>
                                <div class="v-messages theme--light">
                                  <div class="v-messages__wrapper"></div>
                                </div>
                              </div>
                            </div>
                          </div>
                          <div
                            class="caption font-italic font-weight-light pl-1 muted">Any
                            user can join the space. No validation is
                            required.</div>
                          <div class="v-card__actions mt-4 px-0">
                            <button type="button"
                              class="btn v-btn v-btn--contained theme--light v-size--default">
                              <span class="v-btn__content"><i
                                aria-hidden="true"
                                class="v-icon notranslate mr-2 fa fa-caret-left theme--light"
                                style="font-size: 18px;"></i> Back </span>
                            </button>
                            <div class="spacer"></div>
                            <button type="button"
                              class="btn btn-primary v-btn v-btn--depressed v-btn--flat v-btn--outlined theme--light v-size--default">
                              <span class="v-btn__content">
                                Continue <i aria-hidden="true"
                                class="v-icon notranslate ml-2 fa fa-caret-right theme--light"
                                style="font-size: 18px;"></i>
                              </span>
                            </button>
                          </div>
                        </form>
                      </div>
                    </div>
                    <div
                      class="v-stepper__step ma-0 v-stepper__step--inactive">
                      <span class="v-stepper__step__step">3</span>
                      <div class="v-stepper__label">Invite users</div>
                    </div>
                    <div class="v-stepper__content">
                      <div class="v-stepper__wrapper"
                        style="height: 0px;">
                        <form>
                          <label for="inviteMembers"
                            class="v-label theme--light"
                            style="left: 0px; right: auto; position: relative;">
                            Users </label>
                          <div class="flex" id="AutoComplete8295"
                            name="inviteMembers">
                            <div
                              class="v-input identitySuggester v-input--dense theme--light v-text-field v-text-field--solo-flat v-text-field--is-booted v-text-field--placeholder v-select v-select--chips v-select--is-multi v-autocomplete">
                              <div class="v-input__control">
                                <div role="combobox"
                                  aria-haspopup="listbox"
                                  aria-expanded="false"
                                  aria-owns="list-410"
                                  class="v-input__slot"
                                  style="height: 100px;">
                                  <div class="v-select__slot">
                                    <div class="v-select__selections">
                                      <input
                                        content-class="identitySuggesterContent"
                                        width="100%" max-width="100%"
                                        id="input-410"
                                        placeholder="Invite individual users or space members"
                                        type="text" autocomplete="off">
                                    </div>
                                    <input type="hidden" value="">
                                  </div>
                                  <div class="v-menu">
                                    <div
                                      class="v-menu__content theme--light v-autocomplete__content"
                                      style="max-height: 304px; min-width: 0px; top: 12px; left: 0px; transform-origin: left top; z-index: 0; display: none;">
                                      <div role="listbox" tabindex="-1"
                                        class="v-list v-select-list v-sheet v-sheet--tile theme--light v-list--dense theme--light"
                                        id="list-410">
                                        <div tabindex="-1"
                                          id="list-item-415"
                                          class="pa-0 v-list-item theme--light">
                                          <div
                                            class="v-list-item__title px-2">
                                            No users nor spaces was
                                            found</div>
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
                          <div class="v-card__actions mt-4 px-0">
                            <button type="button"
                              class="btn v-btn v-btn--contained theme--light v-size--default">
                              <span class="v-btn__content"><i
                                aria-hidden="true"
                                class="v-icon notranslate mr-2 fa fa-caret-left theme--light"
                                style="font-size: 18px;"></i> Back </span>
                            </button>
                          </div>
                          <!---->
                        </form>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <hr role="separator" aria-orientation="horizontal"
                class="my-0 v-divider theme--light">
              <div class="flex drawerFooter flex-grow-0 px-4 py-3">
                <div class="d-flex">
                  <div class="spacer"></div>
                  <button type="button"
                    class="btn mr-2 v-btn v-btn--contained theme--light v-size--default">
                    <span class="v-btn__content"> Cancel </span>
                  </button>
                  <button type="button" disabled="disabled"
                    class="btn btn-primary v-btn v-btn--contained v-btn--disabled theme--light v-size--default">
                    <span class="v-btn__content"> Update </span>
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