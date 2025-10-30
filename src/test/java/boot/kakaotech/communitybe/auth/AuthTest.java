package boot.kakaotech.communitybe.auth;

import boot.kakaotech.communitybe.auth.dto.SignupDto;
import boot.kakaotech.communitybe.auth.service.AuthService;
import boot.kakaotech.communitybe.common.encoder.PasswordEncoder;
import boot.kakaotech.communitybe.user.entity.User;
import boot.kakaotech.communitybe.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@Transactional
public class AuthTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원 가입 테스트")
    public void signupTest(){
        AuthService mockAuthService = Mockito.mock(AuthService.class);
        PasswordEncoder mockPasswordEncoder = Mockito.mock(PasswordEncoder.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);

        SignupDto signupDto = SignupDto.builder()
                .email("test@test.com")
                .password("test")
                .nickname("testUser")
                .build();

        when(mockPasswordEncoder.encode("test")).thenReturn("test");

        User user = User.builder()
                .email("test@test.com")
                .password(mockPasswordEncoder.encode("test"))
                .nickname("testUser")
                .build();

        when(mockUserRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        mockAuthService.signup(signupDto);
        User saved = mockUserRepository.findByEmail(signupDto.getEmail()).get();

        assertThat(saved.getEmail()).isEqualTo(signupDto.getEmail());
        assertThat(saved.getPassword()).isEqualTo(signupDto.getPassword());
        assertThat(saved.getNickname()).isEqualTo(signupDto.getNickname());
    }

}
