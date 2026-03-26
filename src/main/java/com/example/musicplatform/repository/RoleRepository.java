package com.example.musicplatform.repository;

import com.example.musicplatform.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoleRepository  extends JpaRepository<Role, Long> {

    @Query("SELECT r.roleName FROM Role r WHERE r.id IN :roleIds")
    List<String> findRoleNamesByIds(@Param("roleIds") List<Long> roleIds);

    Role findByRoleName(String roleName);
}
