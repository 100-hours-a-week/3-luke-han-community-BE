package boot.kakaotech.communitybe.post.entity;

import boot.kakaotech.communitybe.comment.entity.Comment;
import boot.kakaotech.communitybe.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User author;

    @Column(length = 30, nullable = false)
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(nullable = false)
    private int viewCount;

    @Column(columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> likes;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images;

    @OneToMany(mappedBy = "post",  cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    public void clearImages() {
        for (PostImage postImage : images) {
            postImage.setPost(null);
        }

        images.clear();
    }

    public void addImage(PostImage postImage) {
        images.add(postImage);
        postImage.setPost(this);
    }

    public void likePost(PostLike postLike) {
        likes.add(postLike);
        postLike.setPost(this);
    }

}
