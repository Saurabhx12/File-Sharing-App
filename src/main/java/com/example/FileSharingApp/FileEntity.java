package com.example.FileSharingApp;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "file_entity")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileEntity 
{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(name = "filename", nullable = false)
    private String filename;
    
    @Column(name = "uploaded_by", nullable = false)
    private String uploadedBy;
    
    @Column(name = "shared_with")
    private String sharedWith;
    
    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime;
    
    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;
    
    @Lob
    @Column(name = "file_data", nullable = false)
    private byte[] fileData;
    
    // Constructor for creating new file entities
    public FileEntity(String filename, String uploadedBy, byte[] fileData) {
        this.filename = filename;
        this.uploadedBy = uploadedBy;
        this.fileData = fileData;
        this.uploadTime = LocalDateTime.now();
    }
    
    // Constructor with expiry time
    public FileEntity(String filename, String uploadedBy, byte[] fileData, LocalDateTime expiryTime) {
        this.filename = filename;
        this.uploadedBy = uploadedBy;
        this.fileData = fileData;
        this.uploadTime = LocalDateTime.now();
        this.expiryTime = expiryTime;
    }
}
