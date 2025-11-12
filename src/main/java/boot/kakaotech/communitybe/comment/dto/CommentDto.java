package boot.kakaotech.communitybe.comment.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {

    private Integer id;

    private Integer userId;

    private String name;

    private String profileImageUrl;

    private String comment;

    private Integer parentId;

    private int depth;

    private LocalDateTime createdAt;

}
