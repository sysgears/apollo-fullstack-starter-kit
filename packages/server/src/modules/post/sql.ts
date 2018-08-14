import { returnId, orderedFor } from '../../sql/helpers';
import knex from '../../sql/connector';

export interface Post {
  title: string;
  content: string;
}

export interface Comment {
  postId: number;
  content: string;
}

export interface Identifier {
  id: number;
}

interface PostDAO {
  postsPagination(limit: number, after: number): Promise<Post & Identifier[]>;
  getCommentsForPostIds(postIds: number[]): Promise<Comment[]>;
  getTotal(): Promise<number>;
  post(id: number): Promise<Post & Identifier>;
  addPost(inputPost: Post): Promise<number>;
  deletePost(id: number): Promise<number>;
  editPost(post: Post): Promise<number>;
  addComment(inputComment: Comment): Promise<number>;
  getComment(id: number): Promise<Comment & Identifier>;
  deleteComment(id: number): Promise<number>;
  editComment(editedComment: Comment & Identifier): Promise<number>;
}

export default class PostDAOImpl implements PostDAO {
  public postsPagination(limit: number, after: number) {
    return knex
      .select('id', 'title', 'content')
      .from('post')
      .orderBy('id', 'desc')
      .limit(limit)
      .offset(after);
  }

  public async getCommentsForPostIds(postIds: number[]) {
    const res = await knex
      .select('id', 'content', 'post_id AS postId')
      .from('comment')
      .whereIn('post_id', postIds);

    return orderedFor(res, postIds, 'postId', false);
  }

  public getTotal() {
    return knex('post')
      .countDistinct('id as count')
      .first();
  }

  public post(id: number) {
    return knex
      .select('id', 'title', 'content')
      .from('post')
      .where('id', '=', id)
      .first();
  }

  public addPost(params: Post) {
    return returnId(knex('post')).insert(params);
  }

  public deletePost(id: number) {
    return knex('post')
      .where('id', '=', id)
      .del();
  }

  public editPost(params: Post & Identifier) {
    const { id, title, content } = params;
    return knex('post')
      .where('id', '=', id)
      .update({ title, content });
  }

  public addComment(params: Comment) {
    const { content, postId } = params;
    return returnId(knex('comment')).insert({ content, post_id: postId });
  }

  public getComment(id: number) {
    return knex
      .select('id', 'content')
      .from('comment')
      .where('id', '=', id)
      .first();
  }

  public deleteComment(id: number) {
    return knex('comment')
      .where('id', '=', id)
      .del();
  }

  public editComment(params: Comment & Identifier) {
    const { id, content } = params;
    return knex('comment')
      .where('id', '=', id)
      .update({
        content
      });
  }
}
