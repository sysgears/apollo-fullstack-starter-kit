import React from 'react';
import PropTypes from 'prop-types';
import { withFormik } from 'formik';
import { View, StyleSheet } from 'react-native';
import Field from '../../../utils/FieldAdapter';
import { RenderField, Button, RenderSelect, RenderSwitch } from '../../common/components/native';
import { email, minLength, required, match, validateForm } from '../../../../../common/validation';

import settings from '../../../../../../settings';
import translate from '../../../i18n';

const userFormSchema = {
  username: [required, minLength(3)],
  email: [required, email],
  password: [required, minLength(8)],
  passwordConfirmation: [match('password'), required, minLength(8)]
};

const handleRoleChange = (type, value, setFieldValue) => {
  const preparedValue = Array.isArray(value) ? value[0] : value;
  setFieldValue(type, preparedValue);
};

const validate = values => validateForm(values, userFormSchema);

const UserForm = ({ values, handleSubmit, setFieldValue, t }) => {
  const { username, email, role, isActive, profile, auth, password, passwordConfirmation } = values;
  return (
    <View style={styles.formContainer}>
      <Field
        placeholder={t('userEdit.form.field.name')}
        name="username"
        component={RenderField}
        type="text"
        value={username}
      />
      <Field
        name="email"
        component={RenderField}
        placeholder={t('userEdit.form.field.email')}
        value={email}
        keyboardType="email-address"
      />
      <Field
        name="isActive"
        label={t('userEdit.form.field.name')}
        onValueChange={() => setFieldValue('isActive', !isActive)}
        component={RenderSwitch}
        placeholder={t('userEdit.form.field.active')}
        checked={isActive}
      />
      <Field
        name="role"
        component={RenderSelect}
        placeholder={t('userEdit.form.field.role.label')}
        selectedValue={role}
        onValueChange={value => handleRoleChange('role', value, setFieldValue)}
        cols={1}
        data={[{ value: 'user', label: 'user' }, { value: 'admin', label: 'admin' }]}
      />
      <Field
        name="firstName"
        component={RenderField}
        placeholder={t('userEdit.form.field.firstName')}
        value={profile.firstName}
        onChange={value => setFieldValue('profile', { ...profile, firstName: value })}
      />
      <Field
        name="lastName"
        component={RenderField}
        placeholder={t('userEdit.form.field.lastName')}
        value={profile.lastName}
        onChange={value => setFieldValue('profile', { ...profile, lastName: value })}
      />
      {settings.user.auth.certificate.enabled && (
        <Field
          name="serial"
          component={RenderField}
          placeholder={t('userEdit.form.field.serial')}
          value={auth && auth.certificate && auth.certificate.serial}
          onChange={value => setFieldValue('auth', { ...auth, certificate: { ...auth.certificate, serial: value } })}
        />
      )}
      <Field
        name="password"
        secureTextEntry={true}
        component={RenderField}
        type="password"
        placeholder={t('userEdit.form.field.pass')}
        value={password}
      />
      <Field
        name="passwordConfirmation"
        component={RenderField}
        placeholder="Password Confirmation"
        value={passwordConfirmation}
        type={t('userEdit.form.field.passConf')}
        secureTextEntry={true}
      />
      <View style={styles.submit}>
        <Button type="primary" onPress={handleSubmit}>
          {t('userEdit.form.btnSubmit')}
        </Button>
      </View>
    </View>
  );
};

UserForm.propTypes = {
  handleSubmit: PropTypes.func,
  t: PropTypes.func,
  handleChange: PropTypes.func,
  setFieldValue: PropTypes.func,
  onSubmit: PropTypes.func,
  setTouched: PropTypes.func,
  isValid: PropTypes.bool,
  error: PropTypes.string,
  values: PropTypes.object,
  errors: PropTypes.object,
  initialValues: PropTypes.object.isRequired,
  touched: PropTypes.object
};

const UserFormWithFormik = withFormik({
  mapPropsToValues: values => {
    const { username, email, role, isActive, profile } = values.initialValues;
    return {
      username: username,
      email: email,
      role: role || 'user',
      isActive: isActive,
      password: '',
      passwordConfirmation: '',
      profile: {
        firstName: profile && profile.firstName,
        lastName: profile && profile.lastName
      },
      auth: {
        ...values.initialValues.auth
      }
    };
  },
  async handleSubmit(values, { setErrors, props: { onSubmit } }) {
    await onSubmit(values).catch(e => setErrors(e));
  },
  displayName: 'SignUpForm ', // helps with React DevTools
  validate: values => validate(values)
});

const styles = StyleSheet.create({
  formContainer: {
    paddingHorizontal: 20,
    justifyContent: 'center'
  },
  submit: {
    paddingTop: 30,
    paddingBottom: 15
  }
});

export default translate('user')(UserFormWithFormik(UserForm));
