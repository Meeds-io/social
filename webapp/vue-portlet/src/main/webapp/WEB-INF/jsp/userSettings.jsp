<%@page import="java.util.Collections"%>
<%@page import="java.util.ArrayList"%>
<%@page import="org.json.JSONObject"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.TimeZone"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Locale"%>
<%@page import="org.exoplatform.web.application.RequestContext"%>
<%@page import="org.json.JSONArray"%>
<%@page import="java.util.Set"%>
<%@page import="org.exoplatform.portal.localization.LocaleContextInfoUtils"%>
<%@page import="org.exoplatform.commons.utils.TimeZoneUtils"%>
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

  List<TimeZone> timeZones = TimeZoneUtils.getTimeZones();
  JSONArray timeZoneJSON = new JSONArray();
  for (TimeZone timeZone : timeZones) {
    JSONObject object = new JSONObject();
    object.put("id", timeZone.getID());
    object.put("offset", timeZone.getRawOffset());
    object.put("text", TimeZoneUtils.getTimeZoneDisplay(timeZone, userLocale));
    timeZoneJSON.put(object);
  }
%>

<div class="VuetifyApp">
  <div id="UserSettings">
    <textarea id="userLocalesValue" class="hidden">{
      "languages": <%=localesJSON.toString()%>,
      "timezones": <%=timeZoneJSON.toString()%>
    }</textarea>
    <script type="text/javascript">
      require(['PORTLET/social-vue-portlet/UserSettings'], app => app.init(JSON.parse(document.getElementById('userLocalesValue').value)));
    </script>
  </div>
</div>