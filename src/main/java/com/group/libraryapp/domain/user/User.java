package com.group.libraryapp.domain.user;

import javax.persistence.*;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 20) // name varchar(20)
    private String name;
    private Integer age;

    protected User() {} // JPA 를 사용하기 위해서는 기본 생성자가 꼭 필요하다.

    public User(String name, Integer age) {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException(String.format("잘못된 name(%s)이 들어왔습니다.", name));
        }
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public Integer getAge() {
        return age;
    }
}
