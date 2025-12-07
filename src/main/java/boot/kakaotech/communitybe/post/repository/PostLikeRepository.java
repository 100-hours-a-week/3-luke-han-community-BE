package boot.kakaotech.communitybe.post.repository;

import boot.kakaotech.communitybe.post.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Integer> {

    boolean existsByPostIdAndUserId(Integer postId, Integer userId);

    Optional<PostLike> findByPostIdAndUserId(Integer postId, Integer userId);

}
