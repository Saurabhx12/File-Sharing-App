package com.example.FileSharingApp;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
public class FileController 
{
    @Autowired
    private FileService fileService;

    @GetMapping("/home")
    public String index(Model model) 
    {
        System.out.println("Accessing home page");
        List<FileModel> files = fileService.getAllFiles();
        if (files == null) files = new ArrayList<>();
        model.addAttribute("files", files);
        System.out.println("Accessed home page");
        return "index";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("uploadedBy") String uploadedBy) throws Throwable 
    {
        System.out.println("Uploading file: " + file.getOriginalFilename());
        fileService.uploadFile(file, uploadedBy);
        System.out.println("Uploaded file: " + file.getOriginalFilename());
        return "redirect:/files";
    }

    @GetMapping("/files")
    public String list(Model model) 
    {
        System.out.println("Accessing file list");
        List<FileModel> files = fileService.getAllFiles(); 
        if (files == null) files = new ArrayList<>();
        model.addAttribute("files", files);
        System.out.println("Accessed file list");
        return "list-files";
    }

    @GetMapping("/share/{fileId}")
    public String share(@PathVariable String fileId, Model model) 
    {
        System.out.println("Accessing file share");
        try 
        {
            FileModel file = fileService.getFileById(Integer.parseInt(fileId));
            if (file != null) 
            {
                String currentURL = ServletUriComponentsBuilder.fromCurrentRequestUri().toUriString();
                model.addAttribute("shareUrl", currentURL);
                model.addAttribute("file", file);
                System.out.println("Accessed file share");
                return "share";
            }
        } 
        catch (NumberFormatException e) 
        {
            System.out.println("Error sharing file: " + e.getMessage());
        }
        System.out.println("File not found");
        return "redirect:/files";
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable int fileId) 
    {
        System.out.println("Accessing download for file with id: " + fileId);
        try
        {
            FileModel file = fileService.getFileById(fileId);
            if(file == null)
            {
                System.out.println("File with ID " + fileId + " not found.");
                return ResponseEntity.notFound().build();
            }

            // Proceed with the download
            return fileService.downloadFile(file.getId());
        }
        catch (Exception e)
        {
            System.out.println("Error downloading file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteFile(@PathVariable("id") int id) {
        System.out.println("Deleting file with id: " + id);
        try {
            fileService.deleteFile(id);
            System.out.println("Deleted file with id: " + id);
        } 
        catch (Exception e) 
        {
            System.out.println("Error deleting file: " + e.getMessage());
        }
        return "redirect:/files";
    }
}
