import React from 'react';
import PropTypes from 'prop-types';
import { View, FlatList, StyleSheet, Text } from 'react-native';
import { Pagination } from '../../common/components/native';

import translate from '../../../i18n';

const PaginationDemoView = ({ data, handlePageChange, renderItem, pagination, t }) => {
  return (
    <View>
      <Text style={styles.title}>{t('list.column.title')}</Text>
      <FlatList
        data={data.edges}
        style={styles.list}
        keyExtractor={item => `${item.node.id}`}
        renderItem={renderItem}
      />
      <Pagination
        totalPages={Math.ceil(data.totalCount / data.limit)}
        handlePageChange={handlePageChange}
        pagination={pagination}
        loadMoreText={t('list.btn.more')}
        hasNextPage={data.pageInfo.hasNextPage}
      />
    </View>
  );
};

PaginationDemoView.propTypes = {
  t: PropTypes.func,
  data: PropTypes.object,
  handlePageChange: PropTypes.func,
  renderItem: PropTypes.func,
  pagination: PropTypes.string
};

const styles = StyleSheet.create({
  list: {
    marginTop: 5
  },
  title: {
    marginTop: 5,
    paddingTop: 10,
    paddingBottom: 10,
    fontSize: 16,
    paddingLeft: 7,
    borderBottomColor: '#000',
    borderBottomWidth: 0.3
  }
});

export default translate('pagination')(PaginationDemoView);
