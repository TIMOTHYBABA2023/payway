package com.payway.factory;

import com.payway.enums.ERole;
import com.payway.model.Role;
import com.payway.exception.RoleNotFoundException;
import com.payway.repository.RoleRepository;
import org.springframework.stereotype.Component;

@Component
public class RoleFactory {
    private final RoleRepository roleRepository;

    public RoleFactory(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role getInstance(String roleName) throws RoleNotFoundException {
        return roleRepository.findByName(ERole.valueOf(roleName.toUpperCase()))
                .orElseThrow(() -> new RoleNotFoundException("No role found for: " + roleName));
    }
}