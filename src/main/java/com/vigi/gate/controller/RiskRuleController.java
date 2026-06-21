package com.vigi.gate.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vigi.gate.dto.RiskRuleForm;
import com.vigi.gate.enumlevel.RiskLevel;
import com.vigi.gate.service.RiskRuleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/risk-rules")
@RequiredArgsConstructor
public class RiskRuleController {

    private final RiskRuleService riskRuleService;

    @GetMapping
    public String list(Model model) {
        if (!model.containsAttribute("riskRuleForm")) {
            model.addAttribute("riskRuleForm", new RiskRuleForm());
        }
        model.addAttribute("rules", riskRuleService.getAllRules());
        model.addAttribute("riskLevels", RiskLevel.values());
        model.addAttribute("conditionTypes", new String[] { "TIME", "FREQUENCY" });
        model.addAttribute("editMode", false);
        return "risk-rule";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("riskRuleForm") RiskRuleForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("rules", riskRuleService.getAllRules());
            model.addAttribute("riskLevels", RiskLevel.values());
            model.addAttribute("conditionTypes", new String[] { "TIME", "FREQUENCY" });
            model.addAttribute("editMode", false);
            return "risk-rule";
        }

        riskRuleService.createRule(form);
        redirectAttributes.addFlashAttribute("successMessage", "Risk rule berhasil ditambahkan.");
        return "redirect:/risk-rules";
    }

    @GetMapping("/{id}/edit")
    public String editPage(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("riskRuleForm", riskRuleService.toForm(riskRuleService.getRuleById(id)));
            model.addAttribute("rules", riskRuleService.getAllRules());
            model.addAttribute("riskLevels", RiskLevel.values());
            model.addAttribute("conditionTypes", new String[] { "TIME", "FREQUENCY" });
            model.addAttribute("editMode", true);
            model.addAttribute("editId", id);
            return "risk-rule";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/risk-rules";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("riskRuleForm") RiskRuleForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("rules", riskRuleService.getAllRules());
            model.addAttribute("riskLevels", RiskLevel.values());
            model.addAttribute("conditionTypes", new String[] { "TIME", "FREQUENCY" });
            model.addAttribute("editMode", true);
            model.addAttribute("editId", id);
            return "risk-rule";
        }

        try {
            riskRuleService.updateRule(id, form);
            redirectAttributes.addFlashAttribute("successMessage", "Risk rule berhasil diubah.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/risk-rules";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            riskRuleService.deleteRule(id);
            redirectAttributes.addFlashAttribute("successMessage", "Risk rule berhasil dihapus.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/risk-rules";
    }
}
