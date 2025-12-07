package boot.kakaotech.communitybe.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentThreadDto {

    private CommentDto parent;

    private List<CommentDto> children;

    private boolean hasMoreChildren;

    private Integer childNextCursor;

}
