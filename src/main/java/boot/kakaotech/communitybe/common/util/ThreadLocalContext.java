package boot.kakaotech.communitybe.common.util;

import boot.kakaotech.communitybe.common.exception.BusinessException;
import boot.kakaotech.communitybe.common.exception.ErrorCode;
import boot.kakaotech.communitybe.user.entity.User;
import org.springframework.stereotype.Component;

import java.nio.file.attribute.UserPrincipalNotFoundException;

@Component
public class ThreadLocalContext {

    private static final ThreadLocal<User> threadLocal = new ThreadLocal<>();

    /**
     * ThreadLocal 내 유저 세팅하는 메서드
     *
     * @param user
     */
    public void set(User user) {
        threadLocal.set(user);
    }

    /**
     * ThreadLocal 초기화하는 메서드
     */
    public void clear() {
        threadLocal.remove();
    }

    /**
     * 현재 요청을 보낸 유저의 id를 반환하는 메서드
     * 1. ThreadLocal에 저장된 User 조회
     * 2. 인증정보가 저장이 안 되어있으면 throw error
     *
     * @return
     */
    public Integer getCurrentUserId() {
        User user = threadLocal.get();
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_REQUEST);
        }

        return user.getId();
    }

    /**
     * 현재 요청을 보낸 유저 객체를 반환하는 메서드
     * 1. ThreadLocal에 저장된 User 조회
     * 2. 인증정보가 저장이 안 되어있으면 throw error
     *
     * @return
     */
    public User getCurrentUser() {
        User user = threadLocal.get();
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_REQUEST);
        }

        return user;
    }

}