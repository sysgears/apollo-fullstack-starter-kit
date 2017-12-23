import React from 'react';
import { Route, NavLink } from 'react-router-dom';
import { MenuItem } from '../../plugins/common/components/web';
import Contact from './containers/Contact';

import Plugin, { featureCatalog } from '../plugin';

console.log('catalog:', featureCatalog);

export default new Plugin({
  route: <Route exact path="/contact" component={Contact} />,
  navItem: (
    <MenuItem key="contact">
      <NavLink to="/contact" className="nav-link" activeClassName="active">
        Contact Us
      </NavLink>
    </MenuItem>
  )
});
