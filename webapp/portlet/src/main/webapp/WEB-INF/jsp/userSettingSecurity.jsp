<%@ page import="org.gatein.sso.integration.SSOUtils" %>
<% boolean ssoEnabled = SSOUtils.isSSOEnabled(); %>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr theme--light"
    id="UserSettingSecurity">
    <script type="text/javascript">
      require(['PORTLET/social-portlet/UserSettingSecurity'],
        app => app.init(<%=ssoEnabled%>)
      );
    </script>
    <%
      if (!ssoEnabled) {
    %>
    <div class="v-application--wrap">
      <div
        class="ma-4 border-radius v-card v-card--flat v-sheet theme--light">
        <div role="list"
          class="v-list v-sheet v-sheet--tile theme--light">
          <div tabindex="-1" role="listitem"
            class="v-list-item theme--light">
            <div class="v-list-item__content">
              <div class="v-list-item__title title text-color">
                <div
                  class="skeleton-background skeleton-border-radius skeleton-text-width skeleton-text-height my-2">
                  &nbsp;</div>
              </div>
            </div>
            <div class="v-list-item__action">
              <button type="button"
                class="v-btn v-btn--flat v-btn--icon v-btn--round theme--light v-size--small skeleton-background">
                <span class="v-btn__content"></span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <%
      }
    %>
  </div>
</div>