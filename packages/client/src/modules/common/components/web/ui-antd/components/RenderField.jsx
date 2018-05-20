import React from 'react';
import PropTypes from 'prop-types';
import { Form, Input } from 'antd';

const FormItem = Form.Item;
const { TextArea } = Input;

const RenderField = ({ input, options, meta: { touched, error } }) => {
  let validateStatus = '';
  if (touched && error) {
    validateStatus = 'error';
  }
  const InputComponent = input.type === 'textarea' ? TextArea : Input;

  return (
    <FormItem label={input.label || ''} validateStatus={validateStatus} help={touched && error} {...options}>
      <div>
        <InputComponent {...input} placeholder={input.placeholder || input.label || ''} />
      </div>
    </FormItem>
  );
};

RenderField.propTypes = {
  input: PropTypes.object,
  options: PropTypes.object,
  meta: PropTypes.object
};

export default RenderField;
