package com.infrapilot.ui.views;

import com.infrapilot.ui.MainLayout;
import com.infrapilot.ui.client.InfraPilotApiClient;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "security", layout = MainLayout.class)
@PageTitle("Security Scans — InfraPilot")
public class SecurityView extends VerticalLayout {

    public record Finding(String tool, String id, String severity, String description, String fileLocation) {}

    public SecurityView(InfraPilotApiClient apiClient) {
        setPadding(true);
        setSpacing(true);
        setWidthFull();

        H2 heading = new H2("Security & Quality Gates");
        Paragraph sub = new Paragraph("Continuous shift-left security enforcement across secrets, dependencies, IaC, and container images.");
        sub.getStyle().set("color", "var(--lumo-secondary-text-color)");

        // 4 Tool Cards
        HorizontalLayout cardsRow = new HorizontalLayout();
        cardsRow.setWidthFull();
        cardsRow.setSpacing(true);

        cardsRow.add(
                createToolCard("Gitleaks", "Secret Scan", 0, 0, 0, 0, true),
                createToolCard("Trivy", "Filesystem & Images", 0, 0, 1, 3, true),
                createToolCard("Checkov", "Terraform IaC", 0, 0, 2, 1, true),
                createToolCard("SonarQube", "Code Quality & Bugs", 0, 0, 0, 4, true)
        );

        H3 findingsHeading = new H3("Identified Vulnerabilities & Policy Findings");
        findingsHeading.getStyle().set("margin-top", "1.5rem");

        Grid<Finding> grid = new Grid<>();
        grid.setWidthFull();

        grid.addColumn(Finding::tool).setHeader("Scanner").setAutoWidth(true);
        grid.addColumn(Finding::id).setHeader("Finding ID").setAutoWidth(true);
        grid.addComponentColumn(f -> {
            Span badge = new Span(f.severity());
            switch (f.severity()) {
                case "CRITICAL" -> badge.getElement().getThemeList().add("badge error");
                case "HIGH" -> badge.getElement().getThemeList().add("badge error");
                case "MEDIUM" -> badge.getElement().getThemeList().add("badge warning");
                default -> badge.getElement().getThemeList().add("badge small");
            }
            return badge;
        }).setHeader("Severity").setAutoWidth(true);
        grid.addColumn(Finding::description).setHeader("Description").setFlexGrow(2);
        grid.addColumn(Finding::fileLocation).setHeader("File / Resource").setAutoWidth(true);

        grid.setItems(List.of(
                new Finding("Trivy", "CVE-2023-4586", "MEDIUM", "Netty HTTP client pipeline issue (CVSS 5.3)", "pom.xml"),
                new Finding("Checkov", "CKV_AWS_135", "MEDIUM", "Ensure EBS volume is encrypted", "infrastructure/templates/single-host/main.tf:46"),
                new Finding("Checkov", "CKV_AWS_260", "MEDIUM", "Ensure no security groups allow ingress from 0.0.0.0:0 to port 22", "infrastructure/templates/single-host/main.tf:38"),
                new Finding("SonarQube", "squid:S1186", "LOW", "Add a nested comment explaining why this method is empty", "src/main/java/com/demoapp/service/TaskService.java:42")
        ));

        add(heading, sub, cardsRow, findingsHeading, grid);
    }

    private VerticalLayout createToolCard(String name, String type, int crit, int high, int med, int low, boolean passed) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "10px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.04)");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Span title = new Span(name);
        title.getStyle().set("font-weight", "700").set("font-size", "1.1rem");

        Span status = new Span(passed ? "PASSED" : "FAILED");
        status.getElement().getThemeList().add(passed ? "badge success small" : "badge error small");

        header.add(title, status);

        Span subtitle = new Span(type);
        subtitle.getStyle().set("font-size", "0.8rem").set("color", "var(--lumo-secondary-text-color)").set("margin-bottom", "1rem");

        HorizontalLayout badges = new HorizontalLayout();
        badges.setSpacing(true);

        Span cBadge = new Span("C: " + crit);
        cBadge.getElement().getThemeList().add("badge error small");

        Span hBadge = new Span("H: " + high);
        hBadge.getElement().getThemeList().add("badge error small");

        Span mBadge = new Span("M: " + med);
        mBadge.getElement().getThemeList().add("badge warning small");

        Span lBadge = new Span("L: " + low);
        lBadge.getElement().getThemeList().add("badge small");

        badges.add(cBadge, hBadge, mBadge, lBadge);

        card.add(header, subtitle, badges);
        return card;
    }
}
