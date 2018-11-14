import i18n from '@module/i18n-client-react';
import counter from '@module/counter-client-react';
import validation from '@module/validation-common-react';
import ClientModule from '@module/module-client-react';

import defaultRouter from './defaultRouter';
import post from './post';
import upload from './upload';
import user from './user';
import payments from './payments';
import contact from './contact';
import pageNotFound from './pageNotFound';
import pagination from './pagination';
import chat from './chat';
import './favicon';

const modules = new ClientModule(
  validation,
  defaultRouter,
  counter,
  post,
  upload,
  contact,
  pagination,
  chat,
  payments,
  user,
  i18n,
  pageNotFound
);
modules.triggerOnCreate();

export default modules;
