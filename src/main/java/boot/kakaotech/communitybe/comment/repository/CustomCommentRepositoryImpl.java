package boot.kakaotech.communitybe.comment.repository;

import boot.kakaotech.communitybe.comment.dto.CommentDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static boot.kakaotech.communitybe.comment.entity.QComment.comment;
import static boot.kakaotech.communitybe.post.entity.QPost.post;
import static boot.kakaotech.communitybe.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class CustomCommentRepositoryImpl implements CustomCommentRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<CommentDto> getComments(Integer postId, Integer parentId, Pageable pageable) {
        List<CommentDto> comments = jpaQueryFactory
                .select(Projections.constructor(CommentDto.class,
                        comment.id,
                        user.id.as("userId"),
                        user.nickname.as("name"),
                        user.profileImageUrl,
                        comment.content.as("comment"),
                        comment.parentComment.id.as("parentId"),
                        comment.depth,
                        comment.createdAt))
                .from(comment)
                .join(user).on(comment.user.id.eq(user.id))
                .join(post).on(comment.post.eq(post))
                .where(comment.post.id.eq(postId),
                        comment.parentComment.id.eq(parentId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        return comments;
    }

}
