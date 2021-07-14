/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.webui.activity;

import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.lifecycle.WebuiBindingContext;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * @deprecated Kept for backward compatibility for conf of other community
 *             addons deployed on Tribe
 */
@Deprecated
public class BaseUIActivity extends UIForm {
  protected static final int  LIKES_NUM_DEFAULT              = 0;

  private static final Log    LOG                            = ExoLogger.getLogger(BaseUIActivity.class);

  private static final int    DEFAULT_LIMIT                  = 10;

  private static final String HTML_AT_SYMBOL_PATTERN         = "@";

  private static final String HTML_AT_SYMBOL_ESCAPED_PATTERN = "&#64;";

  private static final String HTML_ATTRIBUTE_TITLE           = "title";

  public static final String  TEMPLATE_PARAM_COMMENT         = "comment";

  public static final String  COMPOSER_TEXT_AREA_EDIT_INPUT  = "composerEditInput";

  private static int          LATEST_COMMENTS_SIZE           = 2;

  public BaseUIActivity() {
  }

  public RealtimeListAccess<ExoSocialActivity> getActivityCommentsListAccess() {
    return null;
  }

  public void setActivityCommentsListAccess(RealtimeListAccess<ExoSocialActivity> activityCommentsListAccess) {
  }

  public String getSpaceGroupId() {
    Space space = SpaceUtils.getSpaceByContext();
    return space == null ? "" : space.getGroupId();
  }

  public String getSpaceURL() {
    String spaceURL = SpaceUtils.getSpaceUrlByContext();
    return spaceURL;
  }

  public int getCurrentLoadIndex() {
    return 0;
  }

  public int getLoadCapacity() {
    return 0;
  }

  public void setLoadCapacity(int loadCapacity) {
  }

  public ExoSocialActivity getActivity() {
    return null;
  }

  public void setActivity(ExoSocialActivity activity) {
  }

  public int getCommentMinCharactersAllowed() {
    return 0;
  }

  public void setCommentMinCharactersAllowed(int num) {
  }

  public int getCommentMaxCharactersAllowed() {
    return 0;
  }

  public void setCommentMaxCharactersAllowed(int num) {
  }

  public boolean isCommentFormDisplayed() {
    return false;
  }

  public void setCommentFormDisplayed(boolean commentFormDisplayed) {
  }

  public boolean isAllLoaded() {
    return false;
  }

  public void setAllLoaded(boolean allLoaded) {
  }

  public boolean isAllCommentsHidden() {
    return false;
  }

  public void setAllCommentsHidden(boolean allCommentsHidden) {
  }

  public boolean isCommentFormFocused() {
    return false;
  }

  public void setCommentFormFocused(boolean commentFormFocused) {
  }

  public CommentStatus getCommentListStatus() {
    return null;
  }

  public void setCommentListStatus(CommentStatus status) {
  }

  public boolean commentListToggleable() {
    return false;
  }

  public List<ExoSocialActivity> getComments() {
    return null;
  }

  public int getParentCommentsSize(List<ExoSocialActivity> comments) {
    return 0;
  }

  public int getSubCommentsSize(List<ExoSocialActivity> comments, String commentActivityId) {
    return 0;
  }

  public boolean isSubCommentOfComment(List<ExoSocialActivity> comments, String commentId, String subCommentId) {
    return false;
  }

  public List<ExoSocialActivity> getAllComments() {
    return null;
  }

  public int getAllCommentSize() {
    return 0;
  }

  public String[] getIdentityLikes() {
    return null;
  }

  public String[] getDisplayedIdentityLikes() throws Exception {
    return null;
  }

  public void setIdenityLikes(String[] identityLikes) {
  }

  public String event(String actionName, String callback, boolean updateForm) throws Exception {
    return null;
  }

  public String getAbsolutePostedTime(Long postedTime) {
    return null;
  }

  @Deprecated
  public String getPostedTimeString(WebuiBindingContext webuiBindingContext, long postedTime) throws Exception {
    return null;
  }

  public String getRelativeTimeLabel(WebuiBindingContext webuiBindingContext, long postedTime) {
    return null;
  }

  protected ExoSocialActivity saveComment(String remoteUser, String message, String commentId) throws Exception {
    return null;
  }

  protected void editActivity(String message) {
  }

  protected void hideActivity() {
  }

  protected ExoSocialActivity editCommentMessage(ExoSocialActivity commentActivity, String message) {
    return null;
  }

  public void setLike(boolean isLiked) throws Exception {
  }

  public void setLikeComment(boolean isLiked, String commentId) {
  }

  public String getAndSetUpdatedCommentId(String newValue) {
    return null;
  }

  protected String getActivityPermalink(String activityId) {
    return null;
  }

  protected String getCommentPermalink(String activityId, String commentId) {
    return null;
  }

  public boolean isLiked() throws Exception {
    return false;
  }

  protected void refresh() throws ActivityStorageException {
  }

  public boolean isUserActivity() {
    return false;
  }

  public boolean isSpaceActivity() {
    return false;
  }

  public boolean isActivityDeletable() throws SpaceException {
    return false;
  }

  public boolean isActivityShareable() {
    return false;
  }

  public boolean isActivityEditable(ExoSocialActivity activity) {
    return false;
  }

  public boolean isActivityCommentAndLikable() throws Exception {
    return false;
  }

  public boolean isActivityCommentable() throws Exception {
    return false;
  }

  public boolean isCommentDeletable(String activityUserId) throws SpaceException {
    return false;
  }

  public Identity getOwnerIdentity() {
    return null;
  }

  public void setOwnerIdentity(Identity ownerIdentity) {
  }

  public Identity getSpaceCreatorIdentity() {
    return null;
  }

  protected boolean isSpaceStreamOwner() {
    return false;
  }

  protected boolean isUISpaceActivitiesDisplay() {
    return false;
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
  }

  protected boolean isNoLongerExisting(String activityId, Event<BaseUIActivity> event) {
    return false;
  }

  protected boolean isNoLongerExisting(String activityId) {
    return false;
  }

  public boolean isDeletedSpace(String streamOwner) {
    return false;
  }

  public Identity getCommenterIdentity() {
    return Utils.getViewerIdentity();
  }

  protected ExoSocialActivity getI18N(ExoSocialActivity activity) {
    return null;
  }

  private List<ExoSocialActivity> getI18N(List<ExoSocialActivity> comments) {
    return null;
  }

  protected void focusToComment(String commentId) {
  }

  public static enum CommentStatus {
    LATEST("latest"),
    ALL("all"),
    NONE("none");

    private String commentStatus;

    private CommentStatus(String status) {
      commentStatus = status;
    }

    public String getStatus() {
      return commentStatus;
    }
  }

  public static class RefreshActivityActionListener extends EventListener<BaseUIActivity> {
    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
    }
  }

  public static class LoadLikesActionListener extends EventListener<BaseUIActivity> {
    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {

    }
  }

  public static class LikeActivityActionListener extends EventListener<BaseUIActivity> {
    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {

    }
  }

  public static class SetCommentListStatusActionListener extends EventListener<BaseUIActivity> {
    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {

    }
  }

  public static class ToggleDisplayCommentFormActionListener extends EventListener<BaseUIActivity> {
    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {

    }
  }

  public static class PostCommentActionListener extends EventListener<BaseUIActivity> {
    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {

    }
  }

  public static class DeleteActivityActionListener extends EventListener<BaseUIActivity> {

    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
    }
  }

  public static class HideActivityActionListener extends EventListener<BaseUIActivity> {

    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
    }
  }

  public static class DeleteCommentActionListener extends EventListener<BaseUIActivity> {
    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
    }
  }

  public static class LikeCommentActionListener extends EventListener<BaseUIActivity> {
    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
    }
  }

  public static class EditActivityActionListener extends EventListener<BaseUIActivity> {
    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
    }
  }

  public static class EditCommentActionListener extends EventListener<BaseUIActivity> {
    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
    }
  }
}
