// React
import React from 'react';
import { Route, NavLink } from 'react-router-dom';

// Web UI
import { NavItem } from 'reactstrap';

// Component and helpers
import Subscription from './containers/Subscription';
import SubscribersOnly from './containers/SubscribersOnly';
import UpdateCard from './containers/UpdateCard';
import { SubscriberRoute } from './containers/Auth';
import reducers from './reducers';

import Feature from '../connector';

export default new Feature({
  route: [
    <Route exact path="/subscription" component={Subscription} />,
    <SubscriberRoute exact scope="user" path="/subscribers-only" component={SubscribersOnly} />,
    <SubscriberRoute exact scope="user" path="/update-card" component={UpdateCard} />
  ],
  navItem: [
    <NavItem>
      <NavLink to="/subscribers-only" className="nav-link" activeClassName="active">
        Subscribers Only
      </NavLink>
    </NavItem>
  ],
  reducer: { subscription: reducers },
  scriptsInsert: 'https://js.stripe.com/v3/'
});
