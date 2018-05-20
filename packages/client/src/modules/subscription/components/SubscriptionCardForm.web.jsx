import React from 'react';
import PropTypes from 'prop-types';
import { CardElement, injectStripe } from 'react-stripe-elements';
//eslint-disable-next-line import/no-extraneous-dependencies
import { Schema } from '@domain-schema/core';
//eslint-disable-next-line import/no-extraneous-dependencies
import { DomainSchemaFormik } from '@domain-schema/formik';

import translate from '../../../i18n';
import { Label } from '../../common/components/web';

const subscriptionFormSchema = t =>
  class extends Schema {
    __ = { name: 'SubscriptionForm' };
    name = {
      type: String,
      input: {
        label: t('card.name')
      }
    };
  };

class SubscriptionCardForm extends React.Component {
  static propTypes = {
    action: PropTypes.string.isRequired,
    onSubmit: PropTypes.func,
    t: PropTypes.func,
    stripe: PropTypes.object
  };

  render() {
    const { t, action } = this.props;
    const subscriptionForm = new DomainSchemaFormik(subscriptionFormSchema(t));
    const SubscriptionFormComponent = subscriptionForm.generateForm({
      label: action,
      color: 'primary',
      style: { marginTop: 15 }
    });

    return (
      <React.Fragment>
        <Label>{this.props.t('card.info')}</Label>
        <CardElement className="form-control" style={{ base: { lineHeight: '30px' } }} />
        <SubscriptionFormComponent
          onSubmit={async ({ name }) => {
            const { stripe, onSubmit } = this.props;
            const { token, error } = await stripe.createToken({ name });
            if (error) return;

            const {
              id,
              card: { exp_month, exp_year, last4, brand }
            } = token;

            await onSubmit({
              token: id,
              expiryMonth: exp_month,
              expiryYear: exp_year,
              last4,
              brand
            });
          }}
        />
      </React.Fragment>
    );
  }
}

export default translate('subscription')(injectStripe(SubscriptionCardForm));
