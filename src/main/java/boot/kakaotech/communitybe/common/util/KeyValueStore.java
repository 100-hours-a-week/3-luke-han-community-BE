package boot.kakaotech.communitybe.common.util;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@AllArgsConstructor
public class KeyValueStore {

    private final Map<String, String> map = new ConcurrentHashMap<>();

    private final Map<String, String> tokenStore = new ConcurrentHashMap<>();

    public String get(String key) {
        return map.get(key);
    }

    public void put(String key, String value) {
        map.put(key, value);
    }

    public void remove(String key) {
        map.remove(key);
    }

    /**
     * 현재 map 내용을 복사해서 반환하고, 내부 map은 비우는 메서드
     * - 스케줄러에서 한 번에 동기화 후 초기화할 때 사용
     */
    public Map<String, String> snapShotAndClear() {
        synchronized (map) {
            Map<String, String> snapshot = new ConcurrentHashMap<>(map);
            map.clear();
            return snapshot;
        }
    }

    /**
     * 그냥 전체 조회 (비우지 않고)
     */
    public Map<String, String> snapShot() {
        return new ConcurrentHashMap<>(map);
    }

    private static final String RT_PREFIX = "RT:";

    public void saveRefreshToken(int userId, String refreshToken) {
        tokenStore.put(RT_PREFIX + userId, refreshToken);
    }

    public String getRefreshToken(int userId) {
        return tokenStore.get(RT_PREFIX + userId);
    }

    public void deleteRefreshToken(int userId) {
        tokenStore.remove(RT_PREFIX + userId);
    }

}
