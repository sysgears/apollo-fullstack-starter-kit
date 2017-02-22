import React from 'react'
import { createBatchingNetworkInterface } from 'apollo-client'
import { ApolloProvider } from 'react-apollo'
import { Router, browserHistory } from 'react-router'
import { syncHistoryWithStore } from 'react-router-redux'

import createApolloClient from '../apollo_client'
import createReduxStore from '../redux_store'
import addGraphQLSubscriptions from './subscriptions'
import routes from '../routes'
import { app as settings} from '../../package.json'

import '../ui/bootstrap.scss'
import '../ui/styles.scss'

// Favicon.ico should not be hashed, since some browsers expect it to be exactly on /favicon.ico URL
require('!file?name=[name].[ext]!../assets/favicon.ico'); // eslint-disable-line import/no-webpack-loader-syntax

// Require all files from assets dir recursively addding them into assets.json
var req = require.context('!file?name=[hash].[ext]!../assets', true, /.*/);
req.keys().map(req);

let networkInterface = createBatchingNetworkInterface({
  opts: {
    credentials: "same-origin",
  },
  batchInterval: 20,
  uri: "/graphql",
});

if (__CLIENT__) {
  const SubscriptionClient = require('subscriptions-transport-ws').SubscriptionClient;

  const wsClient = new SubscriptionClient(window.location.origin.replace(/^http/, 'ws')
    .replace(':' + settings.webpackDevPort, ':' + settings.apiPort));

  networkInterface = addGraphQLSubscriptions(
    networkInterface,
    wsClient,
  );
}

const client = createApolloClient(networkInterface);

let initialState = {};

if (window.__APOLLO_STATE__) {
  initialState = window.__APOLLO_STATE__;
}

const store = createReduxStore(initialState, client);

const history = syncHistoryWithStore(browserHistory, store);

export default class Main extends React.Component {
  render() {
    return (
      <ApolloProvider store={store} client={client}>
        <Router history={history}>
          {routes}
        </Router>
      </ApolloProvider>
    );
  }
}