package org.exoplatform.social.webui.activity;

import junit.framework.TestCase;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;


public class UILinkActivityTest extends TestCase {

    @Test
    public void testGetMessage() {
        String linkComment = "test test";
        String message = "test message";
        UILinkActivityBuilder activityBuilder = new UILinkActivityBuilder();
        ExoSocialActivity activity = new ExoSocialActivityImpl();
        UILinkActivity linkUIActivity = new UILinkActivity();

        Map<String, String> activityParameters = new HashMap<>();
        activityParameters.put(UILinkActivity.COMMENT_PARAM, linkComment);
        activity.setTemplateParams(activityParameters);

        activityBuilder.extendUIActivity(linkUIActivity, activity);
        assertEquals(linkComment, linkUIActivity.getMessage());

        activityParameters.put(UILinkActivity.MESSAGE, message);
        activityBuilder.extendUIActivity(linkUIActivity, activity);

        assertEquals(message, linkUIActivity.getMessage());



    }
}