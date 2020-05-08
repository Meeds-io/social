<%
  Object filter = request.getAttribute("filter");
			if (filter == null) {
				filter = "";
			} else {
				filter = ((String[]) filter)[0];
			}
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application transparent v-application--is-ltr theme--light peopleList"
    id="peopleListApplication" flat="">
    <script type="text/javascript">
      require(['PORTLET/social-vue-portlet/PeopleList'],
          app => app.init('<%=filter%>')
      );
    </script>
    <div class="v-application--wrap">
      <header
        class="v-sheet v-sheet--tile theme--light v-toolbar v-toolbar--flat"
        id="peopleListToolbar" style="height: 64px;">
        <div class="v-toolbar__content" style="height: 64px;">
          <div
            class="showingPeopleText text-sub-title ml-3 d-none d-sm-flex skeleton-text skeleton-background skeleton-border-radius">
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
                  <input disabled="disabled" id="input-65"
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
              value="all">Everyone</option>
            <option value="connections">My Connections</option></select>
          <button type="button"
            class="v-icon notranslate d-sm-none v-icon--link fa fa-filter theme--light skeleton-text"></button>
          <div role="dialog" class="v-dialog__container pa-0">
            <!---->
          </div>
        </div>
      </header>
      <div class="v-card v-card--flat v-sheet theme--light">
        <div id="peopleListBody" class="v-card__text pb-0">
          <div class="v-item-group theme--light">
            <div class="container pa-0">
              <div class="row ma-0 border-box-sizing">
                <div class="col-md-3 col-12 peopleCardFlip">
                  <div class="peopleCardFront">
                    <div
                      class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="userMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block peopleBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="peopleToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="peopleInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <button type="button" disabled="disabled"
                          class="peopleMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
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
                          class="userFullname text-truncate text-color font-weight-bold d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle userPositionLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions peopleCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <button type="button" disabled="disabled"
                          class="btn mx-auto connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            class="uiIconSocConnectUser d-none d-sm-inline"></i>
                            <span class="d-none d-sm-inline">
                              &nbsp; </span> <i aria-hidden="true"
                            class="v-icon notranslate d-inline d-sm-none mdi mdi-plus theme--light"></i></span>
                        </button>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 peopleCardFlip">
                  <div class="peopleCardFront">
                    <div
                      class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="userMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block peopleBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="peopleToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="peopleInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <button type="button" disabled="disabled"
                          class="peopleMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
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
                          class="userFullname text-truncate text-color font-weight-bold d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle userPositionLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions peopleCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <button type="button" disabled="disabled"
                          class="btn mx-auto connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            class="uiIconSocConnectUser d-none d-sm-inline"></i>
                            <span class="d-none d-sm-inline">
                              &nbsp; </span> <i aria-hidden="true"
                            class="v-icon notranslate d-inline d-sm-none mdi mdi-plus theme--light"></i></span>
                        </button>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 peopleCardFlip">
                  <div class="peopleCardFront">
                    <div
                      class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="userMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block peopleBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="peopleToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="peopleInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <button type="button" disabled="disabled"
                          class="peopleMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
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
                          class="userFullname text-truncate text-color font-weight-bold d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle userPositionLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions peopleCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <button type="button" disabled="disabled"
                          class="btn mx-auto connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            class="uiIconSocConnectUser d-none d-sm-inline"></i>
                            <span class="d-none d-sm-inline">
                              &nbsp; </span> <i aria-hidden="true"
                            class="v-icon notranslate d-inline d-sm-none mdi mdi-plus theme--light"></i></span>
                        </button>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 peopleCardFlip">
                  <div class="peopleCardFront">
                    <div
                      class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="userMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block peopleBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="peopleToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="peopleInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <button type="button" disabled="disabled"
                          class="peopleMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
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
                          class="userFullname text-truncate text-color font-weight-bold d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle userPositionLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions peopleCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <button type="button" disabled="disabled"
                          class="btn mx-auto connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            class="uiIconSocConnectUser d-none d-sm-inline"></i>
                            <span class="d-none d-sm-inline">
                              &nbsp; </span> <i aria-hidden="true"
                            class="v-icon notranslate d-inline d-sm-none mdi mdi-plus theme--light"></i></span>
                        </button>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 peopleCardFlip">
                  <div class="peopleCardFront">
                    <div
                      class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="userMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block peopleBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="peopleToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="peopleInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <button type="button" disabled="disabled"
                          class="peopleMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
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
                          class="userFullname text-truncate text-color font-weight-bold d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle userPositionLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions peopleCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <button type="button" disabled="disabled"
                          class="btn mx-auto connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            class="uiIconSocConnectUser d-none d-sm-inline"></i>
                            <span class="d-none d-sm-inline">
                              &nbsp; </span> <i aria-hidden="true"
                            class="v-icon notranslate d-inline d-sm-none mdi mdi-plus theme--light"></i></span>
                        </button>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 peopleCardFlip">
                  <div class="peopleCardFront">
                    <div
                      class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="userMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block peopleBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="peopleToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="peopleInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <button type="button" disabled="disabled"
                          class="peopleMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
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
                          class="userFullname text-truncate text-color font-weight-bold d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle userPositionLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions peopleCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <button type="button" disabled="disabled"
                          class="btn mx-auto connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            class="uiIconSocConnectUser d-none d-sm-inline"></i>
                            <span class="d-none d-sm-inline">
                              &nbsp; </span> <i aria-hidden="true"
                            class="v-icon notranslate d-inline d-sm-none mdi mdi-plus theme--light"></i></span>
                        </button>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 peopleCardFlip">
                  <div class="peopleCardFront">
                    <div
                      class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="userMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block peopleBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="peopleToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="peopleInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <button type="button" disabled="disabled"
                          class="peopleMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
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
                          class="userFullname text-truncate text-color font-weight-bold d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle userPositionLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions peopleCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <button type="button" disabled="disabled"
                          class="btn mx-auto connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            class="uiIconSocConnectUser d-none d-sm-inline"></i>
                            <span class="d-none d-sm-inline">
                              &nbsp; </span> <i aria-hidden="true"
                            class="v-icon notranslate d-inline d-sm-none mdi mdi-plus theme--light"></i></span>
                        </button>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 peopleCardFlip">
                  <div class="peopleCardFront">
                    <div
                      class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="userMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block peopleBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="peopleToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="peopleInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <button type="button" disabled="disabled"
                          class="peopleMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
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
                          class="userFullname text-truncate text-color font-weight-bold d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle userPositionLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions peopleCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <button type="button" disabled="disabled"
                          class="btn mx-auto connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            class="uiIconSocConnectUser d-none d-sm-inline"></i>
                            <span class="d-none d-sm-inline">
                              &nbsp; </span> <i aria-hidden="true"
                            class="v-icon notranslate d-inline d-sm-none mdi mdi-plus theme--light"></i></span>
                        </button>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 peopleCardFlip">
                  <div class="peopleCardFront">
                    <div
                      class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="userMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block peopleBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="peopleToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="peopleInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <button type="button" disabled="disabled"
                          class="peopleMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
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
                          class="userFullname text-truncate text-color font-weight-bold d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle userPositionLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions peopleCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <button type="button" disabled="disabled"
                          class="btn mx-auto connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            class="uiIconSocConnectUser d-none d-sm-inline"></i>
                            <span class="d-none d-sm-inline">
                              &nbsp; </span> <i aria-hidden="true"
                            class="v-icon notranslate d-inline d-sm-none mdi mdi-plus theme--light"></i></span>
                        </button>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 peopleCardFlip">
                  <div class="peopleCardFront">
                    <div
                      class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="userMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block peopleBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="peopleToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="peopleInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <button type="button" disabled="disabled"
                          class="peopleMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
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
                          class="userFullname text-truncate text-color font-weight-bold d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle userPositionLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions peopleCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <button type="button" disabled="disabled"
                          class="btn mx-auto connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            class="uiIconSocConnectUser d-none d-sm-inline"></i>
                            <span class="d-none d-sm-inline">
                              &nbsp; </span> <i aria-hidden="true"
                            class="v-icon notranslate d-inline d-sm-none mdi mdi-plus theme--light"></i></span>
                        </button>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 peopleCardFlip">
                  <div class="peopleCardFront">
                    <div
                      class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="userMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block peopleBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="peopleToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="peopleInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <button type="button" disabled="disabled"
                          class="peopleMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
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
                          class="userFullname text-truncate text-color font-weight-bold d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle userPositionLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions peopleCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <button type="button" disabled="disabled"
                          class="btn mx-auto connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            class="uiIconSocConnectUser d-none d-sm-inline"></i>
                            <span class="d-none d-sm-inline">
                              &nbsp; </span> <i aria-hidden="true"
                            class="v-icon notranslate d-inline d-sm-none mdi mdi-plus theme--light"></i></span>
                        </button>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 peopleCardFlip">
                  <div class="peopleCardFront">
                    <div
                      class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="userMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block peopleBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="peopleToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="peopleInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <button type="button" disabled="disabled"
                          class="peopleMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
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
                          class="userFullname text-truncate text-color font-weight-bold d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle userPositionLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions peopleCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <button type="button" disabled="disabled"
                          class="btn mx-auto connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            class="uiIconSocConnectUser d-none d-sm-inline"></i>
                            <span class="d-none d-sm-inline">
                              &nbsp; </span> <i aria-hidden="true"
                            class="v-icon notranslate d-inline d-sm-none mdi mdi-plus theme--light"></i></span>
                        </button>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 peopleCardFlip">
                  <div class="peopleCardFront">
                    <div
                      class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="userMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block peopleBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="peopleToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="peopleInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <button type="button" disabled="disabled"
                          class="peopleMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
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
                          class="userFullname text-truncate text-color font-weight-bold d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle userPositionLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions peopleCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <button type="button" disabled="disabled"
                          class="btn mx-auto connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            class="uiIconSocConnectUser d-none d-sm-inline"></i>
                            <span class="d-none d-sm-inline">
                              &nbsp; </span> <i aria-hidden="true"
                            class="v-icon notranslate d-inline d-sm-none mdi mdi-plus theme--light"></i></span>
                        </button>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 peopleCardFlip">
                  <div class="peopleCardFront">
                    <div
                      class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="userMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block peopleBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="peopleToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="peopleInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <button type="button" disabled="disabled"
                          class="peopleMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
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
                          class="userFullname text-truncate text-color font-weight-bold d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle userPositionLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions peopleCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <button type="button" disabled="disabled"
                          class="btn mx-auto connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            class="uiIconSocConnectUser d-none d-sm-inline"></i>
                            <span class="d-none d-sm-inline">
                              &nbsp; </span> <i aria-hidden="true"
                            class="v-icon notranslate d-inline d-sm-none mdi mdi-plus theme--light"></i></span>
                        </button>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 peopleCardFlip">
                  <div class="peopleCardFront">
                    <div
                      class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="userMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block peopleBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="peopleToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="peopleInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <button type="button" disabled="disabled"
                          class="peopleMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
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
                          class="userFullname text-truncate text-color font-weight-bold d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle userPositionLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions peopleCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <button type="button" disabled="disabled"
                          class="btn mx-auto connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            class="uiIconSocConnectUser d-none d-sm-inline"></i>
                            <span class="d-none d-sm-inline">
                              &nbsp; </span> <i aria-hidden="true"
                            class="v-icon notranslate d-inline d-sm-none mdi mdi-plus theme--light"></i></span>
                        </button>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 peopleCardFlip">
                  <div class="peopleCardFront">
                    <div
                      class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="userMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block peopleBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="peopleToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="peopleInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <button type="button" disabled="disabled"
                          class="peopleMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
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
                          class="userFullname text-truncate text-color font-weight-bold d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle userPositionLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions peopleCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <button type="button" disabled="disabled"
                          class="btn mx-auto connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            class="uiIconSocConnectUser d-none d-sm-inline"></i>
                            <span class="d-none d-sm-inline">
                              &nbsp; </span> <i aria-hidden="true"
                            class="v-icon notranslate d-inline d-sm-none mdi mdi-plus theme--light"></i></span>
                        </button>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 peopleCardFlip">
                  <div class="peopleCardFront">
                    <div
                      class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="userMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block peopleBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="peopleToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="peopleInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <button type="button" disabled="disabled"
                          class="peopleMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
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
                          class="userFullname text-truncate text-color font-weight-bold d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle userPositionLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions peopleCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <button type="button" disabled="disabled"
                          class="btn mx-auto connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            class="uiIconSocConnectUser d-none d-sm-inline"></i>
                            <span class="d-none d-sm-inline">
                              &nbsp; </span> <i aria-hidden="true"
                            class="v-icon notranslate d-inline d-sm-none mdi mdi-plus theme--light"></i></span>
                        </button>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 peopleCardFlip">
                  <div class="peopleCardFront">
                    <div
                      class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="userMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block peopleBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="peopleToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="peopleInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <button type="button" disabled="disabled"
                          class="peopleMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
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
                          class="userFullname text-truncate text-color font-weight-bold d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle userPositionLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions peopleCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <button type="button" disabled="disabled"
                          class="btn mx-auto connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            class="uiIconSocConnectUser d-none d-sm-inline"></i>
                            <span class="d-none d-sm-inline">
                              &nbsp; </span> <i aria-hidden="true"
                            class="v-icon notranslate d-inline d-sm-none mdi mdi-plus theme--light"></i></span>
                        </button>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 peopleCardFlip">
                  <div class="peopleCardFront">
                    <div
                      class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="userMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block peopleBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="peopleToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="peopleInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <button type="button" disabled="disabled"
                          class="peopleMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
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
                          class="userFullname text-truncate text-color font-weight-bold d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle userPositionLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions peopleCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <button type="button" disabled="disabled"
                          class="btn mx-auto connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            class="uiIconSocConnectUser d-none d-sm-inline"></i>
                            <span class="d-none d-sm-inline">
                              &nbsp; </span> <i aria-hidden="true"
                            class="v-icon notranslate d-inline d-sm-none mdi mdi-plus theme--light"></i></span>
                        </button>
                      </div>
                    </div>
                  </div>
                  <!---->
                </div>
                <div class="col-md-3 col-12 peopleCardFlip">
                  <div class="peopleCardFront">
                    <div
                      class="peopleCardItem d-block d-sm-flex v-card v-card--flat v-sheet theme--light"
                      id="userMenuParent" front="">
                      <div
                        class="v-responsive v-image white--text align-start d-none d-sm-block peopleBannerImg"
                        style="height: 80px;">
                        <div class="v-responsive__content"></div>
                      </div>
                      <div class="peopleToolbarIcons px-2">
                        <button type="button" disabled="disabled"
                          class="peopleInfoIcon d-none d-sm-flex v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate fa fa-info theme--light"
                            style="font-size: 12px;"></i></span>
                        </button>
                        <div class="spacer"></div>
                        <button type="button" disabled="disabled"
                          class="peopleMenuIcon d-none d-sm-block v-btn v-btn--disabled v-btn--flat v-btn--icon v-btn--round v-btn--text theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            aria-hidden="true"
                            class="v-icon notranslate mdi mdi-dots-vertical theme--light"
                            style="font-size: 21px;"></i></span>
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
                          class="userFullname text-truncate text-color font-weight-bold d-block">
                          &nbsp; </a>
                        <div
                          class="v-card__subtitle userPositionLabel py-0">
                          &nbsp;</div>
                      </div>
                      <div class="v-card__actions peopleCardActions">
                        <div role="dialog" class="v-dialog__container">
                          <!---->
                        </div>
                        <button type="button" disabled="disabled"
                          class="btn mx-auto connectUserButton v-btn v-btn--block v-btn--depressed v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
                          <span class="v-btn__content"><i
                            class="uiIconSocConnectUser d-none d-sm-inline"></i>
                            <span class="d-none d-sm-inline">
                              &nbsp; </span> <i aria-hidden="true"
                            class="v-icon notranslate d-inline d-sm-none mdi mdi-plus theme--light"></i></span>
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
        <div id="peopleListFooter"
          class="v-card__actions pt-0 px-5 border-box-sizing">
          <button type="button" disabled="disabled"
            class="loadMoreButton ma-auto btn v-btn v-btn--block v-btn--contained v-btn--disabled theme--light v-size--default skeleton-background skeleton-text">
            <span class="v-btn__content"> Show more </span>
          </button>
        </div>
      </div>
      <people-list-drawer></people-list-drawer>
    </div>
  </div>
</div>