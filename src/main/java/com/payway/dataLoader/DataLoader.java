//package com.payway.dataLoader;
//
//import com.payway.enums.ERole;
//import com.payway.model.Role;
//import com.payway.repository.RoleRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class DataLoader implements CommandLineRunner {
//
//    private final RoleRepository roleRepository;
//
//    @Override
//    public void run(String... args) throws Exception {
//        if (roleRepository.count() == 0) {
//            roleRepository.save(new Role(ERole.ROLE_USER));
//            roleRepository.save(new Role(ERole.ROLE_ADMIN));
//            roleRepository.save(new Role(ERole.ROLE_SUPER_ADMIN));
//        }
//    }
//}