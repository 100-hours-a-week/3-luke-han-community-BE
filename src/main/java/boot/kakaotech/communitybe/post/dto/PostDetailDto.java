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

    private long likeCount;

    private long commentCount;

    private int viewCount;

    private boolean liked;

    private LocalDateTime createdAt;

}
