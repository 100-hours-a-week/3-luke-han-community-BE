package boot.kakaotech.communitybe.util;

import boot.kakaotech.communitybe.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.attribute.UserPrincipalNotFoundException;

@Component
@RequiredArgsConstructor
public class UserUtil {

    private static final ThreadLocal<User> threadLocal = new ThreadLocal<>();

    /**
     * 현재 요청을 보낸 유저의 id를 반환하는 메서드
     * 1. ThreadLocal에 저장된 User 조회
     * 2. 인증정보가 저장이 안 되어있으면 throw error
     *
     * @return
     * @throws UserPrincipalNotFoundException
     */
    public Integer getCurrentUserId() throws UserPrincipalNotFoundException {
        User user = threadLocal.get();
        if (user == null) {
            throw new UserPrincipalNotFoundException("인증된 유저가 없습니다.");
        }

        return user.getId();
    }

    /**
     * 현재 요청을 보낸 유저 객체를 반환하는 메서드
     * 1. ThreadLocal에 저장된 User 조회
     * 2. 인증정보가 저장이 안 되어있으면 throw error
     *
     * @return
     * @throws UserPrincipalNotFoundException
     */
    public User getCurrentUser() throws UserPrincipalNotFoundException {
        User user = threadLocal.get();
        if (user == null) {
            throw new UserPrincipalNotFoundException("인증된 유저가 없습니다.");
        }

        return user;
    }

}
