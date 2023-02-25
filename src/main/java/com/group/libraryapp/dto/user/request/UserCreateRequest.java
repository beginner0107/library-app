package com.group.libraryapp.dto.user.request;

public class UserCreateRequest {

    private String name;
    private Integer age; // 선택

    public String getName() {
        return name;
    }

    public Integer getAge() {
        return age;
    }
}
