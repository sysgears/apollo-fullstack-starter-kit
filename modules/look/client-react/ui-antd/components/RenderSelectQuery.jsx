import React from 'react';
import PropTypes from 'prop-types';
import { Select, Spin } from 'antd';
import { pascalize } from 'humps';

import { FormItem } from './index';
import schemaQueries from '../../generatedContainers';

const Option = Select.Option;
const LIMIT = 20;

export default class RenderSelectQuery extends React.Component {
  static propTypes = {
    input: PropTypes.object,
    setFieldValue: PropTypes.func.isRequired,
    setFieldTouched: PropTypes.func.isRequired,
    label: PropTypes.string,
    formItemLayout: PropTypes.object,
    meta: PropTypes.object,
    schema: PropTypes.object,
    style: PropTypes.object,
    formType: PropTypes.string.isRequired,
    optional: PropTypes.bool
  };

  state = {
    searchText: '',
    dirty: false
  };

  handleChange = edges => value => {
    const {
      input: { name },
      setFieldValue
    } = this.props;

    const key = value && value.key ? parseInt(value.key) : '';

    setFieldValue(name, edges.find(item => item.id === key) || '');
  };

  handleBlur = () => {
    const {
      input: { name },
      setFieldTouched
    } = this.props;
    setFieldTouched(name, true);
  };

  search = value => {
    const { dirty } = this.state;
    if ((value && value.length >= 1) || dirty) {
      this.setState({ searchText: value, dirty: true });
    }
  };

  render() {
    const {
      input: { value },
      schema,
      style = { width: '80%' },
      formItemLayout,
      meta: { touched, error },
      formType,
      label,
      optional
    } = this.props;
    const { searchText, dirty } = this.state;
    const column = schema.keys().find(key => !!schema.values[key].sortBy) || 'name';
    const orderBy = () => {
      const foundOrderBy = schema.keys().find(key => !!schema.values[key].orderBy);
      return foundOrderBy ? { column: foundOrderBy } : null;
    };
    const toString = schema.__.__toString ? schema.__.__toString : opt => opt[column];
    const formattedValue = value ? { key: `${value.id}`, label: toString(value) } : { key: '0', label: '' };
    const Query = schemaQueries[`${pascalize(schema.name)}Query`];

    return (
      <FormItem label={label} {...formItemLayout} validateStatus={touched && error ? 'error' : ''} help={error}>
        <div>
          <Query limit={LIMIT} filter={{ searchText }} orderBy={orderBy()}>
            {({ loading, data }) => {
              if (!loading || data) {
                const {
                  edges,
                  pageInfo: { totalCount }
                } = data;
                const isEdgesNotIncludeValue = value && edges && !edges.find(({ id }) => id === value.id);
                const renderOptions = () => {
                  const defaultValue = `Select ${pascalize(schema.name)}`;
                  const defaultOption =
                    parseInt(formattedValue.key) === 0
                      ? [
                          <Option key="0" value="0">
                            {defaultValue}
                          </Option>
                        ]
                      : [];
                  return edges
                    ? edges.reduce((acc, opt) => {
                        acc.push(
                          <Option key={opt.id} value={`${opt.id}`}>
                            {toString(opt)}
                          </Option>
                        );
                        return acc;
                      }, defaultOption)
                    : defaultOption;
                };

                const getSearchProps = () => {
                  return {
                    filterOption: false,
                    value: isEdgesNotIncludeValue && dirty ? { key: `${edges[0].id}` } : formattedValue
                  };
                };

                const getChildrenProps = () => {
                  return {
                    optionFilterProp: 'children',
                    filterOption: (input, { props: { children } }) =>
                      children.toLowerCase().includes(input.toLowerCase()),
                    value: formattedValue
                  };
                };

                const basicProps = {
                  allowClear: formType !== 'form' ? true : optional,
                  showSearch: true,
                  labelInValue: true,
                  dropdownMatchSelectWidth: false,
                  style,
                  onSearch: this.search,
                  onChange: this.handleChange(edges || null),
                  onBlur: this.handleBlur
                };
                const filterProps = totalCount > LIMIT ? getSearchProps() : getChildrenProps();
                const props = { ...basicProps, ...filterProps };
                return <Select {...props}>{renderOptions()}</Select>;
              } else {
                return <Spin size="small" />;
              }
            }}
          </Query>
        </div>
      </FormItem>
    );
  }
}
