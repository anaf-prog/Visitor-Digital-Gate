package com.vigi.gate.views.component;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class GridPagination<T> {

    private final Grid<T> grid;
    private final int pageSize;
    private final List<T> allData = new ArrayList<>();
    private int currentPage = 0;

    // Komponen UI
    private final HorizontalLayout paginationLayout = new HorizontalLayout();
    private final Button prevBtn = new Button("Sebelumnya");
    private final Button nextBtn = new Button("Berikutnya");
    private final Span pageInfo = new Span();

    public GridPagination(Grid<T> grid, int pageSize) {
        this.grid = grid;
        this.pageSize = pageSize;
        setupPaginationLayout();
    }

    public GridPagination(Grid<T> grid) {
        this(grid, 10); // Default ke 10 item per halaman jika tidak ditentukan
    }

    private void setupPaginationLayout() {
        prevBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        prevBtn.getStyle()
            .set("background-color", "rgba(0, 255, 102, 0.1)")
            .set("color", "#00ff66")
            .set("border", "1px solid rgba(0, 255, 102, 0.3)")
            .set("font-weight", "600")
            .set("cursor", "pointer");

        nextBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        nextBtn.getStyle()
            .set("background-color", "rgba(0, 255, 102, 0.1)")
            .set("color", "#00ff66")
            .set("border", "1px solid rgba(0, 255, 102, 0.3)")
            .set("font-weight", "600")
            .set("cursor", "pointer");

        pageInfo.getStyle()
            .set("color", "#9ca3af")
            .set("font-size", "14px")
            .set("font-weight", "600")
            .set("margin", "0 15px");

        paginationLayout.setWidthFull();
        paginationLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        paginationLayout.setAlignItems(Alignment.CENTER);
        paginationLayout.getStyle().set("margin-top", "16px");

        paginationLayout.add(prevBtn, pageInfo, nextBtn);

        prevBtn.addClickListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                updateGridPage();
            }
        });

        nextBtn.addClickListener(e -> {
            int totalPages = (int) Math.ceil((double) allData.size() / pageSize);
            if (currentPage < totalPages - 1) {
                currentPage++;
                updateGridPage();
            }
        });

        paginationLayout.setVisible(false);
    }

    public void setData(List<T> data) {
        this.allData.clear();
        if (data != null) {
            this.allData.addAll(data);
        }
        updateGridPage();
    }

    private void updateGridPage() {
        int totalDataSize = allData.size();

        if (totalDataSize > pageSize) {
            paginationLayout.setVisible(true);

            int totalPages = (int) Math.ceil((double) totalDataSize / pageSize);
            
            if (currentPage >= totalPages) {
                currentPage = Math.max(0, totalPages - 1);
            }

            int fromIndex = currentPage * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, totalDataSize);

            List<T> pageData = allData.subList(fromIndex, toIndex);
            grid.setItems(pageData);

            pageInfo.setText(String.format("Halaman %d dari %d", currentPage + 1, totalPages));
            prevBtn.setEnabled(currentPage > 0);
            nextBtn.setEnabled(currentPage < totalPages - 1);
        } else {
            paginationLayout.setVisible(false);
            grid.setItems(allData);
            currentPage = 0;
        }
    }

    public HorizontalLayout getLayout() {
        return paginationLayout;
    }
    
}
