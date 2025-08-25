package com.example.FileSharingApp;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepo extends JpaRepository<FileEntity, Integer>
{
    List<FileEntity> findByExpiryTimeBefore(LocalDateTime now);
    List<FileEntity> findByUploadedBy(String uploadedBy);
    List<FileEntity> findBySharedWith(String sharedWith);
}
    