package boot.kakaotech.communitybe.common.scheduler;

import boot.kakaotech.communitybe.post.entity.Post;
import boot.kakaotech.communitybe.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ViewCountScheduler {

    private final PostRepository postRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String VIEW_COUNT_PREFIX = "post_view:";

    @Scheduled(fixedRate = 300000)
    public void syncViewCount() {
        Set<Integer> postIds = redisTemplate.opsForZSet().range(VIEW_COUNT_PREFIX, 0, -1)
                .stream()
                .map(id -> Integer.parseInt(id))
                .collect(Collectors.toSet());

        if (postIds.isEmpty() || postIds == null) {
            return;
        }

        for (int id : postIds) {
            Double score = redisTemplate.opsForZSet().score(VIEW_COUNT_PREFIX, id);

            if (score == null) {
                Post post = postRepository.findById(id).orElse(null);
                if (post == null) {
                    post.setViewCount(score.intValue());
                    postRepository.save(post);
                }
            }

            redisTemplate.opsForZSet().remove(VIEW_COUNT_PREFIX, id);
        }
    }

}
