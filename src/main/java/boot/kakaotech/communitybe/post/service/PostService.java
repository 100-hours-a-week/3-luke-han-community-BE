package boot.kakaotech.communitybe.post.service;

import boot.kakaotech.communitybe.common.scroll.dto.CursorPage;
import boot.kakaotech.communitybe.post.dto.CreatePostDto;
import boot.kakaotech.communitybe.post.dto.SavedPostDto;
import boot.kakaotech.communitybe.post.dto.PostDetailWrapper;
import boot.kakaotech.communitybe.post.dto.PostListWrapper;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.List;

public interface PostService {

    CursorPage<PostListWrapper> getPosts(int cursor, int size);

    PostDetailWrapper getPost(int postId);

    SavedPostDto savePost(CreatePostDto createPostDto, List<String> images) throws UserPrincipalNotFoundException;

    SavedPostDto updatePost(CreatePostDto createPostDto, List<String> images) throws UserPrincipalNotFoundException;

    void softDeletePost(int postId) throws UserPrincipalNotFoundException;

}
