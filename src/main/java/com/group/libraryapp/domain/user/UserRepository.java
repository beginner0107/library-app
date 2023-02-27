package com.group.libraryapp.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * By 앞에 들어갈 수 있는 구절 정리
 * find: 1건을 가져온다. 반환 타입은 객체가 될 수도 있고, Optional<타입>이 될 수도 있다.
 * findAll: 쿼리의 결과물이 N개인 경우 사용. List<타입> 반환
 * exists: 쿼리 결과가 존재하는지 확인. 반환 타입은 boolean
 * count: SQL의 결과 개수를 센다. 반환 타입은 long이다.
 *
 * By 뒤에 들어갈 수 있는 구절 정리
 * GreaterThan: 초과
 * GreaterThanEqual: 이상
 * LessThan: 미만
 * LessThanEqual: 이하
 * Between: 사이에
 * StartsWith: ~로 시작하는
 * EndsWith: ~로 끝나는
 *
 * 예시)
 * List<User> findAllByAgeBetween(int startAge, int endAge)
 * -> SELECT * FROM user WHERE age BETWEEN ? AND ?;
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByName(String name);

}
