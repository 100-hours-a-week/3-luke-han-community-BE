package boot.kakaotech.communitybe.post.service;

import boot.kakaotech.communitybe.common.scroll.dto.CursorPage;
import boot.kakaotech.communitybe.post.dto.PostListWrapper;

public interface PostService {

    CursorPage<PostListWrapper> getPosts(int cursor, int size);

}
