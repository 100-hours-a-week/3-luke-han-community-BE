package boot.kakaotech.communitybe.util;

import boot.kakaotech.communitybe.user.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.nio.file.attribute.UserPrincipalNotFoundException;

@Component
public class UserUtil {

    /**
     * 현재 요청을 보낸 유저의 id를 반환하는 메서드
     * 1. SecurityContextHolder에서 인증정보 조회
     * 2. 인증정보가 저장이 안 되어있으면 throw error
     * 3. Authentication 객체에서 Principal 가져와서 id 반환
     *
     * @return
     * @throws UserPrincipalNotFoundException
     */
    public Integer getCurrentUserId() throws UserPrincipalNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new UserPrincipalNotFoundException(null);
        }

        // TODO: userId 반환로직 추가

        return null;
    }

    /**
     * 현재 요청을 보낸 유저 객체를 반환하는 메서드
     * 1. SecurityContextHolder에서 인증정보 조회
     * 2. 인증정보가 저장이 안 되어있으면 throw error
     * 3. Authentication 객체에서 Principal 가져와서 User 반환
     *
     * @return
     * @throws UserPrincipalNotFoundException
     */
    public User getCurrentUser() throws UserPrincipalNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new UserPrincipalNotFoundException(null);
        }

        Object principal = authentication.getPrincipal();

        // TODO: User 반환 로직 추가

        return null;
    }

}
