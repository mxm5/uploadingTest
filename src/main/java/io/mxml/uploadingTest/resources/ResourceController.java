package io.mxml.uploadingTest.resources;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.copy;
import static java.nio.file.Paths.get;
import static org.apache.tomcat.util.http.fileupload.FileUploadBase.CONTENT_DISPOSITION;

@RestController
@RequestMapping("/resource")
public class ResourceController {

    ///=========================================================================
    public static final String DIRECTORY = System.getProperty("user.home") + "/Downloads/Server_files_";
    ///=========================================================================

    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadedFiles(@RequestParam("files") List<MultipartFile> multipartFiles) throws Exception {
        List<String> uploadedFilesResultsName = new ArrayList<>();


        int numberOfRequestUploads = multipartFiles.size();

        for (MultipartFile file : multipartFiles) {

            ///=========================================================================
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            ///=========================================================================
            ///static import for Files.Paths
            Path fileStoragePath = get(DIRECTORY, fileName).toAbsolutePath().normalize();
            ///static import for Files.copy
            copy(file.getInputStream(), fileStoragePath);
            uploadedFilesResultsName.add(fileName);
        }
        int successfullyUploadedFiles = uploadedFilesResultsName.size();
        if (successfullyUploadedFiles != numberOfRequestUploads) {
            throw new Exception("all files not uploaded");
        }
        return ResponseEntity.ok().body(uploadedFilesResultsName);

    }


    // Define a method to download files
    @GetMapping("downaload/{filename}")
    public ResponseEntity<Resource> downloadFiles(@PathVariable("filenane") String filename) throws IOException {
        Path filePath = get(DIRECTORY).toAbsolutePath().normalize().resolve(filename);
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException(filename + " was not found on the server");
        }
        Resource resource = new UrlResource(filePath.toUri());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("File-Name", filename);
        httpHeaders.add(CONTENT_DISPOSITION, "attachment;File-Name:" + resource.getFilename());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(Files.probeContentType(filePath)))
                .headers(httpHeaders)
                .body(resource);
    }
}