package com.konfyrm.gigatester.subjects.repository;

import com.konfyrm.gigatester.subjects.domain.entity.SubjectGroupAccessRequest;
import com.konfyrm.gigatester.subjects.domain.entity.SubjectGroupAccessStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubjectGroupAccessRepository extends JpaRepository<SubjectGroupAccessRequest, UUID> {

    Optional<SubjectGroupAccessRequest> findByUser_IdAndSubjectGroup_Id(UUID userId, UUID groupId);

    List<SubjectGroupAccessRequest> findByUser_Id(UUID userId);

    List<SubjectGroupAccessRequest> findBySubjectGroup_IdAndStatus(UUID groupId, SubjectGroupAccessStatus status);

    List<SubjectGroupAccessRequest> findBySubjectGroup_Id(UUID groupId);

    List<SubjectGroupAccessRequest> findByStatus(SubjectGroupAccessStatus status);

}
