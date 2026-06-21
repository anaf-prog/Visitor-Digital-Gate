package com.vigi.gate.dto;

import com.vigi.gate.enumlevel.RiskLevel;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RiskRuleForm {

    @NotBlank(message = "Nama rule wajib diisi")
    private String ruleName;

    @NotBlank(message = "Tipe kondisi wajib diisi")
    private String conditionType;

    @NotBlank(message = "Nilai kondisi wajib diisi")
    private String conditionValue;

    @NotNull(message = "Risk level wajib dipilih")
    private RiskLevel riskLevel;

    @NotNull(message = "Skor wajib diisi")
    @Min(value = 0, message = "Skor minimal 0")
    @Max(value = 100, message = "Skor maksimal 100")
    private Integer scoreContribution;

    private boolean active = true;
}
