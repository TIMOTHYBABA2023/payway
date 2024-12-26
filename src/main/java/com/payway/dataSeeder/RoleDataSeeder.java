package com.payway.dataSeeder;

import com.payway.enums.ERole;
import com.payway.model.Role;
import com.payway.repository.RoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RoleDataSeeder {

    private final RoleRepository roleRepository;

    @EventListener
    @Transactional
    public void LoadRoles(ContextRefreshedEvent event) {

        List<ERole> roles = Arrays.stream(ERole.values()).toList();

        for(ERole erole: roles) {
            roleRepository.findByName(erole).ifPresentOrElse(
                    role -> {},
                    () -> roleRepository.save(new Role(erole))
            );

        }

    }

}