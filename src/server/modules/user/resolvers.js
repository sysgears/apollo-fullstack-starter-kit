/*eslint-disable no-unused-vars*/
import { pick } from 'lodash';
import jwt from 'jsonwebtoken';
import { refreshTokens, tryLogin } from './auth';
import { requiresAuth, requiresAdmin } from './permissions';
import FieldError from '../../../common/FieldError';
import settings from '../../../../settings';

export default pubsub => ({
  Query: {
    users: requiresAdmin.createResolver((obj, { orderBy, filter }, context) => {
      return context.User.getUsers(orderBy, filter);
    }),
    user: requiresAuth.createResolver((obj, { id }, context) => {
      return context.User.getUser(id);
    }),
    currentUser(obj, args, context) {
      if (context.user) {
        return context.User.getUser(context.user.id);
      } else {
        return null;
      }
    }
  },
  Mutation: {
    async register(obj, { input }, context) {
      try {
        const e = new FieldError();

        const userExists = await context.User.getUserByUsername(input.username);
        if (userExists) {
          e.setError('username', 'Username already exists.');
        }

        const localAuth = pick(input, ['email', 'password']);
        const emailExists = await context.User.getLocalOuthByEmail(localAuth.email);
        if (emailExists) {
          e.setError('email', 'E-mail already exists.');
        }

        e.throwIf();

        let userId = 0;
        if (!emailExists) {
          let isActive = false;
          if (!settings.user.auth.password.confirm) {
            isActive = true;
          }

          const [createdUserId] = await context.User.register({ ...input, isActive });

          await context.User.createLocalOuth({
            ...localAuth,
            userId: createdUserId
          });
          userId = createdUserId;

          // if user has previously logged with facebook auth
        } else {
          await context.User.updatePassword(emailExists.userId, localAuth.password);
          userId = emailExists.userId;
        }

        const user = await context.User.getUser(userId);

        if (context.mailer && settings.user.auth.password.sendConfirmationEmail && !emailExists && context.req) {
          // async email
          jwt.sign({ user: pick(user, 'id') }, context.SECRET, { expiresIn: '1d' }, (err, emailToken) => {
            const encodedToken = Buffer.from(emailToken).toString('base64');
            const url = `${context.req.protocol}://${context.req.get('host')}/confirmation/${encodedToken}`;
            context.mailer.sendMail({
              from: 'Apollo Universal Starter Kit <nxau5pr4uc2jtb6u@ethereal.email>',
              to: localAuth.email,
              subject: 'Confirm Email',
              html: `Please click this email to confirm your email: <a href="${url}">${url}</a>`
            });
          });
        }

        return { user };
      } catch (e) {
        return { errors: e };
      }
    },
    async login(obj, { input: { email, password } }, context) {
      try {
        const tokens = await tryLogin(email, password, context.User, context.SECRET);
        if (context.req) {
          context.req.universalCookies.set('x-token', tokens.token, {
            maxAge: 60 * 60 * 24 * 7,
            httpOnly: true
          });
          context.req.universalCookies.set('x-refresh-token', tokens.refreshToken, {
            maxAge: 60 * 60 * 24 * 7,
            httpOnly: true
          });

          context.req.universalCookies.set('r-token', tokens.token, {
            maxAge: 60 * 60 * 24 * 7,
            httpOnly: false
          });
          context.req.universalCookies.set('r-refresh-token', tokens.refreshToken, {
            maxAge: 60 * 60 * 24 * 7,
            httpOnly: false
          });
        }
        return { tokens };
      } catch (e) {
        return { errors: e };
      }
    },
    async logout(obj, args, context) {
      if (context.req) {
        context.req.universalCookies.remove('x-token');
        context.req.universalCookies.remove('x-refresh-token');

        context.req.universalCookies.remove('r-token');
        context.req.universalCookies.remove('r-refresh-token');
      }

      return true;
    },
    refreshTokens(obj, { token, refreshToken }, context) {
      return refreshTokens(token, refreshToken, context.User, context.SECRET);
    },
    addUser: requiresAdmin.createResolver(async (obj, { input }, context) => {
      try {
        const e = new FieldError();

        const userExists = await context.User.getUserByUsername(input.username);
        if (userExists) {
          e.setError('username', 'Username already exists.');
        }

        const localAuth = pick(input, ['email', 'password']);
        const emailExists = await context.User.getLocalOuthByEmail(localAuth.email);
        if (emailExists) {
          e.setError('email', 'E-mail already exists.');
        }

        if (input.password.length < 5) {
          e.setError('password', `Password must be 5 characters or more.`);
        }

        e.throwIf();

        const [createdUserId] = await context.User.register({ ...input });

        await context.User.createLocalOuth({
          ...localAuth,
          userId: createdUserId
        });

        const user = await context.User.getUser(createdUserId);

        return { user };
      } catch (e) {
        return { errors: e };
      }
    }),
    editUser: requiresAdmin.createResolver(async (obj, { input }, context) => {
      try {
        const e = new FieldError();

        const userExists = await context.User.getUserByUsername(input.username);
        if (userExists && userExists.id !== input.id) {
          e.setError('username', 'Username already exists.');
        }

        const localAuth = pick(input, ['email', 'password']);
        const emailExists = await context.User.getLocalOuthByEmail(localAuth.email);
        if (emailExists && emailExists.id !== input.id) {
          e.setError('email', 'E-mail already exists.');
        }

        if (input.password && input.password.length < 5) {
          e.setError('password', `Password must be 5 characters or more.`);
        }

        e.throwIf();

        await context.User.editUser(input);
        const user = await context.User.getUser(input.id);
        return { user };
      } catch (e) {
        return { errors: e };
      }
    }),
    deleteUser: requiresAdmin.createResolver(async (obj, { id }, context) => {
      try {
        const e = new FieldError();
        const user = await context.User.getUser(id);
        if (!user) {
          e.setError('delete', 'User does not exist.');
          e.throwIf();
        }

        if (user.id === context.user.id) {
          e.setError('delete', 'You can not delete your self.');
          e.throwIf();
        }

        const isDeleted = await context.User.deleteUser(id);
        if (isDeleted) {
          return { user };
        } else {
          e.setError('delete', 'Could not delete user. Please try again later.');
          e.throwIf();
        }
      } catch (e) {
        return { errors: e };
      }
    }),
    async forgotPassword(obj, { input }, context) {
      try {
        const localAuth = pick(input, 'email');
        const user = await context.User.getLocalOuthByEmail(localAuth.email);

        if (user && context.mailer) {
          // async email
          jwt.sign(
            { email: user.email, password: user.password },
            context.SECRET,
            { expiresIn: '1d' },
            (err, emailToken) => {
              // encoded token since react router does not match dots in params
              const encodedToken = Buffer.from(emailToken).toString('base64');
              const url = `${context.req.protocol}://${context.req.get('host')}/reset-password/${encodedToken}`;
              context.mailer.sendMail({
                from: 'Apollo Universal Starter Kit <nxau5pr4uc2jtb6u@ethereal.email>',
                to: user.email,
                subject: 'Reset Password',
                html: `Please click this link to reset your password: <a href="${url}">${url}</a>`
              });
            }
          );
        }
        return true;
      } catch (e) {
        // always return true so you can't discover users this way
        return true;
      }
    },
    async resetPassword(obj, { input }, context) {
      try {
        const e = new FieldError();
        const reset = pick(input, ['password', 'passwordConfirmation', 'token']);
        if (reset.password !== reset.passwordConfirmation) {
          e.setError('password', 'Passwords do not match.');
        }

        if (reset.password.length < 5) {
          e.setError('password', `Password must be 5 characters or more.`);
        }
        e.throwIf();

        const token = Buffer.from(reset.token, 'base64').toString();
        const { email, password } = jwt.verify(token, context.SECRET);
        const user = await context.User.getLocalOuthByEmail(email);
        if (user.password !== password) {
          e.setError('token', 'Invalid token');
          e.throwIf();
        }

        if (user) {
          await context.User.updatePassword(user.id, reset.password);
        }
        return { errors: null };
      } catch (e) {
        return { errors: e };
      }
    }
  },
  Subscription: {}
});
