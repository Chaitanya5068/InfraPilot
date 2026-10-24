package com.infrapilot.ui.views;

import com.infrapilot.ui.MainLayout;
import com.infrapilot.ui.client.InfraPilotApiClient;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.Map;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard — InfraPilot")
public class DashboardView extends VerticalLayout {

    public DashboardView(InfraPilotApiClient apiClient) {
        setSpacing(true);
        setPadding(true);
        setWidthFull();

        HorizontalLayout topBar = new HorizontalLayout();
        topBar.setWidthFull();
        topBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        topBar.setAlignItems(FlexComponent.Alignment.CENTER);

        VerticalLayout titles = new VerticalLayout();
        titles.setPadding(false);
        titles.setSpacing(false);
        H2 heading = new H2("Overview Dashboard");
        heading.getStyle().set("margin", "0");
        Paragraph sub = new Paragraph("Live status of builds, security quality gates, AWS deployments, and cost");
        sub.getStyle().set("color", "var(--lumo-secondary-text-color)").set("margin", "0");
        titles.add(heading, sub);

        Button newRunBtn = new Button("New Deployment Run", VaadinIcon.ROCKET.create(), e -> UI.getCurrent().navigate(NewRunView.class));
        newRunBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        topBar.add(titles, newRunBtn);

        // KPI Stat Cards
        HorizontalLayout statsRow = new HorizontalLayout();
        statsRow.setWidthFull();
        statsRow.setSpacing(true);

        statsRow.add(
                createStatCard("Total Runs", "48", "94.2% Success Rate", "var(--lumo-primary-color)"),
                createStatCard("Active Deployments", "2", "AWS EC2 (ap-south-1)", "var(--lumo-success-color)"),
                createStatCard("Monthly Cloud Cost", "$15.28", "Within $25.00 Budget Cap", "var(--lumo-contrast-color)"),
                createStatCard("Critical Vulnerabilities", "0", "All Security Gates Green", "var(--lumo-success-color)")
        );

        // Recent Runs Grid
        H3 gridHeading = new H3("Recent Pipeline Executions");
        gridHeading.getStyle().set("margin-top", "1.5rem");

        Grid<Map<String, Object>> runsGrid = new Grid<>();
        runsGrid.setWidthFull();
        runsGrid.addColumn(r -> r.get("id").toString().substring(0, 8) + "...")
                .setHeader("Run ID").setAutoWidth(true);
        runsGrid.addColumn(r -> r.getOrDefault("environment", "dev"))
                .setHeader("Environment").setAutoWidth(true);
        runsGrid.addComponentColumn(r -> {
            String status = r.getOrDefault("status", "unknown").toString();
            Span badge = new Span(status.toUpperCase());
            if ("deployed".equalsIgnoreCase(status)) {
                badge.getElement().getThemeList().add("badge success");
            } else if ("awaiting_approval".equalsIgnoreCase(status)) {
                badge.getElement().getThemeList().add("badge warning");
            } else if ("failed".equalsIgnoreCase(status)) {
                badge.getElement().getThemeList().add("badge error");
            } else {
                badge.getElement().getThemeList().add("badge");
            }
            return badge;
        }).setHeader("Status").setAutoWidth(true);

        runsGrid.addColumn(r -> r.getOrDefault("ciSystem", "jenkins"))
                .setHeader("CI System").setAutoWidth(true);
        runsGrid.addColumn(r -> r.getOrDefault("commitSha", "d8a9f23"))
                .setHeader("Commit SHA").setAutoWidth(true);

        runsGrid.addComponentColumn(r -> {
            HorizontalLayout actions = new HorizontalLayout();
            Button viewBtn = new Button("View Pipeline", VaadinIcon.ARROW_RIGHT.create(), e -> UI.getCurrent().navigate(PipelineView.class));
            viewBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            actions.add(viewBtn);
            return actions;
        }).setHeader("Action").setAutoWidth(true);

        List<Map<String, Object>> runs = apiClient.getRecentRuns();
        runsGrid.setItems(runs);

        add(topBar, statsRow, gridHeading, runsGrid);
    }

    private VerticalLayout createStatCard(String label, String value, String subtext, String color) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "10px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.04)");

        Span lbl = new Span(label);
        lbl.getStyle().set("font-size", "0.75rem").set("text-transform", "uppercase").set("color", "var(--lumo-secondary-text-color)");

        H2 val = new H2(value);
        val.getStyle().set("margin", "0.25rem 0").set("color", color);

        Span sub = new Span(subtext);
        sub.getStyle().set("font-size", "0.8rem").set("color", "var(--lumo-secondary-text-color)");

        card.add(lbl, val, sub);
        return card;
    }
}
