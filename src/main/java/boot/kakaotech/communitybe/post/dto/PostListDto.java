package boot.kakaotech.communitybe.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostListDto {

    private Integer id;

    private String title;

    private long likeCount;

    private long commentCount;

    private int viewCount;

    private LocalDateTime createdAt;

}
