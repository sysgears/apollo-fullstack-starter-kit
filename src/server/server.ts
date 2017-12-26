import 'zone.js/dist/zone-node';
import 'reflect-metadata';
import 'rxjs/Rx';
import { enableProdMode } from '@angular/core';
import * as express from 'express';
import * as http from 'http';
import { NextFunction, Request, Response } from 'express';
import { createServer, Server } from 'http';
import * as compression from 'compression';
import { ngExpressEngine } from '@nguniversal/express-engine';
import * as bodyParser from 'body-parser';
import * as cors from 'cors';
import { invert, isArray } from 'lodash';
import * as path from 'path';
// import * as cookiesMiddleware from 'universal-cookie-express';
// import * as queryMap from 'persisted_queries.json';
import * as url from 'url';

import { AppServerModule } from './app/app.server.module';
import { modules } from './modules';
import * as spinRc from '../../.spinrc.json';
import { log } from '../common/log';
import { addGraphQLSubscriptions } from './api/subscriptions';
import { graphiqlMiddleware } from './middleware/graphiql';
import { graphqlMiddleware } from './middleware/graphql';
// import websiteMiddleware from './middleware/website';

enableProdMode();
const spinConfig = spinRc.options;
let server: Server;
const app = express();
app.use(compression());

app.engine(
  'html',
  ngExpressEngine({
    bootstrap: AppServerModule
  })
);

for (const applyBeforeware of modules.beforewares) {
  applyBeforeware(app);
}

// app.use(cookiesMiddleware());

const { port, pathname } = url.parse(__BACKEND_URL__);
const serverPort = process.env.PORT || port || 8080;

// Don't rate limit heroku
app.enable('trust proxy');

if (__DEV__) {
  const corsOptions = {
    credentials: true,
    origin: (origin: any, callback: any) => {
      callback(null, true);
    }
  };
  app.use(cors(corsOptions));
}

app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());

app.set('view engine', 'html');
app.set('views', 'build/client');
app.use('/', express.static(spinConfig.frontendBuildDir, { index: false }));
// app.use(
//   '/',
//   express.static(path.join(spinConfig.frontendBuildDir, 'web'), {
//     maxAge: '180 days'
//   })
// );

if (__DEV__) {
  app.use('/', express.static(spinConfig.dllBuildDir, { maxAge: '180 days' }));
}

// if (__PERSIST_GQL__) {
//   const invertedMap = invert(queryMap);
//
//   app.use(pathname, (req, resp, next) => {
//     if (isArray(req.body)) {
//       req.body = req.body.map(body => {
//         return {
//           query: invertedMap[body.id],
//           ...body
//         };
//       });
//       next();
//     } else {
//       if (!__DEV__ || (req.get('Referer') || '').indexOf('/graphiql') < 0) {
//         resp.status(500).send('Unknown GraphQL query has been received, rejecting...');
//       } else {
//         next();
//       }
//     }
//   });
// }

for (const applyMiddleware of modules.middlewares) {
  applyMiddleware(app);
}

app.get('*', (req, res) => {
  res.render('../client/index.html', {
    req,
    res
  });
});

app.use(pathname, (req: Request, res: Response, next: NextFunction) => graphqlMiddleware(req, res, next));
app.use('/graphiql', (req: Request, res: Response, next: NextFunction) => graphiqlMiddleware(req, res, next));
// app.use((req: Request, res: Response, next: NextFunction) => websiteMiddleware(queryMap)(req, res, next));

server = http.createServer(app);

addGraphQLSubscriptions(server);

server.listen(serverPort, () => {
  log.info(`API is now running on port ${serverPort}`);
});

server.on('close', () => {
  server = undefined;
});

if (module.hot) {
  module.hot.dispose(() => {
    try {
      if (server) {
        server.close();
      }
    } catch (error) {
      log(error.stack);
    }
  });
  module.hot.accept(['./middleware/website', './middleware/graphql'], () => {});
  module.hot.accept(['./api/subscriptions'], () => {
    try {
      addGraphQLSubscriptions(server);
    } catch (error) {
      log(error.stack);
    }
  });

  module.hot.accept();
}

export { server };
