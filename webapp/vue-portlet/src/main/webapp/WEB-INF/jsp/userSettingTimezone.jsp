<%@page import="org.json.JSONObject"%>
<%@page import="java.util.TimeZone"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Locale"%>
<%@page import="org.exoplatform.web.application.RequestContext"%>
<%@page import="org.json.JSONArray"%>
<%@page import="java.util.Set"%>
<%@page import="org.exoplatform.commons.utils.TimeZoneUtils"%>
<%
  Locale userLocale = RequestContext.getCurrentInstance().getLocale();

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
  <div id="UserSettingTimezone">
    <textarea id="userTimezoneValue" class="hidden"><%=timeZoneJSON.toString()%></textarea>
    <script type="text/javascript">
      require(['PORTLET/social-vue-portlet/UserSettingTimezone'], app => app.init(JSON.parse(document.getElementById('userTimezoneValue').value)));
    </script>
  </div>
</div>