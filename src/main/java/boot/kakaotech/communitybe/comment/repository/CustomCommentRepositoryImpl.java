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
    public List<CommentDto> getComments(Integer postId, Integer parentId, Integer lastCommentId, int size) {
        BooleanExpression parentFilter;
        if (parentId == null || parentId == 0) {
            // 최상위 댓글: parentComment IS NULL
            parentFilter = comment.parentComment.isNull();
        } else {
            // 대댓글: parentComment.id = parentId
            parentFilter = comment.parentComment.id.eq(parentId);
        }

        // 커서(id) 필터: 첫 페이지(lastCommentId == null)이면 필터 없음
        BooleanExpression cursorFilter =
                (lastCommentId == null || lastCommentId == 0)
                        ? null
                        : comment.id.gt(lastCommentId);  // 이전 페이지 마지막 id보다 더 오래된(작은) 것들

        return jpaQueryFactory
                .select(Projections.constructor(CommentDto.class,
                        comment.id,
                        user.id.as("userId"),
                        user.nickname.as("name"),
                        user.profileImageKey,
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
                        parentFilter,
                        cursorFilter,
                        comment.deletedAt.isNull(),
                        post.deletedAt.isNull()
                )
                .orderBy(comment.id.asc())
                .limit(size + 1)   // size+1로 다음 페이지 여부 판단
                .fetch();
    }

}
