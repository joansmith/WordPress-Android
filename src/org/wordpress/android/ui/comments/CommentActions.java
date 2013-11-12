package org.wordpress.android.ui.comments;

import android.os.Handler;
import android.text.TextUtils;

import org.wordpress.android.WordPress;
import org.wordpress.android.models.Blog;
import org.wordpress.android.models.Comment;
import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nbradbury on 11/8/13.
 * actions related to comments - replies, etc.
 * methods below do network calls in the background & update local DB upon success
 * all methods below MUST be called from UI thread
 */
public class CommentActions {

    private CommentActions() {
        throw new AssertionError();
    }

    /*
     * result when a comment action is performed
     */
    protected interface CommentActionListener {
        public void onActionResult(boolean succeeded);
    }

    /***
     * reply to an individual comment
     ***/
    protected static void submitReply(final Blog blog,
                                      final Comment comment,
                                      final String replyText,
                                      final CommentActionListener listener) {

        if (blog==null || comment==null || TextUtils.isEmpty(replyText)) {
            if (listener != null)
                listener.onActionResult(false);
            return;
        }

        final Handler handler = new Handler();

        new Thread() {
            @Override
            public void run() {
                XMLRPCClient client = new XMLRPCClient(blog.getUrl(),
                        blog.getHttpuser(),
                        blog.getHttppassword());

                Map<String, Object> replyHash = new HashMap<String, Object>();
                replyHash.put("comment_parent", comment.commentID);
                replyHash.put("content", replyText);
                replyHash.put("author", "");
                replyHash.put("author_url", "");
                replyHash.put("author_email", "");

                Object[] params = { blog.getBlogId(),
                        blog.getUsername(),
                        blog.getPassword(),
                        Integer.valueOf(comment.postID),
                        replyHash };


                int newCommentID;
                try {
                    newCommentID = (Integer) client.call("wp.newComment", params);
                } catch (XMLRPCException e) {
                    newCommentID = -1;
                }

                final boolean succeeded = (newCommentID >= 0);
                if (succeeded)
                    WordPress.wpDB.updateLatestCommentID(blog.getId(), newCommentID);

                if (listener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onActionResult(succeeded);
                        }
                    });
                }
            }
        }.start();
    }

    /***
     * change the status of a comment
     ***/
    protected static void setCommentStatus(final Blog blog,
                                           final Comment comment,
                                           final Comment.CommentStatus status,
                                           final CommentActionListener listener) {

        if (blog==null || comment==null || status==null || status== Comment.CommentStatus.UNKNOWN) {
            if (listener != null)
                listener.onActionResult(false);
            return;
        }

        final Handler handler = new Handler();

        new Thread() {
            @Override
            public void run() {
                XMLRPCClient client = new XMLRPCClient(blog.getUrl(),
                    blog.getHttpuser(),
                    blog.getHttppassword());

                Map<String, String> postHash = new HashMap<String, String>();
                postHash.put("status", status.toString());
                postHash.put("content", comment.comment);
                postHash.put("author", comment.name);
                postHash.put("author_url", comment.authorURL);
                postHash.put("author_email", comment.authorEmail);

                Object[] params = { blog.getBlogId(),
                        blog.getUsername(),
                        blog.getPassword(),
                        comment.commentID,
                        postHash};

                Object result;
                try {
                    result = client.call("wp.editComment", params);
                } catch (final XMLRPCException e) {
                    result = null;
                }

                final boolean success = (result != null && Boolean.parseBoolean(result.toString()));
                if (success)
                    WordPress.wpDB.updateCommentStatus(blog.getId(), comment.commentID, status.toString());

                if (listener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onActionResult(success);
                        }
                    });
                }
            }
        }.start();
    }
}