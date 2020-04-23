import React, { ReactElement } from 'react';
import ReactDOMServer from 'react-dom/server';
import { SchemaLink } from 'apollo-link-schema';
import { ApolloProvider, getDataFromTree } from 'react-apollo';
import { Provider } from 'react-redux';
import { StaticRouter } from 'react-router';
import { ServerStyleSheet } from 'styled-components';
import fs from 'fs';
import path from 'path';
import Helmet, { HelmetData } from 'react-helmet';
import serialize from 'serialize-javascript';
import { GraphQLSchema } from 'graphql';
import { ChunkExtractor } from '@loadable/server';

import { isApiExternal, apiUrl } from '@gqlapp/core-common';
import ServerModule from '@gqlapp/module-server-ts';
import ClientModule from '@gqlapp/module-client-react';
import { createApolloClient, createReduxStore } from '@gqlapp/core-common';

let assetMap: { [key: string]: string };

interface HtmlProps {
  content: string;
  state: any;
  headElements: Array<ReactElement<{}>>;
  css: Array<ReactElement<{}>>;
  helmet: HelmetData;
}

let clientModules: ClientModule;
if (__SSR__) {
  clientModules = require('client').default;
  if (module.hot) {
    module.hot.accept(['client'], () => {
      clientModules = require('client').default;
    });
  }
}

const Html = ({ content, state, css, headElements, helmet }: HtmlProps) => (
  <html lang="en" {...helmet.htmlAttributes.toComponent()}>
    <head>
      {helmet.title.toComponent()}
      {helmet.meta.toComponent()}
      {helmet.link.toComponent()}
      {helmet.style.toComponent()}
      {helmet.script.toComponent()}
      {helmet.noscript.toComponent()}
      {assetMap['vendor.js'] && <script src={`${assetMap['vendor.js']}`} charSet="utf-8" />}
      {headElements}
      <meta charSet="utf-8" />
      <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1" />
      <link rel="apple-touch-icon" sizes="180x180" href={`${assetMap['apple-touch-icon.png']}`} />
      <link rel="icon" type="image/png" href={`${assetMap['favicon-32x32.png']}`} sizes="32x32" />
      <link rel="icon" type="image/png" href={`${assetMap['favicon-16x16.png']}`} sizes="16x16" />
      <link rel="manifest" href={`${assetMap['manifest.xjson']}`} />
      <link rel="mask-icon" href={`${assetMap['safari-pinned-tab.svg']}`} color="#5bbad5" />
      <link rel="shortcut icon" href={`${assetMap['favicon.ico']}`} />
      <meta name="msapplication-config" content={`${assetMap['browserconfig.xml']}`} />
      <meta name="theme-color" content="#ffffff" />
      {!!css && css}
    </head>
    <body {...helmet.bodyAttributes.toComponent()}>
      <div id="root" dangerouslySetInnerHTML={{ __html: content || '' }} />
      <script src="initServiceWorker.js" />
      <script
        dangerouslySetInnerHTML={{
          __html: `window.__APOLLO_STATE__=${serialize(state, {
            isJSON: true
          })};`
        }}
        charSet="UTF-8"
      />
    </body>
  </html>
);

const renderServerSide = async (req: any, res: any, schema: GraphQLSchema, modules: ServerModule) => {
  const schemaLink = new SchemaLink({
    schema,
    context: { ...(await modules.createContext(req, res)), req, res }
  });
  const client = createApolloClient({
    apiUrl,
    createNetLink: !isApiExternal ? () => schemaLink : undefined,
    createLink: clientModules.createLink,
    clientResolvers: clientModules.resolvers,
    connectionParams: null
  });
  const store = createReduxStore(clientModules.reducers, {}, client);
  const context: any = {};
  const App = clientModules.getWrappedRoot(
    <Provider store={store}>
      <ApolloProvider client={client}>
        {clientModules.getDataRoot(
          <StaticRouter location={req.url} context={context}>
            {clientModules.router}
          </StaticRouter>
        )}
      </ApolloProvider>
    </Provider>,
    req
  );

  await getDataFromTree(App);

  context.pageNotFound === true ? res.status(404) : res.status(200);

  if (context.url) {
    res.writeHead(307, { Location: context.url });
    res.end();
  } else {
    if (__DEV__ || !assetMap) {
      assetMap = JSON.parse(fs.readFileSync(path.join(__FRONTEND_BUILD_DIR__, 'assets.json')).toString());
      if (!__DEV__ && __CDN_URL__) {
        for (const key of Object.keys(assetMap)) {
          assetMap[key] = __CDN_URL__ + assetMap[key];
        }
      }
    }

    const extractor = new ChunkExtractor({
      statsFile: path.resolve(__FRONTEND_BUILD_DIR__, 'loadable-stats.json'),
      entrypoints: ['index'],
      publicPath: !__DEV__ && __CDN_URL__ ? __CDN_URL__ : '/'
    });
    const sheet = new ServerStyleSheet();
    const JSX = extractor.collectChunks(sheet.collectStyles(App));

    const content = ReactDOMServer.renderToString(JSX);
    const helmet = Helmet.renderStatic(); // Avoid memory leak while tracking mounted instances

    const htmlProps: HtmlProps = {
      content,
      headElements: [...extractor.getScriptElements(), ...extractor.getLinkElements(), ...extractor.getStyleElements()],
      css: sheet.getStyleElement().map((el, idx) => (el ? React.cloneElement(el, { key: idx }) : el)),
      helmet,
      state: { ...client.cache.extract() }
    };

    res.send(`<!doctype html>\n${ReactDOMServer.renderToStaticMarkup(<Html {...htmlProps} />)}`);
    res.end();
  }
};

export default (schema: GraphQLSchema, modules: ServerModule) => async (
  req: any,
  res: any,
  next: (e?: Error) => void
) => {
  try {
    if (req.path.indexOf('.') < 0 && __SSR__) {
      return await renderServerSide(req, res, schema, modules);
    } else if (req.path.indexOf('.') < 0 && !__SSR__ && req.method === 'GET' && !__DEV__) {
      res.sendFile(path.resolve(__FRONTEND_BUILD_DIR__, 'index.html'));
    } else {
      next();
    }
  } catch (e) {
    next(e);
  }
};
