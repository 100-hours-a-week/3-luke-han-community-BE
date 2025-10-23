package boot.kakaotech.communitybe.post.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostDto {

    private Integer id;

    private String title;

    private String content;

    private List<String> images;

    private Boolean isImageChanged;

}
