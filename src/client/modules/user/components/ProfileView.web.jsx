// Web only component
// React
import React from 'react';
import PropTypes from 'prop-types';
import Helmet from 'react-helmet';
import { PageLayout } from '../../common/components/web';

const ProfileView = ({ loading, currentUser }) => {
  const renderMetaData = () => (
    <Helmet
      title="Profile"
      meta={[
        {
          name: 'description',
          content: 'Profile page'
        }
      ]}
    />
  );

  if (loading && !currentUser) {
    return (
      <PageLayout>
        {renderMetaData()}
        <div className="text-center">Loading...</div>
      </PageLayout>
    );
  } else if (currentUser) {
    return (
      <PageLayout>
        {renderMetaData()}
        <h2>Profile</h2>
        <p>username: {currentUser.username}</p>
        <p>email: {currentUser.email}</p>
        <p>role: {currentUser.role}</p>
      </PageLayout>
    );
  } else {
    return (
      <PageLayout>
        {renderMetaData()}
        <h2>No current user logged in</h2>
      </PageLayout>
    );
  }
};

ProfileView.propTypes = {
  loading: PropTypes.bool.isRequired,
  currentUser: PropTypes.object
};

export default ProfileView;
