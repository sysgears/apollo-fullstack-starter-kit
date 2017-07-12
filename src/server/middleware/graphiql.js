import { graphiqlExpress } from 'graphql-server-express';

const subscriptionsUrl = __BACKEND_URL__.replace(/^http/, 'ws');

export default graphiqlExpress({
  endpointURL: '/graphql',
  subscriptionsEndpoint: subscriptionsUrl,
  query:
   '{\n' +
   '  count {\n' +
   '    amount\n' +
   '  }\n' +
   '}'
});
