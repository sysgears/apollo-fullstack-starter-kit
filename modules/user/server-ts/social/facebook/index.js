import { pick } from 'lodash';
import { AuthModule } from '@module/authentication-server-ts';
import { onAuthenticationSuccess, registerUser } from '../shared';
import User from '../../sql';
import resolvers from './resolvers';
import settings from '../../../../../settings';

const createFacebookAuth = async user => User.createFacebookAuth(user);

async function verifyCallback(accessToken, refreshToken, profile, cb) {
  const {
    id,
    displayName,
    emails: [{ value }]
  } = profile;

  try {
    let user = await User.getUserByFbIdOrEmail(id, value);

    if (!user) {
      const [createdUserId] = await registerUser(profile);
      await createFacebookAuth({ id, displayName, userId: createdUserId });
      user = await User.getUser(createdUserId);
    } else if (!user.fbId) {
      await createFacebookAuth({ id, displayName, userId: user.id });
    }

    return cb(null, pick(user, ['id', 'username', 'role', 'email']));
  } catch (err) {
    return cb(err, {});
  }
}

export const facebookData = {
  facebook: {
    onAuthenticationSuccess,
    verifyCallback
  }
};

export default (settings.auth.social.facebook.enabled && !__TEST__
  ? new AuthModule({ createResolversFunc: [resolvers] })
  : undefined);
