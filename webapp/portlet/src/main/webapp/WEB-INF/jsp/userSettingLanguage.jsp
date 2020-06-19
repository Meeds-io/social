<%@page import="java.util.ArrayList"%>
<%@page import="org.json.JSONObject"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Locale"%>
<%@page import="org.exoplatform.web.application.RequestContext"%>
<%@page import="org.json.JSONArray"%>
<%@page import="java.util.Set"%>
<%@page
  import="org.exoplatform.portal.localization.LocaleContextInfoUtils"%>
<%
  Locale userLocale = RequestContext.getCurrentInstance().getLocale();

			Set<Locale> locales = LocaleContextInfoUtils.getSupportedLocales();
			List<Locale> localesList = new ArrayList(locales);
			JSONArray localesJSON = new JSONArray();
			for (Locale locale : localesList) {
				JSONObject object = new JSONObject();
				if (locale.toString().equals("ma")) {
					continue;
				} else {
					object.put("value", locale.toString());
					object.put("text", locale.getDisplayName(Locale.ENGLISH) + " / " + locale.getDisplayName(locale));
				}
				localesJSON.put(object);
			}
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr theme--light"
    id="UserSettingLanguage">
    <textarea id="userLanguagesValue" class="hidden"><%=localesJSON.toString()%></textarea>
    <script type="text/javascript">
      require(['PORTLET/social-portlet/UserSettingLanguage'], app => app.init(JSON.parse(document.getElementById('userLanguagesValue').value)));
    </script>
    <div class="v-application--wrap">
      <div
        class="ma-4 border-radius v-card v-card--flat v-sheet theme--light">
        <div role="list"
          class="v-list v-sheet v-sheet--tile theme--light v-list--two-line">
          <div tabindex="-1" role="listitem"
            class="v-list-item theme--light">
            <div class="v-list-item__content">
              <div class="v-list-item__title title text-color">
                <div
                  class="skeleton-background skeleton-border-radius skeleton-text-width skeleton-text-height my-2">
                  &nbsp;</div>
              </div>
              <div
                class="v-list-item__subtitle text-sub-title text-capitalize">
                <div
                  class="skeleton-background skeleton-border-radius skeleton-text-width-small skeleton-text-height-fine my-2">
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
  </div>
</div>