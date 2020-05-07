import SpaceMemberList from './components/SpaceMemberList.vue';
import SpaceMemberCardList from './components/SpaceMemberCardList.vue';
import SpaceMemberToolbar from './components/SpaceMemberToolbar.vue';
import SpaceMemberCard from './components/SpaceMemberCard.vue';
import SpaceMemberCardFront from './components/SpaceMemberCardFront.vue';
import SpaceMemberCardReverse from './components/SpaceMemberCardReverse.vue';

const components = {
  'space-members': SpaceMemberList,
  'space-member-card-list': SpaceMemberCardList,
  'space-member-toolbar': SpaceMemberToolbar,
  'space-member-card': SpaceMemberCard,
  'space-member-card-front': SpaceMemberCardFront,
  'space-member-card-reverse': SpaceMemberCardReverse,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
