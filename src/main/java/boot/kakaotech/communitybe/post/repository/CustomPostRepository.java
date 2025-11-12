package boot.kakaotech.communitybe.post.repository;

import boot.kakaotech.communitybe.post.dto.PostDetailWrapper;
import boot.kakaotech.communitybe.post.dto.PostListWrapper;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomPostRepository {

    List<PostListWrapper> getPostsUsingFetch(Pageable pageable);

    PostDetailWrapper getPostById(int postId, int userId);

    List<String> getImages(int postId);

}
