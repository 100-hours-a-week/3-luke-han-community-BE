package boot.kakaotech.communitybe.comment.repository;

import boot.kakaotech.communitybe.comment.dto.CommentDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomCommentRepository {

    List<CommentDto> getComments(Integer postId, Integer parentId, Pageable pageable);

}
