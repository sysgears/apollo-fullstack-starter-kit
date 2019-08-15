import React from 'react';

import { translate, TranslateFunction } from '@gqlapp/i18n-client-react';
import { Button } from '@gqlapp/look-client-react';

interface ButtonProps {
  onClick: () => any;
  text: string;
  t: TranslateFunction;
}

const IncreaseButton = ({ onClick, text, t }: ButtonProps) => (
  <Button id="increase-button" color="primary" onClick={onClick}>
    {t(text)}
  </Button>
);

export default translate('serverCounter')(IncreaseButton);
