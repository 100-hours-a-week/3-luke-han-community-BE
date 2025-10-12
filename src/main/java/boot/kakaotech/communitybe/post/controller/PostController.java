package boot.kakaotech.communitybe.post.controller;

import boot.kakaotech.communitybe.common.scroll.dto.CursorPage;
import boot.kakaotech.communitybe.post.dto.CreatePostDto;
import boot.kakaotech.communitybe.post.dto.PostDetailWrapper;
import boot.kakaotech.communitybe.post.dto.PostListWrapper;
import boot.kakaotech.communitybe.post.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<CursorPage<PostListWrapper>> getPosts(@RequestParam Integer cursor, @RequestParam Integer size) {
        log.info("[PostController] 게시글목록 조회 시작");

        CursorPage<PostListWrapper> posts = postService.getPosts(cursor, size);
        log.info("[PostController] 게시글목록 조회 성공");

        return posts != null ? ResponseEntity.ok(posts) : ResponseEntity.noContent().build();
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailWrapper> getPost(@PathVariable("postId") Integer postId) {
        log.info("[PostController] 게시글 상세조회 시작");

        PostDetailWrapper post = postService.getPost(postId);
        log.info("[PostController] 게시글 상세조회 성공");

        return post != null ? ResponseEntity.ok(post) : ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<Integer> savePost(@RequestBody CreatePostDto createPostDto, @RequestBody List<String> images) throws UserPrincipalNotFoundException {
        log.info("[PostController] 게시글 생성 시작");

        Integer postId = postService.savePost(createPostDto, images);
        log.info("[PostController] 게시글 생성 성공");

        return ResponseEntity.status(HttpStatus.CREATED).body(postId);
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<Void> updatePost(
            @PathVariable("postId") Integer postId,
            @RequestBody CreatePostDto createPostDto,
            @RequestBody List<String> images) throws UserPrincipalNotFoundException {
        log.info("[PostController] 게시글 수정 시작");

        postService.updatePost(createPostDto, images);
        log.info("[PostController] 게시글 수정 성공");

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{postId}/status")
    public ResponseEntity<Void> updatePostStatus(@PathVariable("postId") Integer postId) throws UserPrincipalNotFoundException {
        log.info("[PostController] 게시글 삭제 시작");

        postService.softDeletePost(postId);
        log.info("[PostController] 게시글 삭제 성공");

        return ResponseEntity.ok().build();
    }

}
