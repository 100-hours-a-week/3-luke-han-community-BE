package boot.kakaotech.communitybe.post.service;

import boot.kakaotech.communitybe.common.scroll.dto.CursorPage;
import boot.kakaotech.communitybe.post.dto.CreatePostDto;
import boot.kakaotech.communitybe.post.dto.SavedPostDto;
import boot.kakaotech.communitybe.post.dto.PostDetailWrapper;
import boot.kakaotech.communitybe.post.dto.PostListWrapper;
import jakarta.servlet.http.HttpServletRequest;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.List;

public interface PostService {

    CursorPage<PostListWrapper> getPosts(int cursor, int size);

    PostDetailWrapper getPost(HttpServletRequest request, int postId) throws UserPrincipalNotFoundException;

    SavedPostDto savePost(HttpServletRequest request, CreatePostDto createPostDto) throws UserPrincipalNotFoundException;

    SavedPostDto updatePost(HttpServletRequest request, CreatePostDto createPostDto) throws UserPrincipalNotFoundException;

    void softDeletePost(HttpServletRequest request, int postId) throws UserPrincipalNotFoundException;

    void addPostLike(HttpServletRequest request, int postId) throws UserPrincipalNotFoundException;

    void deletePostLike(HttpServletRequest request, int postId) throws UserPrincipalNotFoundException;

}
