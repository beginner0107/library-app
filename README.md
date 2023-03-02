# library-app
## 아이템 선정
- 스프링 부트를 공부하기 위해 강의를 찾다가, 도서관리 웹 프로젝트를 만드는 흥미로운 소재를 찾아 따라해보고, 
실제 배포까지 해보는 과정을 경험하고 싶어 선택하게 되었다.

## 개요
- 프로젝트 명칭: library-app
- 개발인원: 1명
- 개발기간: 2023-02-23 ~ 
- 주요기능:
  - 사용자
    - [X] 도서관의 사용자를 등록할 수 있다.(이름 필수, 나이 선택)
    - [X] 도서관 사용자의 목록을 볼 수 있다.
    - [X] 도서관 사용자 이름을 업데이트 할 수 있다.
    - [X] 도서관 사용자를 삭제할 수 있다.
  - 책
    - [X] 도서관에 책을 등록 및 삭제할 수 있다.
    - [X] 사용자가 책을 빌릴 수 있다.
      - [X] 다른 사람이 그 책을 진작 빌렸다면 빌릴 수 없다.
    - [X] 사용자가 책을 반납할 수 있다.
- 개발 언어: Java 11
- 개발 환경: SpringBoot 2.7.8, gradle
- 형상관리 툴: GitHub

## 요구사항 분석
- 도서관 사용자 등록 페이지
  - 유효성 검사
    - 이름은 필수로 입력이 되어야 하며, 빈칸이 입력되면 에러 메시지 보여주기

- 강의에서 제공한 html page를 사용하기 때문에 자세한 설정 사항은 추후에 수정(서버 개발에 집중)

## 리팩토링을 해야 하는 이유(1)
### [Clean Code]
- 함수는 최대한 작게 만들고 한 가지 일만 수행하는 것이 좋다. 
- 클래스는 작아야 하며 하나의 책임만을 가져야 한다.
-> 현재 Controller에 비즈니스적인 요소, DB와 연동하는 기능이 모두 담겨 있음

- 우리가 작성한 Controller 함수 1개가 3000줄이 넘는다면?
1. 그 함수를 동시에 여러 명이 수정할 수 없다.
2. 그 함수를 읽고, 이해하는 것이 너무 어렵다.
3. 그 함수의 어느 부분을 수정하더라도 함수 전체에 영향을 미칠 수 있기 때문에 함부로 건들 수 없게 된다.
4. 너무 큰 기능이기 때문에 테스트가 힘들다.
5. 종합적으로 유지보수성이 매우 떨어진다.


### 현재 Controller는?

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
1. API의 <b>진입 지점</b>으로써 HTTP Body를 객체로 변환한다.<br>
2. 현재 유저가 있는지 없는지 확인하고 <b>예외 처리</b>를 한다.<br>
3. SQL을 사용해 실제 Database와의 통신을 담당한다.<br>

## 리팩토링을 해야 하는 이유(2)
### 현재 Repository를 확인해보면? 
```java
    public boolean isUserNotExist(long id) {
        String readSql = "SELECT * FROM user WHERE id = ?";
        return jdbcTemplate.query(readSql, (rs, rowNum) -> 0, id).isEmpty();
    }
```
- SQL을 문자열로 작성하였다.
- 문자열로 작성하기 때문에 실수를 할 수 있고, 실수를 인지하는 시점이 느리다.
- 즉, 컴파일 시점(서버를 실행할 때)에 발견되지 않고, 런타임 시점(실제 서버가 가동되고 나서)에 발견된다는 단점이 존재한다.
- 특정 데이터베이스에 종속적이게 된다.
- 반복 작업이 많아진다. 테이블을 하나 만들 때마다 CRUD 쿼리가 항상 필요하다.
- 데이터베이스의 테이블과 객체는 패러다임이 다르다. 

### JPA(Java Persistence API)
- 영속성(persistence): 서버가 재시작되어도 데이터는 영구적으로 저장되는 속성
- JPA란? 객체와 관계형 DB의 테이블을 짝지어 데이터를 영구적으로 저장할 수 있도록 정해진 Java 진영의 규칙.
- 자바 진영의 ORM(Object-Relational Mapping)

## 배포
- 최종 사용자에게 SW를 전달하는 과정

#### 상황 가정: 최종 사용자가 나의 서버를 쓸 수 있는 방법
1. 직접 나의 집으로 와서 내 컴퓨터로 서비스를 이용한다(?) -> 이상하고 불편..
2. 사용자의 컴퓨터를 이용해 나의 컴퓨터에 접속한다. -> 내 컴퓨터가 24시간 켜져 있지 않음
   - 내 컴퓨터는 스프링, MySQL 외에도 문서 편집기, 게임, 비디오 재생등을 처리
3. 내 컴퓨터를 쓰는게 아니라, 배포를 위한 전용 컴퓨터를 빌려, 내가 작성한 <b>코드를 옮긴다.</b> 
   - 전용 컴퓨터에 스프링, MySQL 등을 설치해 최종 사용자가 접속하게 한다.

#### 배포란?
- 내 컴퓨터에서 전용 컴퓨터로 코드를 옮길 수 있는 환경을 준비하고, 실제 코드로 옮기는 행위를 배포라고 부른다.
- 전용 컴퓨터에 나의 서버를 옮겨 실행시키는 것

#### 전용 컴퓨터가 없다?
- 미국 쇼핑몰 아마존이 운영하는 AWS(Amazon Web Service)에서 컴퓨터를 빌릴 예정

#### AWS에서 컴퓨터를 빌릴 때 알아두어야 할 점
- 서버용 컴퓨터는 보통 리눅스를 사용!

#### 대여한 컴퓨터에 접속하는 방법
1. 다운로드 받은 키 페어(pem키)를 이용하는 방법
  - 접속하려는 EC2의 IP주소가 필요
  - 이전 시간에 다운받았던 키 페어
  - 접속하기 위한 프로그램(git CLI 혹은 Mac terminal)
2. AWS 콘솔을 활용해 접속하는 방법

#### 리눅스 명령어
- `mkdir`: 폴더를 만드는 명령어
- `ls`: 현재 위치에서 폴더나 파일을 확인하는 명령어
- `ls -l`: 조금 더 자세한 정보를 확인할 수 있음
  - <img width="323" alt="image" src="https://user-images.githubusercontent.com/81161819/222340719-0fc2decd-8438-46af-bad8-b23d70838c1e.png">
  - `drwxrwxr-x 2 ec2-user ec2-user 6 Mar  2 05:35 folder1`
    - `d`: folder1은 폴더라는 뜻(없으면 파일)
    - `rwx` -> `r`: 읽을 수 있는 권한 / `w`: 쓸 수 있는 권한 / `x`: 실행할 수 있는 권한
    - `rwx` `rwx` `r-x` -> 폴더 소유자의 권한 / 폴더 소유그룹의 권한 / 아무나 접근했을 때
    - 2: 폴더에 걸려 있는 바로가기 개수
    - ec2-user: 이 폴더의 소유주(주인) 이름
    - ec2-user: 이 폴더의 소유그룹 이름
    - 6: 이 폴더(파일)의 크기, byte 단위
    - Mar  2 05:35: 파일의 최종 변경 시각
- `cd`: 폴더 안으로 들어가는 명령어
- `pwd`: 현재 위치를 확인하는 명령어
- `cd ..`: 상위 폴더로 올라가는 명령어
- `rmdir`: 비어 있는 폴더(디렉토리)를 제거하는 명령어

#### 리눅스에서 스프링 서버 배포를 위한 프로그램을 설치
- `sudo yum update`: 리눅스 패키지 관리 프로그램(gradle과 비슷한 역할)
- `sudo yum install git -y`: yum을 이용해 프로그램을 다운로드
- `sudo yum install java-11-amazon-corretto -y`: Java 설치
- `wget https://dev.mysql.com/get/mysql80-community-release-el7-5.noarch.rpm`
- `sudo rpm -ivh mysql80-community-release-el7-5.noarch.rpm`
- `sudo yum install mysql-community-server`: mySQL 설치
- `sudo systemctl status mysqld`: 현재 보이지 않는 프로그램을 관리하는 명령어
- `sudo systemctl restart mysql`: mysqld 프로그램을 재시작
- `sudo cat /var/log/mysqld.log | grep "A temporary password"`: mysql8의 임시 비밀번호를 확인하는 명령어
- `mysql -u root -p`: 입력한 뒤 임시 비밀번호 입력
 
- `ALTER user 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY "비밀번호"`: 임시 비밀번호를 변경해야 함
- yml 파일에 password도 재설정

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

#### 빌드 & 실행
- 현재 사용하고 있는 대여한 컴퓨터는 성능이 좋지 않아, 빌드나 실행시 렉이 많이 걸릴 수 있음
- 따라서, 메모리가 부족한 경우 디스크를 사용할 수 있는 `SWAP` 설정을 해야 함.
- SWAP설정: 원래 RAM을 사용해야 하지만, RAM이 부족한 경우에 일부 DISK를 사용하게 해주는 설정
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
nohup java -jar library-app/build/libs/library-app-0.0.1-SNAPSHOT.jar &

# background의 서버 종료하기
ps aux | grep java
# kill 9 프로그램 번호 
```

## AWS로 배포한 사이트 주소
- http://3.35.174.160:8080/v1/index.html
