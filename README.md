## 🥬 목차

- [1️⃣ 서비스 소개](#-서비스-소개)
- [2️⃣ 팀원 소개](#-팀원-소개)
- [3️⃣ 기술 스택](#-기술-스택)
- [4️⃣ 시스템 아키텍처](#-시스템-아키텍처)

<br><br>

## 🥬 서비스 소개
### 가볍게 소통하는 창구 목적의 커뮤니티 프로젝트입니다.

- 🕜 **진행 기간** : 2025.09.10. ~ 2025.12.7.
- 👨‍👩‍👧‍👦 **진행 인원** : 1명

| <a href="https://github.com/100-hours-a-week/3-luke-han-community-FE">Frontend-Repository</a>

<br>

## 🥬 팀원 소개
<table>
  <tr>
    <td align="center"><a href="https://github.com/rpeowiqu"><img src="https://github.com/rpeowiqu.png" width="160px;" alt=""/><br /><b>한재서</b></a><br />프론트엔드<br/><a href="https://github.com/rpeowiqu"><img src="https://img.shields.io/badge/GitHub-181717?style=flat&logo=github&logoColor=white"/></a></td>
  </tr>
</table>

<br><br>

## 🥬 기술 스택

<table>
  <tr>
    <td align="center">백엔드</td>
    <td>
      <img src="https://img.shields.io/badge/Java-007396?style=flat&logo=&logoColor=white"/>
      <img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=flat&logo=springboot&logoColor=white"/>
    </td>
  </tr>
  <tr>
    <td align="center">DB</td>
    <td>
      <img src="https://img.shields.io/badge/MySQL-4479A1?style=flat&logo=mysql&logoColor=white"/>
    </td>
  </tr>
  <tr>
    <td align="center">인프라</td>
    <td>
      <img src="https://img.shields.io/badge/AWS-232F3E?style=flat&logo=amazonwebservices&logoColor=white"/>
      <img src="https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white"/>
    </td>
  </tr>
</table>

<br><br>

## 🥬 시스템 아키텍처

<img src="/src/main/resources/static/damul_board_architecture.png"/>
<br />

### 🧱 시스템 아키텍처 설계 의도
- **Nginx 리버스 프록시**
  - public subnet에 Nginx를 위치시켜 private subnet으로의 직접적인 요청을 막고 애플리케이션 보호
  - 프론트엔드와 백엔드 앞단에 Nginx를 두어 **도메인/경로에 따른 라우팅** 진행
- **용도에 따른 인스턴스 분리**
  - 리버스 프록싱을 진행하는 Nginx는 public subnet에, 애플리케이션과 데이터베이스는 private subnet에 위치시켜 보안성 확보
  - 메모리를 많이 사용하는 스프링부트 애플리케이션과 데이터베이스를 분리시켜 안정적인 램 성능 확보
- **S3 Storage Service**
  - 이미지를 서버 로컬이 아닌 오브젝트 스토리지에 저장해 **스케일 아웃** 가능한 구조 확보
  - 용량 최적화를 위해 Lambda로 이미지 후처리 진행
  - 인스턴스 스펙 대비 이미지 업로드를 통한 서버 부하를 줄이기 위해 presigned url을 통해 클라이언트에서 업로드하는 구조 설정

## 🥬 ERD
<img src="/src/main/resources/static/board_assignment.png" />
<br />

### 🧩ERD 설계 의도
- 유저 관리
  - 사용자는 이메일, 프로필 이미지, 비밀번호, 닉네임 정보 필요
- 게시글 관리
  - 제목, 내용, 이미지, 작성 일시, 삭제 일시 등의 정보 포함
  - 사용자의 특정 게시글 좋아요 여부를 판단하기 위해 테이블 분리
- 댓글
  - 작성자, 내용, 대댓글인지 여부, 작성 일시, 삭제 일시 등의 정보 포함

<br><br>

### 시연 영상
[![Youtube Badge](https://img.shields.io/badge/Youtube-ff0000?style=flat-square&logo=youtube)](https://youtu.be/foXF3dAmswU)
