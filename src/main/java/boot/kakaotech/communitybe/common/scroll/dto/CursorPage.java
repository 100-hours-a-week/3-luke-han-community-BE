package boot.kakaotech.communitybe.common.scroll.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorPage<T> {

    private List<T> list;

    private Integer nextCursor;

    private boolean hasNextCursor;

}
