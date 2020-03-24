package org.exoplatform.social.processor;


import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.service.rest.LinkShare;


public class EmbedProcessorTest extends AbstractCoreTest {
  private EmbedProcessor processor;

    @Override
    public void setUp() throws Exception {
      super.setUp();
        processor = (EmbedProcessor) PortalContainer.getInstance().getComponentInstanceOfType(EmbedProcessor.class);

    }

    @Override
    public void tearDown() throws Exception {
      super.tearDown();
    }

    public void testProcessActivityWith0embedTagAndVideoLink() throws Exception {
      // Given
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      StringBuilder testTitle = new StringBuilder();
      testTitle.append("<p>test</p><oembed><a href=").append("https://youtu.be/jx1nBuBdtWo").append("target=").append("_blank").append(">https://youtu.be/jx1nBuBdtWo</a></oembed>");
      String embedMedia = LinkShare.getInstance("https://youtu.be/jx1nBuBdtWo").getMediaObject().getHtml();
      String body = "test test test";
      activity.setTitle(testTitle.toString());
      activity.setBody(body);
      // When
      processor.processActivity(activity);
      // Then
      assertEquals(activity.getTitle(), testTitle + embedMedia + "<p></p>");
    }

    public void testProcessActivityWithOutOembedTag() throws Exception {
      // Given
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      String title = "<p>test Title</p>";
      String body = "test test test";
      activity.setTitle(title);
      activity.setBody(body);
      // When
      processor.processActivity(activity);
      // Then
      assertEquals(activity.getTitle(), title);
    }

    public void testProcessActivityWith0embedTagAndArticleLink() throws Exception {
      // Given
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      StringBuilder testTitle = new StringBuilder();
      testTitle.append("<p>test</p><oembed><a href=").append("https://www.exoplatform.com/").append("target=").append("_blank").append(">https://www.exoplatform.com/</a></oembed>");
      String image = LinkShare.getInstance("https://www.exoplatform.com/").getImages().get(0);
      String description = LinkShare.getInstance("https://www.exoplatform.com/").getDescription();
      String title = LinkShare.getInstance("https://www.exoplatform.com/").getTitle();
      String body = "test test test";
      activity.setTitle(testTitle.toString());
      activity.setBody(body);
      // When
      processor.processActivity(activity);
      // Then
      assertEquals(activity.getTemplateParams().get("image"), image);
      assertEquals(activity.getTemplateParams().get("description"), description);
      assertEquals(activity.getTemplateParams().get("title"), title);
    }
}