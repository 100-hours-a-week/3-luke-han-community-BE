package boot.kakaotech.communitybe.comment.repository;

import boot.kakaotech.communitybe.comment.dto.CommentDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
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
        BooleanExpression parentFilter;
        if (parentId == null || parentId == 0) {
            // 최상위 댓글: parentComment IS NULL
            parentFilter = comment.parentComment.isNull();
        } else {
            // 대댓글: parentComment.id = parentId
            parentFilter = comment.parentComment.id.eq(parentId);
        }

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
                .join(comment.user, user)
                .join(comment.post, post)
                .leftJoin(comment.parentComment)
                .where(
                        comment.post.id.eq(postId),
                        parentFilter
                )
                .orderBy(comment.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        return comments;
    }

}
