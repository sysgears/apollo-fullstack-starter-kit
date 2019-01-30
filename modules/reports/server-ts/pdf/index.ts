import Report from '../sql';
import schema from './schema.graphql';
import resolvers from './resolvers';
import ReportModule from '../ReportModule';
import resources from './locales';

export default new ReportModule({
  schema: [schema],
  createResolversFunc: [resolvers],
  createContextFunc: [() => ({ Report: new Report() })],
  localization: [{ ns: 'PdfReport', resources }]
});
