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
    - [ ] 도서관에 책을 등록 및 삭제할 수 있다.
    - [X] 사용자가 책을 빌릴 수 있다.
      - [X] 다른 사람이 그 책을 진작 빌렸다면 빌릴 수 없다.
    - [ ] 사용자가 책을 반납할 수 있다.
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
