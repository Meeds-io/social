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
					object.put("text", locale.getDisplayName(Locale.ENGLISH));
				}
				localesJSON.put(object);
			}
%>
<div class="VuetifyApp">
  <div data-app="true"
           class="v-application transparent v-application--is-ltr theme--light singlePageApplication"
           id="profileSettings" flat="">
    <textarea id="languagesValue" class="hidden"><%=localesJSON.toString()%></textarea>
    <script type="text/javascript">
                require(['PORTLET/social-portlet/ProfileSettingsPortlet'],
                    app => app.init(JSON.parse(document.getElementById('languagesValue').value)));
            </script>
  </div>
</div>