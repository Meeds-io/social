import ActivityStream from './components/ActivityStream.vue';
import ActivityStreamEmptyMessageSpace from './components/empty-stream/ActivityStreamEmptyMessageSpace.vue';
import ActivityStreamEmptyMessageUser from './components/empty-stream/ActivityStreamEmptyMessageUser.vue';
import ActivityNotFound from './components/empty-stream/ActivityNotFound.vue';
import ActivityAutoLink from './components/common/ActivityAutoLink.vue';
import ActivityNotificationAlerts from './components/common/ActivityNotificationAlerts.vue';
import ActivityStreamUpdater from './components/common/ActivityStreamUpdater.vue';
import ActivityStreamConfirm from './components/common/ActivityStreamConfirm.vue';
import ActivityStreamList from './components/activity/list/ActivityStreamList.vue';
import ActivityStreamActivity from './components/activity/ActivityStreamActivity.vue';
import ActivityStreamLoader from './components/activity/ActivityStreamLoader.vue';
import ActivityHead from './components/activity/header/ActivityHead.vue';
import ActivityHeadSpace from './components/activity/header/ActivityHeadSpace.vue';
import ActivityHeadUser from './components/activity/header/ActivityHeadUser.vue';
import ActivityHeadTime from './components/activity/header/ActivityHeadTime.vue';
import ActivityHeadMenu from './components/activity/header/ActivityHeadMenu.vue';
import ActivityBody from './components/activity/content/ActivityBody.vue';
import ActivityLink from './components/activity/content/ActivityLink.vue';
import ActivityShare from './components/activity/content/ActivityShare.vue';
import ActivityEmbeddedHTML from './components/activity/content/ActivityEmbeddedHTML.vue';
import ActivityFooter from './components/activity/footer/ActivityFooter.vue';
import ActivityShareInformation from './components/activity/footer/ActivityShareInformation.vue';
import ActivityActions from './components/activity/footer/ActivityActions.vue';
import ActivityCommentAction from './components/activity/footer/actions/ActivityCommentAction.vue';
import ActivityLikeAction from './components/activity/footer/actions/ActivityLikeAction.vue';
import ActivityShareAction from './components/activity/footer/actions/ActivityShareAction.vue';
import ActivityCommentsPreview from './components/comment/list/ActivityCommentsPreview.vue';
import ActivityCommentsDrawer from './components/comment/list/ActivityCommentsDrawer.vue';
import ActivityComments from './components/comment/list/ActivityComments.vue';
import ActivityComment from './components/comment/ActivityComment.vue';
import ActivityCommentMenu from './components/comment/header/ActivityCommentMenu.vue';
import ActivityCommentBodyText from './components/comment/content/ActivityCommentBodyText.vue';
import ActivityCommentRichText from './components/comment/content/ActivityCommentRichText.vue';
import ActivityCommentTime from './components/comment/footer/ActivityCommentTime.vue';
import ActivityCommentActions from './components/comment/footer/ActivityCommentActions.vue';
import ActivityCommentReplyAction from './components/comment/footer/actions/ActivityCommentReplyAction.vue';
import ActivityCommentLikeAction from './components/comment/footer/actions/ActivityCommentLikeAction.vue';

const components = {
  'activity-stream': ActivityStream,
  'activity-stream-list': ActivityStreamList,
  'activity-notification-alerts': ActivityNotificationAlerts,
  'activity-auto-link': ActivityAutoLink,
  'activity-stream-empty-message-space': ActivityStreamEmptyMessageSpace,
  'activity-stream-empty-message-user': ActivityStreamEmptyMessageUser,
  'activity-not-found': ActivityNotFound,
  'activity-stream-updater': ActivityStreamUpdater,
  'activity-stream-confirm-dialog': ActivityStreamConfirm,
  'activity-stream-activity': ActivityStreamActivity,
  'activity-stream-loader': ActivityStreamLoader,
  'activity-head': ActivityHead,
  'activity-head-space': ActivityHeadSpace,
  'activity-head-user': ActivityHeadUser,
  'activity-head-time': ActivityHeadTime,
  'activity-head-menu': ActivityHeadMenu,
  'activity-body': ActivityBody,
  'activity-link': ActivityLink,
  'activity-share': ActivityShare,
  'activity-embedded-html': ActivityEmbeddedHTML,
  'activity-footer': ActivityFooter,
  'activity-share-information': ActivityShareInformation,
  'activity-actions': ActivityActions,
  'activity-comment-action': ActivityCommentAction,
  'activity-like-action': ActivityLikeAction,
  'activity-share-action': ActivityShareAction,
  'activity-comments-preview': ActivityCommentsPreview,
  'activity-comments-drawer': ActivityCommentsDrawer,
  'activity-comments': ActivityComments,
  'activity-comment': ActivityComment,
  'activity-comment-body-text': ActivityCommentBodyText,
  'activity-comment-rich-text': ActivityCommentRichText,
  'activity-comment-time': ActivityCommentTime,
  'activity-comment-actions': ActivityCommentActions,
  'activity-comment-reply-action': ActivityCommentReplyAction,
  'activity-comment-like-action': ActivityCommentLikeAction,
  'activity-comment-menu': ActivityCommentMenu,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
