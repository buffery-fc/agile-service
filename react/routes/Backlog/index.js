import React from 'react';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const BacklogHome = asyncRouter(() => (import('./BacklogHome')), () => import('../../stores/project/backlog/BacklogStore'));

const BacklogIndex = ({ match }) => (
  <Switch>
    <Route path={`${match.url}`} component={BacklogHome} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default BacklogIndex;
