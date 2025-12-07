package boot.kakaotech.communitybe.common.validation;

import boot.kakaotech.communitybe.auth.dto.LoginRequest;
import boot.kakaotech.communitybe.auth.dto.SignupRequest;
import boot.kakaotech.communitybe.comment.entity.Comment;
import boot.kakaotech.communitybe.comment.repository.CommentRepository;
import boot.kakaotech.communitybe.common.encoder.PasswordEncoder;
import boot.kakaotech.communitybe.common.exception.BusinessException;
import boot.kakaotech.communitybe.common.exception.ErrorCode;
import boot.kakaotech.communitybe.post.entity.Post;
import boot.kakaotech.communitybe.post.repository.PostRepository;
import boot.kakaotech.communitybe.user.dto.PasswordDto;
import boot.kakaotech.communitybe.user.entity.User;
import boot.kakaotech.communitybe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Validator {

    private final PasswordEncoder passwordEncoder;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    /**
     * 이메일과 패스워드 validation하는 메서드
     * 맞지 않으면 throw error
     *
     * @param request
     * @param user
     */
    public void validateUserInfo(LoginRequest request, User user) {
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_USERINFO);
        }
    }

    /**
     * signup 요청 시 받은 데이터 검증 메서드
     * 1. 이메일 형식 체크
     * 2. 비밀번호 길이, 영문 대문자, 소문자, 특수문자, 숫자 체크
     * 3. 닉네임 길이, 공백 체크
     *
     * @param signupRequest
     */
    public void validateSignup(SignupRequest signupRequest) {
        String email = signupRequest.getEmail();
        validateEmail(email);

        String password = signupRequest.getPassword();
        validatePassword(password);

        String nickname = signupRequest.getNickname();
        validateNickname(nickname);
    }

    /**
     * 이메일 validation하는 메서드
     * ooo@ooo.ooo 형식인지 확인
     *
     * @param email
     */
    public void validateEmail(String email) {
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

        if (!email.matches(regex)) {
            throw new BusinessException(ErrorCode.INVALID_FORMAT);
        }
    }

    /**
     * 비밀번호 validation하는 메서드
     * 8자 이상, 20자 이하, 대문자, 소문자, 특수문자 1개 이상 포함하는지 확인
     *
     * @param password
     */
    public void validatePassword(String password) {
        String regex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=`~\\[\\]{};':\",./<>?]).+$";

        if (password.length() < 8 || password.length() > 20 || !password.matches(regex)) {
            throw new BusinessException(ErrorCode.INVALID_FORMAT);
        }
    }

    /**
     * 닉네임 validation하는 메서드
     * 비어있는지, 10자 이내인지, 중간에 띄어쓰기 있는지 확인
     *
     * @param nickname
     */
    public void validateNickname(String nickname) {
        String regex = ".*\\s.*";

        if (nickname.isEmpty() || nickname.length() > 10 || nickname.matches(regex)) {
            throw new BusinessException(ErrorCode.INVALID_FORMAT);
        }
    }

    /**
     * 게시글과 작성자에 대한 validation 하는 메서드
     * 존재하지 않는 게시글이거나 게시글 작성자가 아니면 에러
     *
     * @param post
     * @param user
     */
    public void validatePostAndAuthor(Post post, User user) {
        if (post == null) {
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
        }
        if (user == null) {
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
        }

        Integer authorId = post.getAuthor() != null ? post.getAuthor().getId() : null;
        if (!user.getId().equals(authorId)) {
            throw new BusinessException(ErrorCode.REQUEST_FROM_OTHERS);
        }
    }

    /**
     * Post 존재하는지 validation하는 메서드
     *
     * @param postId
     * @return
     */
    public Post validatePostByIdAndReturn(Integer postId) {
        Post post = postRepository.findById(postId).orElse(null);

        if (post == null) {
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
        }

        return post;
    }

    /**
     * 댓글과 작성자에 대해 validation하는 메서드
     *
     * @param comment
     * @param user
     */
    public void validateCommentAndAuthor(Comment comment, User user) {
        if (comment == null) {
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
        }
        if (user == null) {
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
        }

        Integer writerId = comment.getUser() != null ? comment.getUser().getId() : null;
        if (!user.getId().equals(writerId)) {
            throw new BusinessException(ErrorCode.REQUEST_FROM_OTHERS);
        }
    }

    /**
     * 댓글 id로 해당 댓글 존재하는지 확인 후 반환하는 메서드
     *
     * @param commentId
     * @return
     */
    public Comment validateCommentByIdAndReturn(Integer commentId, User user) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
        }

        if (user == null) {
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
        }

        if (comment.getUser() == null || comment.getUser().getId() == null) {
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
        }

        Integer writerId = comment.getUser().getId();
        if (!user.getId().equals(writerId)) {
            throw new BusinessException(ErrorCode.REQUEST_FROM_OTHERS);
        }

        return comment;
    }

    /**
     * 요청한 유저와 수정할 유저 일치하는지 validation하는 메서드
     *
     * @param requestUser
     * @param userId
     * @return
     */
    public User validateUserInfo(User requestUser, Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
        }

        if (!user.getId().equals(requestUser.getId())) {
            throw new BusinessException(ErrorCode.REQUEST_FROM_OTHERS);
        }

        return user;
    }

    /**
     * 유저 확인 후 비밀번호 일치하는지 validation하는 메서드
     *
     * @param requestUser
     * @param dto
     * @return
     */
    public User validateNewPassword(User requestUser, PasswordDto dto) {
        User user = userRepository.findById(dto.getUserId()).orElse(null);
        if (user == null) {
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
        }

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_NOT_MATCHED);
        }

        return user;
    }

    public void validatePost(Post post) {
        if  (post == null) {
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
        }
    }

}
