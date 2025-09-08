package io.meeds.social.activity.listener;

import jakarta.annotation.PostConstruct;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListener;
import org.exoplatform.social.core.activity.model.ActivityShareAction;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ActivitySharedActionsListener implements ActivityListener {

  @Autowired
  private PortalContainer container;

  private ActivityManager activityManager;

    @PostConstruct
  public void init() {
    activityManager = container.getComponentInstanceOfType(ActivityManager.class);
    activityManager.addActivityListener(this);
  }

  public void updateActivity(ActivityLifeCycleEvent event) {
    Set<ActivityShareAction> sharedActions = event.getActivity().getShareActions();
    for (ActivityShareAction action : sharedActions) {
      for (Long id : action.getSharedActivityIds()) {
        ExoSocialActivity sharedactivity =  activityManager.getActivity(id.toString());
        sharedactivity.setCacheTime(System.currentTimeMillis());
        activityManager.updateActivity(sharedactivity);
      }
    }
  }

}
