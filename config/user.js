const CERTIFICATE_DEVSERIAL = '00';
export default {
  secret: process.env.NODE_ENV === 'test' ? 'secret for tests' : process.env.AUTH_SECRET,
  auth: {
    access: {
      session: {
        enabled: true
      },
      jwt: {
        enabled: true,
        tokenExpiresIn: '1m',
        refreshTokenExpiresIn: '7d'
      }
    },
    password: {
      confirm: true,
      sendConfirmationEmail: true,
      sendAddNewUserEmail: true,
      minLength: 8,
      enabled: true
    },
    certificate: {
      devSerial: CERTIFICATE_DEVSERIAL,
      enabled: false
    },
    facebook: {
      enabled: false,
      clientID: process.env.FACEBOOK_CLIENTID,
      clientSecret: process.env.FACEBOOK_CLIENTSECRET,
      callbackURL: '/auth/facebook/callback',
      scope: ['email'],
      profileFields: ['id', 'emails', 'displayName']
    },
    github: {
      enabled: false,
      clientID: process.env.GITHUB_CLIENTID,
      clientSecret: process.env.GITHUB_CLIENTSECRET,
      callbackURL: '/auth/github/callback',
      scope: ['user:email']
    },
    linkedin: {
      enabled: false,
      clientID: process.env.LINKEDIN_CLIENTID,
      clientSecret: process.env.LINKEDIN_CLIENTSECRET,
      callbackURL: '/auth/linkedin/callback',
      scope: ['r_emailaddress', 'r_basicprofile']
    },
    google: {
      enabled: true,
      clientID: '936744536604-phfojlvtniin07oukj07b33qju8edjdn.apps.googleusercontent.com',
      clientSecret: 'rq_IhawCIRqWezn4Y_QDah0Z',
      callbackURL: '/auth/google/callback',
      scope: ['https://www.googleapis.com/auth/userinfo.email', 'https://www.googleapis.com/auth/userinfo.profile']
    }
  }
};
