import React from 'react';
import { Link } from 'react-router-dom';
import { Container, Row, Navbar, Nav, NavItem } from 'reactstrap';

import modules from '../modules';
import { LocaleToggle } from '../modules/language/containers/locale-toggle';

const NavBar = () => (
  <Navbar color="faded" light>
    <Container>
      <Row className="align-items-center">
        <Link to="/" className="navbar-brand">Apollo Starter Kit</Link>
        <Nav>
          {modules.navItems}
          <LocaleToggle />
        </Nav>
        {(!__PERSIST_GQL__ || __DEV__) && <Nav className="ml-auto" navbar>
          <NavItem>
            <a href="/graphiql">GraphiQL</a>
          </NavItem>
        </Nav>}
      </Row>
    </Container>
  </Navbar>
);

export default NavBar;
