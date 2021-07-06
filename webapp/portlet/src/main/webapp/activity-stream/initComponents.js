import ActivityStream from './components/ActivityStream.vue';
import ActivityStreamFeatureSwitch from './components/ActivityStreamFeatureSwitch.vue';
import ActivityStreamList from './components/list/ActivityStreamList.vue';
import ActivityStreamEmptyMessageSpace from './components/list/ActivityStreamEmptyMessageSpace.vue';
import ActivityStreamEmptyMessageUser from './components/list/ActivityStreamEmptyMessageUser.vue';
import ActivityStreamUpdater from './components/list/ActivityStreamUpdater.vue';
import ActivityStreamConfirm from './components/list/ActivityStreamConfirm.vue';
import ActivityStreamActivity from './components/activity/ActivityStreamActivity.vue';
import ActivityHead from './components/activity/header/ActivityHead.vue';
import ActivityHeadSpace from './components/activity/header/ActivityHeadSpace.vue';
import ActivityHeadUser from './components/activity/header/ActivityHeadUser.vue';
import ActivityHeadTime from './components/activity/header/ActivityHeadTime.vue';
import ActivityHeadMenu from './components/activity/header/ActivityHeadMenu.vue';
import ActivityBody from './components/activity/content/ActivityBody.vue';
import ActivityLink from './components/activity/content/ActivityLink.vue';
import ActivityEmbeddedHTML from './components/activity/content/ActivityEmbeddedHTML.vue';
import ActivityFooter from './components/activity/footer/ActivityFooter.vue';
import ActivityActions from './components/activity/footer/ActivityActions.vue';
import ActivityCommentAction from './components/activity/footer/actions/ActivityCommentAction.vue';
import ActivityLikeAction from './components/activity/footer/actions/ActivityLikeAction.vue';
import ActivityCommentsPreview from './components/comment/ActivityCommentsPreview.vue';
import ActivityCommentsDrawer from './components/comment/ActivityCommentsDrawer.vue';
import ActivityComments from './components/comment/ActivityComments.vue';
import ActivityCommentBody from './components/comment/content/ActivityCommentBody.vue';
import ActivityCommentBodyText from './components/comment/content/ActivityCommentBodyText.vue';
import ActivityCommentRichText from './components/comment/content/ActivityCommentRichText.vue';
import ActivityCommentActions from './components/comment/footer/ActivityCommentActions.vue';
import ActivityCommentReplyAction from './components/comment/footer/actions/ActivityCommentReplyAction.vue';
import ActivityCommentLikeAction from './components/comment/footer/actions/ActivityCommentLikeAction.vue';
import ActivityCommentMenu from './components/comment/header/ActivityCommentMenu.vue';

const components = {
  'activity-stream': ActivityStream,
  'activity-stream-feature-switch': ActivityStreamFeatureSwitch,
  'activity-stream-list': ActivityStreamList,
  'activity-stream-empty-message-space': ActivityStreamEmptyMessageSpace,
  'activity-stream-empty-message-user': ActivityStreamEmptyMessageUser,
  'activity-stream-updater': ActivityStreamUpdater,
  'activity-stream-confirm-dialog': ActivityStreamConfirm,
  'activity-stream-activity': ActivityStreamActivity,
  'activity-head': ActivityHead,
  'activity-head-space': ActivityHeadSpace,
  'activity-head-user': ActivityHeadUser,
  'activity-head-time': ActivityHeadTime,
  'activity-head-menu': ActivityHeadMenu,
  'activity-body': ActivityBody,
  'activity-link': ActivityLink,
  'activity-embedded-html': ActivityEmbeddedHTML,
  'activity-footer': ActivityFooter,
  'activity-actions': ActivityActions,
  'activity-comment-action': ActivityCommentAction,
  'activity-like-action': ActivityLikeAction,
  'activity-comments-preview': ActivityCommentsPreview,
  'activity-comments-drawer': ActivityCommentsDrawer,
  'activity-comments': ActivityComments,
  'activity-comment-body': ActivityCommentBody,
  'activity-comment-body-text': ActivityCommentBodyText,
  'activity-comment-rich-text': ActivityCommentRichText,
  'activity-comment-actions': ActivityCommentActions,
  'activity-comment-reply-action': ActivityCommentReplyAction,
  'activity-comment-like-action': ActivityCommentLikeAction,
  'activity-comment-menu': ActivityCommentMenu,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
