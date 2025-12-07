package boot.kakaotech.communitybe.common.scheduler;

import boot.kakaotech.communitybe.common.properties.PrefixProperty;
import boot.kakaotech.communitybe.common.util.KeyValueStore;
import boot.kakaotech.communitybe.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ViewCountScheduler {

    private final KeyValueStore kvStore;

    private final PostRepository postRepository;

    private final PrefixProperty property;

    @Scheduled(fixedDelayString = "300000")
    @Transactional
    public void syncViewCount() {
        Map<String, String> snapshot = kvStore.snapShotAndClear();

        if (snapshot.isEmpty()) {
            return;
        }

        String viewPrefix = property.getViewCount();

        log.info("[ViewCountScheduler] 조회수 동기화 시작 - {}개", snapshot.size());

        int success = 0;
        int fail = 0;

        for (Map.Entry<String, String> entry : snapshot.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (!key.startsWith(viewPrefix)) {
                continue;
            }

            try {
                String idPart = key.substring(viewPrefix.length());
                int postId = Integer.parseInt(idPart);
                int viewCount = Integer.parseInt(value);

                // 기존 값에 증가값(delta) 더해서 저장 - 다중 인스턴스 고려
                int updated = postRepository.updateViewCountByPostId(postId, viewCount);
                if (updated > 0) {
                    success++;
                } else {
                    fail++;
                }
            } catch (Exception e) {
                fail++;
                log.warn("[ViewCountScheduler] 조회수 동기화 실패 key={}, value={} - {}", key, value, e.getMessage());
            }

            log.info("[ViewCountScheduler] 조회수 동기화 완 - success: {}, fail: {}", success, fail);
        }
    }

}
