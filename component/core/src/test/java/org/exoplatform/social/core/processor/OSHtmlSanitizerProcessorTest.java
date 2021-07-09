/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.processor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.test.AbstractCoreTest;

/**
 * Unit Test for {@link OSHtmlSanitizerProcessor}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since  Jun 29, 2011
 */
public class OSHtmlSanitizerProcessorTest extends AbstractCoreTest {

  private OSHtmlSanitizerProcessor processor;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    processor = (OSHtmlSanitizerProcessor) PortalContainer.getInstance().
                                           getComponentInstanceOfType(OSHtmlSanitizerProcessor.class);

  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }


  public void testProcessActivity() throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    String sample = "this is a <strong> tag to keep</strong>";
    activity.setTitle(sample);
    activity.setBody(sample);
    Map<String, String> templateParams = new HashMap<>();
    templateParams.put("MESSAGE", sample);
    activity.setTemplateParams(templateParams);
    processor.processActivity(activity);

    assertEquals(sample, activity.getTitle());
    assertEquals(sample, activity.getBody());
    assertEquals(sample, activity.getTemplateParams().get("MESSAGE"));

    // tags with attributes
    sample = "text <a href='#'>bar</a> zed";

    activity.setTitle(sample);
    templateParams.put(BaseActivityProcessorPlugin.TEMPLATE_PARAM_TO_PROCESS,"comment");
    templateParams.put("MESSAGE", sample);
    templateParams.put("comment", sample);
    activity.setTemplateParams(templateParams);
    processor.processActivity(activity);
    assertEquals("comment|MESSAGE", templateParams.get(BaseActivityProcessorPlugin.TEMPLATE_PARAM_TO_PROCESS));
    assertEquals("text <a href=\"#\" rel=\"nofollow\" target=\"_blank\">bar</a> zed", activity.getTitle());
    assertEquals("text <a href=\"#\" rel=\"nofollow\" target=\"_blank\">bar</a> zed", activity.getTemplateParams().get("MESSAGE"));
    assertEquals("text <a href=\"#\" rel=\"nofollow\" target=\"_blank\">bar</a> zed", activity.getTemplateParams().get("comment"));

    // only with open tag
    sample = "<strong> only open!!!";
    activity.setTitle(sample);
    templateParams.put(BaseActivityProcessorPlugin.TEMPLATE_PARAM_TO_PROCESS,"comment");
    templateParams.put("MESSAGE", sample);
    templateParams.put("comment", sample);
    activity.setTemplateParams(templateParams);
    processor.processActivity(activity);
    assertEquals("<strong> only open!!!</strong>", activity.getTitle());
    assertEquals("<strong> only open!!!</strong>", activity.getTemplateParams().get("MESSAGE"));
    assertEquals("<strong> only open!!!</strong>", activity.getTemplateParams().get("comment"));
    // self closing tags
    sample = "<script href='#' />bar</a>";
    activity.setTitle(sample);
    templateParams.put("MESSAGE", sample);
    activity.setTemplateParams(templateParams);
    processor.processActivity(activity);
    assertEquals("bar&lt;/a&gt;", activity.getTitle());
    assertEquals("bar&lt;/a&gt;", activity.getTemplateParams().get("MESSAGE"));
    // forbidden tag
    sample = "<script>foo</script>";
    activity.setTitle(sample);
    templateParams.put("MESSAGE", sample);
    activity.setTemplateParams(templateParams);
    processor.processActivity(activity);
    assertEquals("", activity.getTitle());
    assertEquals("", activity.getTemplateParams().get("MESSAGE"));
    // embedded
    sample = "<span><strong>foo</strong>bar<script>zed</script></span>";
    activity.setTitle(sample);
    templateParams.put("MESSAGE", sample);
    activity.setTemplateParams(templateParams);
    processor.processActivity(activity);
    assertEquals("<strong>foo</strong>bar", activity.getTitle());
    assertEquals("<strong>foo</strong>bar", activity.getTemplateParams().get("MESSAGE"));
  }
  
  public void testProcessActivityWithTemplateParam() throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    String sample = "this is a <strong> tag to keep</strong>";
    activity.setTitle(sample);
    activity.setBody(sample);
    String keysToProcess = "a|b|c";
    Map<String, String> templateParams = new LinkedHashMap<String, String>();
    templateParams.put("a", "a\nb");
    templateParams.put("b", "exoplatform.com");
    templateParams.put("d", "exoplatform.com");
    templateParams.put(BaseActivityProcessorPlugin.TEMPLATE_PARAM_TO_PROCESS, keysToProcess);
    activity.setTemplateParams(templateParams);
    processor.processActivity(activity);
    
    templateParams = activity.getTemplateParams();
    assertEquals("a\nb", templateParams.get("a"));
    assertEquals("<a href=\"http://exoplatform.com\" rel=\"nofollow\" target=\"_blank\">exoplatform.com</a>", templateParams.get("b"));
    assertEquals("exoplatform.com", templateParams.get("d"));
  }
  
}
