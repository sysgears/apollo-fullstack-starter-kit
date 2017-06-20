import { expect } from 'chai';
import { step } from 'mocha-steps';

import Renderer from '../../../test-helpers/apollo_renderer';
import routes from '../../../../web/src/app/routes';

describe('Page not found example UI works', () => {
  const renderer = new Renderer({});
  let app;
  let content;

  step('404 page renders with sample text', () => {
    app = renderer.mount(routes);
    renderer.history.push('/non-existing-page');
    content = app.find('#content');
    expect(content.text()).to.include('Page not found - 404');
  });

  step('Clicking on home link works', () => {
    const homeLink = content.find('.home-link');
    homeLink.last().simulate("click", { button: 0 });
    expect(content.text()).to.not.include('Page not found - 404');
  });
});
