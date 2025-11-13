package boot.kakaotech.communitybe.post.controller;

import boot.kakaotech.communitybe.common.CommonResponseDto;
import boot.kakaotech.communitybe.common.CommonResponseMapper;
import boot.kakaotech.communitybe.common.scroll.dto.CursorPage;
import boot.kakaotech.communitybe.post.dto.CreatePostDto;
import boot.kakaotech.communitybe.post.dto.PostDetailWrapper;
import boot.kakaotech.communitybe.post.dto.PostListWrapper;
import boot.kakaotech.communitybe.post.dto.SavedPostDto;
import boot.kakaotech.communitybe.post.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    private final CommonResponseMapper responseMapper;

    /**
     * 게시글 목록 조회 API
     *
     * @param cursor
     * @param size
     * @return
     */
    @GetMapping
    public ResponseEntity<CommonResponseDto<CursorPage<PostListWrapper>>> getPosts(
            @RequestParam Integer cursor,
            @RequestParam Integer size
    ) {
        log.info("[PostController] 게시글 목록 조회 시작");

        CursorPage<PostListWrapper> posts = postService.getPosts(cursor, size);
        CommonResponseDto<CursorPage<PostListWrapper>> response = responseMapper.createResponse(
                posts,
                "게시글 목록 조회 성공"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 상세 조회 API
     *
     * @param postId
     * @return
     */
    @GetMapping("/{postId}")
    public ResponseEntity<CommonResponseDto<PostDetailWrapper>> getPost(
            @PathVariable Integer postId
    )  {
        log.info("[PostController] 게시글 상세 조회 시작 - postId: {}", postId);

        PostDetailWrapper post = postService.getPost(postId);
        CommonResponseDto<PostDetailWrapper> response = responseMapper.createResponse(
                post,
                "게시글 상세 조회 성공"
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CommonResponseDto<SavedPostDto>> post(
            @RequestBody CreatePostDto createPostDto
    ) {
        log.info("[PostController] 게시글 생성 시작");

        SavedPostDto res = postService.savePost(createPostDto);
        CommonResponseDto<SavedPostDto> response = responseMapper.createResponse(
                res,
                "게시글 생성 성공"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
