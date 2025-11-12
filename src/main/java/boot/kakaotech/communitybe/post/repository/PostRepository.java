package boot.kakaotech.communitybe.post.repository;

import boot.kakaotech.communitybe.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Integer>, CustomPostRepository {
}
