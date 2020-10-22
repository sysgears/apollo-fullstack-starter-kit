import { access } from '@gqlapp/authentication-server-ts';
import bcrypt from 'bcryptjs';
import User from '../sql';

export async function onAuthenticationSuccess(req, res) {
  const user = await User.getUserWithPassword(req.user.id);
  const redirectUrl = req.query.state;
  const tokens = await access.grantAccess(user, req, user.passwordHash);

  if (redirectUrl) {
    res.redirect(redirectUrl + (tokens ? '?data=' + JSON.stringify({ tokens }) : ''));
  } else {
    res.redirect('/profile');
  }
}

export const registerUser = async ({ id, username, displayName, emails: [{ value }] }) => {
  const passwordHash = await bcrypt.hash(id || username || displayName, 12); 
  return User.register({
    username: username || displayName,
    email: value,
    isActive: true
  },
   passwordHash
  );
};
