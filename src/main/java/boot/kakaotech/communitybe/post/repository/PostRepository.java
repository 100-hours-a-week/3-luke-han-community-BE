package boot.kakaotech.communitybe.post.repository;

import boot.kakaotech.communitybe.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Integer>, CustomPostRepository {

    Optional<Integer> findViewCountById(Integer postId);

}
