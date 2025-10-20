package boot.kakaotech.communitybe.post.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailDto {

    private Integer id;

    private String title;

    private List<String> images;

    private String content;

    private int likeCount;

    private int commentCount;

    private int viewCount;

    private LocalDateTime createdAt;

}
