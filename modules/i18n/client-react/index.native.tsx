import React from 'react';
import { default as i18next } from 'i18next';
import { I18nextProvider } from 'react-i18next';
import ClientModule from '@module/module-client-react-native';
import commonI18n from '@module/i18n-common-react';

import { LanguagePicker, Root } from '../../../packages/client/src/modules/common/components/native';
import resources from './locales';
import settings from '../../../settings';

// export * from './translate';
// tslint:disable-next-line
const reactI18next = settings.i18n.enabled ? require('react-i18next') : null;

export const translate = (ns: any) => {
  return (Component: any) => {
    if (settings.i18n.enabled) {
      return reactI18next.translate(ns)(Component);
    } else {
      // eslint-disable-next-line react/display-name
      return (props: any) => React.createElement(Component, { ...props, t: (key: any) => key });
    }
  };
};

export type TranslateFunction = (msg: string, ...params: any[]) => string;

const I18nProvider = ({ i18n, children }: any) => {
  return (
    <Root>
      <I18nextProvider i18n={i18n}>{children}</I18nextProvider>
    </Root>
  );
};

const langPicker =
  settings.i18n.enabled && settings.i18n.langPickerRender
    ? new ClientModule({
        drawerItem: [
          {
            LangPicker: {
              screen: (): any => null,
              navigationOptions: {
                drawerLabel: <LanguagePicker key={'picker'} i18n={i18next} />
              },
              skip: true
            }
          }
        ]
      })
    : undefined;

export default (settings.i18n.enabled
  ? new ClientModule(commonI18n, langPicker, {
      localization: [{ ns: 'i18n', resources }],
      // eslint-disable-next-line react/display-name
      rootComponentFactory: [() => <I18nProvider i18n={i18next} />]
    })
  : undefined);
