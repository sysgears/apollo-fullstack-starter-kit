import React from 'react';
import { connect } from 'react-redux';
import { graphql, compose } from 'react-apollo';
import { Link } from 'react-router-dom';

import PostForm from '../components/post_form';
import PostComments from './post_comments';
import POST_QUERY from '../graphql/post_get.graphql';
import POST_EDIT from '../graphql/post_edit.graphql';
import POST_SUBSCRIPTION from '../graphql/post_subscription.graphql';

class PostEdit extends React.Component {
  constructor(props) {
    super(props);

    this.subscription = null;
  }

  componentWillReceiveProps(nextProps) {
    const { post, loading, subscribeToMore } = this.props;

    // Check if props have changed and, if necessary, stop the subscription
    if (this.subscription && post.id !== nextProps.post.id) {
      this.subscription = null;
    }

    // Subscribe or re-subscribe
    if (!this.subscription && !loading) {
      this.subscription = subscribeToMore({
        document: POST_SUBSCRIPTION,
        variables: { id: post.id },
        updateQuery: (prev) => {
          return prev;
        },
        onError: (err) => console.error(err),
      });
    }
  }

  onSubmit(values) {
    const { post, editPost } = this.props;

    editPost(post.id, values.title, values.content);
  }

  render() {
    const { loading, post, match, subscribeToMore } = this.props;

    if (loading) {
      return (
        <div>{ /* loading... */ }</div>
      );
    } else {
      return (
        <div>
          <Link to="/posts">Back</Link>
          <h2>Edit Post</h2>
          <PostForm onSubmit={this.onSubmit.bind(this)} initialValues={post}/>
          <br/>
          <PostComments postId={match.params.id} comments={post.comments} subscribeToMore={subscribeToMore}/>
        </div>
      );
    }
  }
}

PostEdit.propTypes = {
  loading: React.PropTypes.bool.isRequired,
  post: React.PropTypes.object,
  editPost: React.PropTypes.func.isRequired,
  match: React.PropTypes.object.isRequired,
  subscribeToMore: React.PropTypes.func.isRequired,
  endCursor: React.PropTypes.string.isRequired,
};

const PostEditWithApollo = compose(
  graphql(POST_QUERY, {
    options: (props) => {
      return {
        variables: { id: props.match.params.id }
      };
    },
    props({ data: { loading, post, subscribeToMore } }) {
      return { loading, post, subscribeToMore };
    }
  }),
  graphql(POST_EDIT, {
    props: ({ ownProps: { endCursor, history }, mutate }) => ({
      editPost: (id, title, content) => mutate({
        variables: { input: { id, title, content, endCursor } }
      }).then(() => history.push('/posts')),
    })
  })
)(PostEdit);

export default connect(
  (state) => ({ endCursor: state.post.endCursor })
)(PostEditWithApollo);