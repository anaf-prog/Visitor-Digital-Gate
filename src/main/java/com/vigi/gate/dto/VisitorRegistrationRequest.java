package com.vigi.gate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VisitorRegistrationRequest {

    @NotBlank(message = "Nama wajib diisi")
    private String fullName;

    @NotBlank(message = "NIK wajib diisi")
    @Pattern(regexp = "\\d{16}", message = "NIK harus 16 digit angka")
    private String nik;

    @NotBlank(message = "Tujuan kunjungan wajib diisi")
    private String purpose;

    private String photo;
}
