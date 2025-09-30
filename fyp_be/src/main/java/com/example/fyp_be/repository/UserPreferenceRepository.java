package com.example.fyp_be.repository;

import com.example.fyp_be.model.UserPreference;
import com.example.fyp_be.model.UserPreferenceId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, UserPreferenceId> {

    // JPA magic: Spring will automatically create a query that finds all
    // preferences based on the User object's primary key (the userId).
    List<UserPreference> findByUser_UserId(Integer userId);

    // We also need a way to delete all existing preferences for a user before saving new ones.
    // The @Modifying and @Transactional annotations are required for delete operations.
    @Modifying
    @Transactional
    void deleteByUser_UserId(Integer userId);
}