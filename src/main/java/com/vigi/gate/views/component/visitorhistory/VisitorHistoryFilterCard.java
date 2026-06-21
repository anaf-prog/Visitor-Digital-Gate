package com.vigi.gate.views.component.visitorhistory;

import java.time.LocalTime;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vigi.gate.dto.VisitorLogResponse;
import com.vigi.gate.views.VistorHistoryView;
import com.vigi.gate.views.component.BaseCard;

public class VisitorHistoryFilterCard extends BaseCard {

    private final ListDataProvider<VisitorLogResponse> dataProvider;
    private final VistorHistoryView vistorHistoryView;

    // Komponen Input Filter Data
    private final TextField filterNama = new TextField("Nama");
    private final TextField filterNik = new TextField("NIK");
    private final TextField filterTujuan = new TextField("Tujuan");
    private final TimePicker filterCheckin = new TimePicker("Jam Checkin");

    public VisitorHistoryFilterCard(ListDataProvider<VisitorLogResponse> dataProvider, VistorHistoryView vistorHistoryView) {
        super(""); // Kosongkan karena memakai H3 custom styling di bawah
        this.dataProvider = dataProvider;
        this.vistorHistoryView = vistorHistoryView;

        H3 filterTitle = new H3("Filter Data");
        filterTitle.getStyle().set("margin-top", "0");
        add(filterTitle);

        filterNama.setPlaceholder("Cari berdasarkan nama");
        filterNik.setPlaceholder("Cari berdasarkan NIK");
        filterTujuan.setPlaceholder("Cari berdasarkan tujuan");
        filterCheckin.setPlaceholder("HH:MM");
        filterCheckin.setClearButtonVisible(true);

        // Aksi Tombol Terapkan dan Hapus Filter
        Button applyFilterBtn = new Button("Terapkan Filter", event -> applyDataFilters());
        applyFilterBtn.getStyle().set("background-color", "#2563eb").set("color", "#fff");
        
        Button clearFilterBtn = new Button("Hapus Filter", event -> clearDataFilters());
        clearFilterBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearFilterBtn.getStyle().set("background-color", "#f3f4f6").set("color", "#374151").set("border", "1px solid #d1d5db");

        HorizontalLayout filterActions = new HorizontalLayout(applyFilterBtn, clearFilterBtn);
        filterActions.getStyle().set("margin-top", "auto");

        FormLayout filterGrid = new FormLayout(filterNama, filterNik, filterTujuan, filterCheckin, filterActions);
        filterGrid.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("600px", 2),
            new FormLayout.ResponsiveStep("900px", 5)
        );
        add(filterGrid);

        setupFilterEnterListeners();
    }

    public void applyDataFilters() {
        dataProvider.clearFilters();

        String nameSearch = filterNama.getValue().trim().toLowerCase();
        String nikSearch = filterNik.getValue().trim();
        String purposeSearch = filterTujuan.getValue().trim().toLowerCase();
        LocalTime timeSearch = filterCheckin.getValue();

        if (!nameSearch.isEmpty()) {
            dataProvider.addFilter(res -> res.getFullName() != null && res.getFullName().toLowerCase().contains(nameSearch));
        }
        if (!nikSearch.isEmpty()) {
            dataProvider.addFilter(res -> res.getNik() != null && res.getNik().contains(nikSearch));
        }
        if (!purposeSearch.isEmpty()) {
            dataProvider.addFilter(res -> res.getPurpose() != null && res.getPurpose().toLowerCase().contains(purposeSearch));
        }
        if (timeSearch != null) {
            dataProvider.addFilter(res -> {
                if (res.getCheckinTime() == null) return false;
                LocalTime checkinLocalTime = res.getCheckinTime().toLocalTime();
                return checkinLocalTime.getHour() == timeSearch.getHour() && checkinLocalTime.getMinute() == timeSearch.getMinute();
            });
        }

        // Ambil jumlah data yang lolos kriteria pencarian dan kirimkan informasinya ke layout tabel induk
        int filteredSize = dataProvider.getItems().size();
        vistorHistoryView.updateTableRecordCount(filteredSize + " data");
    }

    private void clearDataFilters() {
        filterNama.clear();
        filterNik.clear();
        filterTujuan.clear();
        filterCheckin.clear();
        applyDataFilters();
    }

    private void setupFilterEnterListeners() {
        filterNama.addKeyPressListener(com.vaadin.flow.component.Key.ENTER, event -> applyDataFilters());
        filterNik.addKeyPressListener(com.vaadin.flow.component.Key.ENTER, event -> applyDataFilters());
        filterTujuan.addKeyPressListener(com.vaadin.flow.component.Key.ENTER, event -> applyDataFilters());
    }
    
}
