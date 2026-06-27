package com.vigi.gate.views.component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

public class GridSearchFilter<T> {

    private final HorizontalLayout filterLayout = new HorizontalLayout();
    private final TextField nameField = new TextField();
    private final TextField nikField = new TextField();
    private final Button resetBtn = new Button("Reset");

    private final List<T> originalData = new ArrayList<>();
    private final Consumer<List<T>> updateCallback;
    private final FilterPredicate<T> filterPredicate;

    @FunctionalInterface
    public interface FilterPredicate<T> {
        boolean test(T item, String nameQuery, String nikQuery);
    }

    public GridSearchFilter(Consumer<List<T>> updateCallback, FilterPredicate<T> filterPredicate) {
        this.updateCallback = updateCallback;
        this.filterPredicate = filterPredicate;
        setupUI();
    }

    private void setupUI() {
        // Pengaturan Input Pencarian Nama
        nameField.setPlaceholder("Cari Nama...");
        nameField.setClearButtonVisible(true);
        nameField.setValueChangeMode(ValueChangeMode.LAZY);
        nameField.setValueChangeTimeout(400); // Menunda pencarian selama 400ms setelah mengetik selesai
        applyDarkStyle(nameField);

        // Pengaturan Input Pencarian NIK
        nikField.setPlaceholder("Cari NIK...");
        nikField.setClearButtonVisible(true);
        nikField.setValueChangeMode(ValueChangeMode.LAZY);
        nikField.setValueChangeTimeout(400);
        applyDarkStyle(nikField);

        // Tombol Reset Pencarian
        resetBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        resetBtn.getStyle()
            .set("background-color", "rgba(239, 68, 68, 0.1)")
            .set("color", "#ef4444")
            .set("border", "1px solid rgba(239, 68, 68, 0.3)")
            .set("font-weight", "600")
            .set("cursor", "pointer");

        // Penggabungan komponen ke dalam tata letak horizontal
        filterLayout.add(nameField, nikField, resetBtn);
        filterLayout.setAlignItems(Alignment.CENTER);
        filterLayout.setSpacing(true);

        // Event listener saat input berubah
        nameField.addValueChangeListener(e -> applyFilter());
        nikField.addValueChangeListener(e -> applyFilter());
        
        resetBtn.addClickListener(e -> {
            nameField.clear();
            nikField.clear();
            applyFilter();
        });
    }

    private void applyDarkStyle(TextField textField) {
        textField.getStyle()
            .set("--lumo-contrast-10pct", "rgba(255, 255, 255, 0.05)")
            .set("--lumo-body-text-color", "#f3f4f6");
    }

    public void setOriginalData(List<T> data) {
        this.originalData.clear();
        if (data != null) {
            this.originalData.addAll(data);
        }
        applyFilter(); // Terapkan pencarian yang sedang aktif ke data baru
    }

    public void applyFilter() {
        String nameQuery = nameField.getValue() != null ? nameField.getValue().trim().toLowerCase() : "";
        String nikQuery = nikField.getValue() != null ? nikField.getValue().trim().toLowerCase() : "";

        if (nameQuery.isEmpty() && nikQuery.isEmpty()) {
            updateCallback.accept(originalData);
        } else {
            List<T> filtered = originalData.stream()
                .filter(item -> filterPredicate.test(item, nameQuery, nikQuery))
                .collect(Collectors.toList());
            updateCallback.accept(filtered);
        }
    }

    public HorizontalLayout getLayout() {
        return filterLayout;
    }
    
}
