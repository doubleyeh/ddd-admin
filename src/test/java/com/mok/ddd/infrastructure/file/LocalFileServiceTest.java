package com.mok.ddd.infrastructure.file;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = LocalFileService.class)
@TestPropertySource(properties = {
        "file.storage.local.path=./test-uploads",
        "file.storage.local.domain=http://localhost:8080",
        "file.storage.local.prefix=/files"
})
public class LocalFileServiceTest {

    @Autowired
    private FileService fileService;

    private final Path testUploadDir = Paths.get("./test-uploads");

    @BeforeEach
    void setUp() throws IOException {
        // 为每个测试创建一个干净的目录
        if (Files.exists(testUploadDir)) {
            FileSystemUtils.deleteRecursively(testUploadDir);
        }
        Files.createDirectories(testUploadDir);
    }

    @AfterEach
    void tearDown() throws IOException {
        // 测试后清理目录
        FileSystemUtils.deleteRecursively(testUploadDir);
    }

    @Test
    void testUpload_shouldSaveFileAndReturnUrl() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-upload.txt",
                "text/plain",
                "Hello, Upload!".getBytes()
        );

        // When
        String url = fileService.upload(file);

        // Then
        assertNotNull(url);
        assertTrue(url.startsWith("http://localhost:8080/files/"));

        // 验证文件是否真实创建
        String relativePath = url.substring("http://localhost:8080/files/".length());
        Path uploadedFilePath = testUploadDir.resolve(relativePath);
        assertTrue(Files.exists(uploadedFilePath));
        assertEquals("Hello, Upload!", Files.readString(uploadedFilePath));
    }

    @Test
    void testDelete_shouldRemoveUploadedFile() throws IOException {
        // Given: 一个已上传的文件
        InputStream inputStream = new java.io.ByteArrayInputStream("Hello, Delete!".getBytes());
        String url = fileService.upload(inputStream, "test-delete.txt");
        String relativePath = url.substring("http://localhost:8080/files/".length());
        Path uploadedFilePath = testUploadDir.resolve(relativePath);

        // 确保文件在删除前存在
        assertTrue(Files.exists(uploadedFilePath));

        // When
        fileService.delete(url);

        // Then
        assertFalse(Files.exists(uploadedFilePath));
    }
}
