package com.example.FileSharingApp;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;

public interface FileService
{
    public List<FileModel> getAllFiles();
    public FileModel getFileById(int id);
    public FileModel getFileByUploadedBy(String uploadedBy);
    public FileModel getFileBySharedWith(String sharedWith);
    public ResponseEntity<FileModel> uploadFile(MultipartFile file, String uploadedBy) throws Exception;
    public ResponseEntity<FileModel> shareFile(int fileId, String sharedWith) throws Exception;
    public void deleteFile(int id) throws Exception;
    public ResponseEntity<String> deleteExpiredFiles() throws Exception;
    public ResponseEntity<Resource> downloadFile(int file) throws Exception;
    public ResponseEntity<String> updateFile(int id, MultipartFile file) throws Exception;
}
