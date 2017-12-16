import DataLoader from 'dataloader';
import jwt from 'jsonwebtoken';

import Auth from './lib';
import schema from './schema.graphqls';
import createResolvers from './resolvers';
import Feature from '../connector';

import { confirmAccountHandler } from './flow/confirm';
import authTokenMiddleware from './middleware/token';

import OAuth from './oauth';
import { refreshToken } from './flow/token';

import settings from '../../../../settings';

const SECRET = settings.auth.secret;
const authn = settings.auth.authentication;
const authz = settings.auth.authorization;

const localAuth = new Auth();

export default new Feature({
  schema,
  createResolversFunc: createResolvers,
  createContextFunc: async (req, connectionParams, webSocket) => {
    const tokenUser = await parseUser({ req, connectionParams, webSocket });

    let userAuth = {};
    if (authz.method === 'basic') {
      const scopes = authz.basic.scopes;
      const userScopes = tokenUser ? scopes[tokenUser.role] : null;

      userAuth = {
        isAuthenticated: tokenUser ? true : false,
        scope: userScopes
      };
    } else if (authz.method === 'rbac') {
      // TODO
      userAuth = {
        isAuthenticated: tokenUser ? true : false,
        scope: null
      };
    }

    const auth = userAuth;

    return {
      Auth: localAuth,
      user: tokenUser,
      auth,
      SECRET,
      req,
      loaders: {
        getUserWithApiKeys: new DataLoader(localAuth.getUserWithApiKeys),
        getUserWithSerials: new DataLoader(localAuth.getUserWithSerials),
        getUserWithOAuths: new DataLoader(localAuth.getUserWithOAuths)
      }
    };
  },
  middleware: app => {
    app.use(authTokenMiddleware(localAuth));
    if (authn.password.sendConfirmationEmail) {
      app.get('/confirmation/:token', confirmAccountHandler);
    }

    if (authn.oauth.enabled === true) {
      OAuth.Enable(app);
    }
  }
});

/*
 * Extracts a user from the connection, looks in the order:
 *  - jwt-token
 *  - apikey
 *  - certificate
 */
export const parseUser = async ({ req, connectionParams, webSocket }) => {
  if (
    connectionParams &&
    connectionParams.token &&
    connectionParams.token !== 'null' &&
    connectionParams.token !== 'undefined'
  ) {
    try {
      const { user } = jwt.verify(connectionParams.token, SECRET);
      return user;
    } catch (err) {
      const newToken = await refreshToken(connectionParams.token, connectionParams.refreshToken, SECRET);
      return newToken.user;
    }
  } else if (req) {
    if (req.user) {
      return req.user;
    }
    if (authn.apikey.enabled) {
      let apikey = '';
      // in case you need to access req headers
      if (req.headers['apikey']) {
        apikey = req.headers['apikey'];
      }

      if (apikey !== '') {
        const user = await Auth.getUserFromApiKey(apikey);
        if (user) {
          return user;
        }
      }
    }

    if (authn.certificate.enabled) {
      let serial = '';
      // in case you need to access req headers
      if (req.headers['x-serial']) {
        serial = req.headers['x-serial'];
      }

      if (serial !== '') {
        const user = await Auth.getUserWithSerial(serial);
        if (user) {
          return user;
        }
      }
    }
  } else if (webSocket) {
    if (authn.apikey.enabled) {
      let apikey = '';
      // in case you need to access req headers
      if (webSocket.upgradeReq.headers['apikey']) {
        apikey = webSocket.upgradeReq.headers['apikey'];
      }

      if (apikey !== '') {
        const user = await Auth.getUserFromApiKey(apikey);
        if (user) {
          return user;
        }
      }
    } else if (authn.certificate.enabled) {
      let serial = '';
      // in case you need to access req headers
      if (webSocket.upgradeReq.headers['x-serial']) {
        serial = webSocket.upgradeReq.headers['x-serial'];
      }

      if (serial !== '') {
        const user = await Auth.getUserWithSerial(serial);
        if (user) {
          return user;
        }
      }
    }
  }
};
