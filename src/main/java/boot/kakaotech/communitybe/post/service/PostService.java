package boot.kakaotech.communitybe.post.service;

import boot.kakaotech.communitybe.common.scroll.dto.CursorPage;
import boot.kakaotech.communitybe.post.dto.CreatePostDto;
import boot.kakaotech.communitybe.post.dto.PostDetailWrapper;
import boot.kakaotech.communitybe.post.dto.PostListWrapper;
import boot.kakaotech.communitybe.post.dto.SavedPostDto;

public interface PostService {

    CursorPage<PostListWrapper> getPosts(int cursor, int size);

    PostDetailWrapper getPost(int postId);

    SavedPostDto savePost(CreatePostDto createPostDto);

    SavedPostDto updatePost(CreatePostDto createPostDto);

    void softDeletePost(int postId);

}
