import AccessModule from '../AccessModule';
import settings from '../../../../../settings';

const getCurrentIdentity = async ({ req, getIdentity }) => {
  if (req && req.headers['authorization']) {
    const [, serial] = req.headers['authorization'].split(' ');

    if (serial) {
      return getIdentity(null, serial);
    }
  }
};

const createContextFunc = async ({ req, graphqlContext, appContext }) => {
  const { getIdentity } = appContext;

  if (getIdentity) {
    const identity = graphqlContext.identity || (await getCurrentIdentity({ req, getIdentity }));

    return { identity };
  }
};

export default new AccessModule(
  settings.auth.serial.enabled
    ? {
        createContextFunc: [createContextFunc]
      }
    : {}
);
