package com.example.FileSharingApp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileServiceImpl implements FileService
{
  @Autowired
  private FileRepo fileRepository;
  
  private FileModel ConvertToFileModel(FileEntity file, String uploadedBy) 
  {
    FileModel fileModel = new FileModel();
    BeanUtils.copyProperties(file, fileModel);
    fileModel.setUploadedBy(uploadedBy);
    return fileModel;
  }

  @Override
  public List<FileModel> getAllFiles() 
  {
    List<FileEntity> fileEntities = fileRepository.findAll();
    if(fileEntities.isEmpty() || fileEntities.stream().allMatch(file -> file.getFileData() == null)) 
    {
      System.out.println("No files found in the repository.");
      return List.of(); // Return an empty list if no files are found
    }
    return fileEntities.stream().map(entity -> ConvertToFileModel(entity, entity.getUploadedBy())).collect(Collectors.toList());
  }

  @Override
  public FileModel getFileById(int id) 
  {
    FileEntity fileEntity = fileRepository.findById(id).orElse(null);
    if (fileEntity != null) return ConvertToFileModel(fileEntity, fileEntity.getUploadedBy());
    else 
    {
      // Handle the case where the file is not found, e.g., throw an exception or return null
      // For now, returning null as per the original code structure
      System.out.println("File with ID " + id + " not found.");
      return null;
    } 
  }

  @Override
  public FileModel getFileByUploadedBy(String uploadedBy) 
  {
    List<FileEntity> fileEntities = fileRepository.findByUploadedBy(uploadedBy);
    if (!fileEntities.isEmpty()) return ConvertToFileModel(fileEntities.get(0), uploadedBy);
    // Handle the case where no files are found for the given user
    System.out.println("No files found for user: " + uploadedBy);
    return null;
  }

  @Override
  public FileModel getFileBySharedWith(String sharedWith) 
  {
    List<FileEntity> fileEntities = fileRepository.findBySharedWith(sharedWith);
    if (!fileEntities.isEmpty()) return ConvertToFileModel(fileEntities.get(0), fileEntities.get(0).getUploadedBy());
    // Handle the case where no files are found shared with the given user
    System.out.println("No files found shared with user: " + sharedWith);
    return null;
  }

  @Override
  public ResponseEntity<com.example.FileSharingApp.FileModel> uploadFile(MultipartFile file, String uploadedBy) throws IOException
  {
    try
    {
      if(file.isEmpty()) throw new FileNotFoundException("File is empty or not found.");
      if(uploadedBy == null || uploadedBy.isEmpty()) throw new IllegalArgumentException("Uploader name is required.");
      
      FileEntity fileEntity = new FileEntity();
      fileEntity.setFilename(file.getOriginalFilename());
      fileEntity.setUploadedBy(uploadedBy);
      fileEntity.setExpiryTime(LocalDateTime.now().plusDays(1));
      fileEntity.setUploadTime(LocalDateTime.now());
      fileEntity.setFileData(file.getBytes());
      fileRepository.save(fileEntity);

      return ResponseEntity.ok().body(ConvertToFileModel(fileEntity, uploadedBy));
    }
    catch (IOException e)
    {
      throw new IOException("Error uploading file: " + e.getMessage(), e);
    }
    catch (IllegalArgumentException e)
    {
      throw new RuntimeException("Unexpected error occurred while uploading file: " + e.getMessage(), e);
    }
    
  }

  @Override
  public ResponseEntity<FileModel> shareFile(int fileId, String sharedWith) throws FileNotFoundException 
  {
    Optional<FileEntity> optionalFileEntity = fileRepository.findById(fileId);
    if(optionalFileEntity.isPresent()) 
    {
      FileEntity fileEntity = optionalFileEntity.get();
      fileEntity.setSharedWith(sharedWith);
      fileRepository.save(fileEntity);
      return ResponseEntity.ok().body(ConvertToFileModel(fileEntity, sharedWith));
    } 
    else throw new FileNotFoundException("File with ID " + fileId + " not found.");
  }

  @Override
  public void deleteFile(int id) throws FileNotFoundException 
  {
    Optional<FileEntity> optionalFileEntity = fileRepository.findById(id);
    if (optionalFileEntity.isPresent()) fileRepository.delete(optionalFileEntity.get());
    else  throw new FileNotFoundException("File with ID " + id + " not found.");
  }

  @Override
  public ResponseEntity<org.springframework.core.io.Resource> downloadFile(int fileId) 
  {
    try 
    {
      FileModel fileModel = getFileById(fileId);
      if (fileModel == null) 
      {
        System.out.println("File with ID " + fileId + " not found.");
        return ResponseEntity.notFound().build();
      }
        
      if (fileModel.getFileData() == null || fileModel.getFileData().length == 0) 
      {
        System.out.println("File data is empty for file ID: " + fileId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
      }

        ByteArrayResource resource = new ByteArrayResource(fileModel.getFileData());
        String contentType = determineContentType(fileModel.getFilename());
        
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileModel.getFilename() + "\"")
              .contentLength(fileModel.getFileData().length).body(resource);
                
    } 
    catch (Exception e) 
    {
      System.out.println("Error in downloadFile service: " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}

  private String determineContentType(String filename)
  {
    if(filename == null) return "application/octet-stream";
    int lastDotIndex = filename.lastIndexOf('.');
    if (lastDotIndex == -1) return "application/octet-stream";
    String extension = filename.substring(lastDotIndex + 1);
    switch (extension.toLowerCase())
    {
      case "jpg":
      case "jpeg":
        return "image/jpeg";
      case "png":
        return "image/png";
      case "gif":
        return "image/gif";
      case "pdf":
        return "application/pdf";
      case "txt":
        return "text/plain";
      case "zip":
        return "application/zip";
      case "mp4":
        return "video/mp4";
      case "mp3":
        return "audio/mpeg";
      case "doc":
      case "docx":
        return "application/msword";
      case "xls":
      case "xlsx":
        return "application/vnd.ms-excel";
      default:
        return "application/octet-stream";
    }
  }

  @Override
  public ResponseEntity<String> updateFile(int id, MultipartFile file) throws Exception 
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public ResponseEntity<String> deleteExpiredFiles() throws Exception 
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
