# 프로젝트 설명
### 도서관리 프로젝트
#### 개발 필수 요건

#### 회원관리
- [X] 회원 가입/목록/수정/삭제가 가능해야 한다.

#### 도서관리
- [X] 도서를 저장할 수 있어야 한다.
- [X] 도서를 대여할 수 있어야 한다.
- [X] 도서를 반납할 수 있어야 한다.

#### 대출내역관리
- [X] 도서가 대여 중인지, 반납인지 확인할 수 있어야 한다.

# 사용한 기술
- <b>Java 11</b>
- <b>SpringBoot 2.7.8</b>
- <b>H2 DB</b>
  - 로컬 환경에서 H2 DB를 사용하였습니다.
- <b>JPA</b>
- <b>MySql</b>
  - 개발 및 배포 환경에서 MySql을 사용하였습니다.
- <b>AWS-EC2</b>
  
# Project Sturucture
```

─ src
    ├─ config // 설정관련
    ├─ controller // 컨트롤러
    │   ├─ book 
    │   ├─ calculator // 공부용 (Disabled)
    │   └─ user  
    ├─ domain // 도메인
    │   ├─ book // 도서 엔티티, 도서 Repository 
    │   └─ user // 회원 엔티티, 회원 Repository
    │       └─ loanhistory // 대출 기록 엔티티, 대출 기록 Repository
    ├─ dto // DTO
    │   ├─ book // 도서관리 요청
    │   │  └─ request  
    │   ├─ calculator // (Disabled)
    │   │  └─ request
    │   └─ user // 회원관리 요청/응답
    │       ├─ request 
    │       └─ response
    ├─ repository 
    │   └─ user // JdbcTemplate (리팩토링 후 사용 X)
    └─ service // 서비스 계층
        └─ book
        └─ user
```

## 아이템 선정
- 스프링 부트를 공부하기 위해 강의를 찾다가, 도서관리 웹 프로젝트를 만드는 흥미로운 소재를 찾아 따라해보고, 
실제 배포까지 해보는 과정을 경험하고 싶어 선택하게 되었다.

# ERD
![erd](https://user-images.githubusercontent.com/81161819/226174494-e1488ad3-34f9-4e5a-a9ce-5c36f9620fd9.svg)

# API
## User
- 회원 가입 `(POST /user)`
- 회원 목록 `(GET /user)`
- 회원 정보 수정 `(PUT /user)`
- 회원 탈퇴 `(DELETE /user)`

##  Book
- 도서 등록 `(POST /book)`
- 도서 대여 `(POST /book/loan)`
- 도서 반납 `(PUT /book/return)`

# ISSUE, WORKFLOW
## Clean Code 리팩토링 
```
함수는 최대한 작게 만들고 한 가지 일만 수행하는 것이 좋다.
클래스는 작아야 하며 하나의 책임만을 가져야 한다. (단일 책임 원칙)
```
현재 `Controller`에 비즈니스적인 요소, DB와 연동하는 기능이 모두 담겨 있습니다. 

작성한 Controller 함수 1개가 3000줄이 넘는다면?
- 그 함수를 <b>동시에</b> 여러 명이 수정할 수 없습니다.
- 그 함수를 <b>읽고, 이해</b>하는 것이 너무 어렵습니다.
- 그 함수의 어느 부분을 수정하더라도 <b>함수 전체</b>에 <b>영향</b>을 미칠 수 있기 때문에 함부로 건들 수 없게 됩니다.
- 너무 큰 기능이기 때문에 <b>테스트가 힘들어집니다.</b>
- 종합적으로 <b>유지보수성</b>이 매우 떨어집니다.

### 현재 `Controller`

```java
    @GetMapping("/user")
    public List<UserResponse> getUsers() {
        String sql = "SELECT * FROM user";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            long id = rs.getLong("id");
            String name = rs.getString("name");
            int age = rs.getInt("age");
            return new UserResponse(id, name, age);
        });
    }
```
1. API의 <b>진입 지점</b>으로써 HTTP Body를 객체로 변환한다.<br> -> `Controller` 담당
2. 현재 유저가 있는지 없는지 확인하고 <b>예외 처리</b>를 한다.<br> -> `Service` 담당
3. SQL을 사용해 실제 Database와의 통신을 담당한다.<br> -> `Repository` 담당

<b>해결</b>: 역할을 분담해서 기존 Controller를 Service, Repository로 분리하였습니다. 

## JPA 리팩토링
### 현재 Repository

jdbcTemplate를 사용하였습니다.
```java
    public boolean isUserNotExist(long id) {
        String readSql = "SELECT * FROM user WHERE id = ?";
        return jdbcTemplate.query(readSql, (rs, rowNum) -> 0, id).isEmpty();
    }
```

<b>문제점</b>
1. SQL을 문자열로 작성하였습니다.
2. 문자열로 작성하기 때문에 실수를 할 수 있고, 실수를 인지하는 시점이 느립니다.
3. 컴파일 시점(서버를 실행할 때)에 발견되지 않고, 런타임 시점(실제 서버가 가동되고 나서)에 발견된다는 단점이 존재합니다.
4. 특정 데이터베이스에 종속적입니다.
5. 반복 작업이 많아진다. 테이블을 하나 만들 때마다 CRUD 쿼리가 항상 필요합니다.
6. 데이터베이스의 테이블과 객체는 패러다임이 다릅니다.

### JPA refactoring Example
```java
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByName(String name);
}
```
<b>해결</b>: `Repository`를 만들고 `JpaRepository`를 상속 받아 JPA로 전환하였습니다.



## 배포
- 최종 사용자에게 SW를 전달하는 과정

### 대여한 컴퓨터에 접속하는 방법
1. 다운로드 받은 키 페어(pem키)를 이용하는 방법
  - 접속하려는 EC2의 IP주소가 필요
  - 이전 시간에 다운받았던 키 페어
  - 접속하기 위한 프로그램(git CLI 혹은 Mac terminal)
2. AWS 콘솔을 활용해 접속하는 방법

#### 리눅스에서 스프링 서버 배포를 위한 프로그램을 설치
```
// 리눅스 패키지 관리 프로그램(gradle과 비슷한 역할)
sudo yum update 

// yum을 이용해 프로그램을 다운로드
sudo yum install git -y

// Java 설치
sudo yum install java-11-amazon-corretto -y

wget https://dev.mysql.com/get/mysql80-community-release-el7-5.noarch.rpm

sudo rpm -ivh mysql80-community-release-el7-5.noarch.rpm

// mySQL 설치
sudo yum install mysql-community-server

// 현재 보이지 않는 프로그램을 관리하는 명령어
sudo systemctl status mysqld

// mysqld 프로그램을 재시작
sudo systemctl restart mysql

// mysql8의 임시 비밀번호를 확인하는 명령어
sudo cat /var/log/mysqld.log | grep "A temporary password"

// 입력한 뒤 임시 비밀번호 입력
mysql -u root -p "비밀번호"

// 임시 비밀번호를 변경해야 함 / yml 파일에 password도 재설정
ALTER user 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY "비밀번호"
```
```sql
create database library;
create table user(
  id bigint auto_increment,
  name varchar(25),
  age int,
  primary key(id)
);

create table book(
  id bigint auto_increment,
  name varchar(255),
  primary key (id)
);

create table user_loan_history(
  id bigint auto_increment,
  user_id bigint,
  book_name varchar(255),
  is_return tinyint(1),
  primary key(id)
);
```
```
git clone [github 저장소 주소]
```

### 빌드 & 실행
- 현재 사용하고 있는 대여한 컴퓨터는 성능이 좋지 않아, 빌드나 실행시 렉이 많이 걸릴 수 있습니다.
- 따라서, 메모리가 부족한 경우 디스크를 사용할 수 있는 `SWAP` 설정을 해야 합니다.
- SWAP설정: 원래 RAM을 사용해야 하지만, RAM이 부족한 경우에 일부 DISK를 사용하게 해주는 설정입니다.
```
# swap 메모리를 할당한다 (128M * 16 = 2GB)
sudo dd if=/dev/zero of=/swapfile bs=128M count=16

# 스왑 파일에 대한 권한 업데이트
sudo chmod 600 /swapfile

# swap 영역 설정
sudo mkswap /swapfile

# swap 파일을 사용할 수 있도록 만든다.
sudo swapon /swapfile

# swap 성공 확인
sudo swapon -s
```
- 빌드 준비
```
# gradlew를 사용하기 위해 실행할 수 있도록 설정한다.
chmod + x./gradlew
```
- 빌드
```
# gradle을 이용해 프로젝트를 빌드한다. 이때 테스트를 돌리지 않는다.
./gradlew build -x test

# 테스트를 돌리고 싶다면
./gradlew build

# 빌드된 프로젝트 실행
java -jar build/libs/library-app-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```   

#### foreground / background
- 서버를 background로 동작하게 만들어야 함
```
# nohub [명령어] &
nohup java -jar library-app/build/libs/library-app-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev &

# background의 서버 종료하기
ps aux | grep java
# kill 9 프로그램 번호 
```

## 배포 사이트 주소
- http://3.35.174.160:8080/v1/index.html
