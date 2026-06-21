package com.vigi.gate.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vigi.gate.entity.Visitor;
import com.vigi.gate.entity.VisitorLog;
import com.vigi.gate.repository.VisitorLogRepository;
import com.vigi.gate.repository.VisitorRepository;
import com.vigi.gate.service.CloudinaryService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class VisitorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VisitorRepository visitorRepository;

    @Autowired
    private VisitorLogRepository visitorLogRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    private String testNik;

    // Lokasi file foto lokal untuk test
    // Path ini otomatis mengarah ke: C:\Users\<user>\Pictures\modif.jpg
    // jadi test ini pakai fallback ekstensi.
    private final Path localPhotoPath = resolveLocalPhotoPath();

    private static Path resolveLocalPhotoPath() {
        // Daftar nama file yang akan dicoba berurutan
        String[] candidates = new String[] { "modif.jpg", "modif.jpeg", "modif.png" };

        Path baseDir = Path.of(System.getProperty("user.home"), "Pictures");

        // Cari candidate pertama yang benar-benar ada di disk
        for (String name : candidates) {
            Path p = baseDir.resolve(name);
            if (Files.exists(p)) {
                return p;
            }
        }

        // Kalau tidak ada semuanya, return kandidat pertama supaya pesan errornya jelas
        return baseDir.resolve(candidates[0]);
    }

    @BeforeEach
    void setup() {
        // Buat NIK unik yang valid: tepat 16 digit angka
        String digits = UUID.randomUUID().toString().replaceAll("\\D", "");
        if (digits.length() < 15) {
            digits = (digits + "000000000000000").substring(0, 15);
        } else {
            digits = digits.substring(0, 15);
        }
        testNik = "9" + digits;
    }

    @AfterEach
    void cleanupUploadedPhoto() {
        // Cek apakah ada Visitor dengan NIK ini
        // Kalau ada, ambil `photoUrl` lalu hapus foto yang diupload ke Cloudinary
        visitorRepository.findByNik(testNik)
            .map(Visitor::getPhoto)
            .map(this::extractPublicIdFromUrl)
            .ifPresent(cloudinaryService::deleteImage);
    }

    @Test
    @DisplayName("registerVisitor success (foto dari Pictures/modif.jpg)")
    void registerVisitor_success() throws Exception {
        // Pastikan file foto lokal ada, supaya error-nya jelas kalau kamu belum taruh filenya
        assertTrue(Files.exists(localPhotoPath), "File foto test tidak ditemukan: " + localPhotoPath);

        // Baca isi file foto ke byte[] agar bisa dijadikan multipart upload
        byte[] photoBytes = readAllBytes(localPhotoPath);

        // Ambil content type file (mis. image/jpeg). Kalau tidak bisa ditebak, pakai fallback.
        String contentType = Files.probeContentType(localPhotoPath);
        if (contentType == null || contentType.isBlank()) {
            contentType = "image/jpeg"; // fallback sederhana
        }

        // Buat multipart file untuk dikirim ke controller:
        // field name harus "photo" (sesuai @RequestParam("photo") di VisitorController)
        MockMultipartFile photo = new MockMultipartFile(
            "photo", // nama field form-data
            localPhotoPath.getFileName().toString(), // nama file asli
            contentType, // tipe konten file
            photoBytes // isi file
        );

        // Siapkan data request wajib
        String fullName = "Visitor Test";
        String purpose = "Meeting QA";

        // Kirim request multipart ke endpoint register
        MvcResult mvcResult = mockMvc.perform(multipart("/api/visitors/register")
                .file(photo)
                .param("fullName", fullName)
                .param("nik", testNik)
                .param("purpose", purpose))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.logId").isNumber())
            .andExpect(jsonPath("$.fullName").value(fullName))
            .andExpect(jsonPath("$.nik").value(testNik))
            .andExpect(jsonPath("$.purpose").value(purpose))
            .andExpect(jsonPath("$.insideArea").value(true))
            .andReturn();

        // Ambil response JSON biar kita bisa ambil `logId` untuk verifikasi database
        JsonNode response = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        Long logId = response.get("logId").asLong();

        // ===== Verifikasi DB: Visitor harus tersimpan =====
        Optional<Visitor> visitorDb = visitorRepository.findByNik(testNik);
        assertTrue(visitorDb.isPresent());

        // Ambil data Visitor dari database
        Visitor visitor = visitorDb.get();
        assertNotNull(visitor.getId());
        assertNotNull(visitor.getPhoto());
        // `photoUrl` hasil upload Cloudinary biasanya mengandung domain `res.cloudinary.com`
        assertTrue(visitor.getPhoto().contains("res.cloudinary.com"));

        // ===== Verifikasi DB: VisitorLog harus tersimpan =====
        Optional<VisitorLog> visitorLogDb = visitorLogRepository.findById(logId);
        assertTrue(visitorLogDb.isPresent());

        // Ambil data VisitorLog
        VisitorLog visitorLog = visitorLogDb.get();
        assertNotNull(visitorLog.getCheckinTime());
        assertTrue(visitorLog.getCheckoutTime() == null);
        assertNotNull(visitorLog.getRiskLevel());
        assertNotNull(visitorLog.getRiskScore());
        assertNotNull(visitorLog.getRiskReason());
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        // Kalau imageUrl null, tidak bisa diextract
        if (imageUrl == null) {
            return null;
        }

        // Cari bagian setelah "/upload/"
        String uploadMarker = "/upload/";
        int markerIndex = imageUrl.indexOf(uploadMarker);
        if (markerIndex < 0) {
            // Kalau format URL tidak sesuai, fallback return apa adanya
            return imageUrl;
        }

        // Contoh setelah marker:
        // v1700000000/visitor_photos/xxx.jpg
        String afterUpload = imageUrl.substring(markerIndex + uploadMarker.length());

        // Hilangkan segment versi awal "v<digits>/"
        afterUpload = afterUpload.replaceFirst("^v\\d+/", "");

        // Hilangkan ekstensi file, mis. ".jpg"
        afterUpload = afterUpload.replaceAll("\\.[a-zA-Z0-9]+$", "");

        // Hasil akhir harus seperti: "visitor_photos/xxx"
        return afterUpload;
    }

    private byte[] readAllBytes(Path path) throws IOException {
        // Helper untuk baca file lokal jadi byte array
        return Files.readAllBytes(path);
    }

    @Test
    @DisplayName("registerVisitor error (parameter fullName hilang -> 400)")
    void registerVisitor_missingFullName_returnsBadRequest() throws Exception {
        // Pastikan file ada supaya test error-nya hanya karena parameter, bukan karena file.
        assertTrue(Files.exists(localPhotoPath), "File foto test tidak ditemukan: " + localPhotoPath);

        // Baca byte foto untuk multipart request
        byte[] photoBytes = readAllBytes(localPhotoPath);

        // Tentukan contentType
        String contentType = Files.probeContentType(localPhotoPath);
        if (contentType == null || contentType.isBlank()) {
            contentType = "image/jpeg";
        }

        // Siapkan multipart photo
        MockMultipartFile photo = new MockMultipartFile(
            "photo",
            localPhotoPath.getFileName().toString(),
            contentType,
            photoBytes
        );

        // Request ini sengaja TIDAK mengirim parameter `fullName`
        // Karena controller mendefinisikan @RequestParam("fullName") tanpa required=false
        mockMvc.perform(multipart("/api/visitors/register")
                .file(photo)
                .param("nik", testNik)
                .param("purpose", "Meeting QA"))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }
}
