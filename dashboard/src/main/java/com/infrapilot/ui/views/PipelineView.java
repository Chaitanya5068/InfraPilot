package com.infrapilot.ui.views;

import com.infrapilot.ui.MainLayout;
import com.infrapilot.ui.client.InfraPilotApiClient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "pipeline", layout = MainLayout.class)
@PageTitle("Pipeline — InfraPilot")
public class PipelineView extends VerticalLayout {

    private final InfraPilotApiClient apiClient;
    private final VerticalLayout stagesContainer = new VerticalLayout();

    public record StageItem(String name, String status, int durationSec, String category) {}

    public PipelineView(InfraPilotApiClient apiClient) {
        this.apiClient = apiClient;
        setPadding(true);
        setSpacing(true);
        setWidthFull();

        HorizontalLayout topBar = new HorizontalLayout();
        topBar.setWidthFull();
        topBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        topBar.setAlignItems(FlexComponent.Alignment.CENTER);

        VerticalLayout titles = new VerticalLayout();
        titles.setPadding(false);
        titles.setSpacing(false);
        H2 heading = new H2("Active Pipeline Execution");
        heading.getStyle().set("margin", "0");
        Paragraph sub = new Paragraph("Run ID: bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb • Environment: dev • CI: Jenkins");
        sub.getStyle().set("color", "var(--lumo-secondary-text-color)").set("margin", "0");
        titles.add(heading, sub);

        // DevOps Approval Actions
        HorizontalLayout approvalControls = new HorizontalLayout();
        Button approveBtn = new Button("Approve Terraform Apply", VaadinIcon.CHECK.create(), e -> {
            apiClient.approveRun("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
            Notification.show("Stage approved! Deployment initiated.", 3000, Notification.Position.TOP_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        approveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        Button rejectBtn = new Button("Reject", VaadinIcon.CLOSE.create(), e -> {
            apiClient.rejectRun("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
            Notification.show("Pipeline aborted.", 3000, Notification.Position.TOP_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
        rejectBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button destroyBtn = new Button("Destroy Stack", VaadinIcon.TRASH.create(), e -> {
            apiClient.destroyRun("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
            Notification.show("Terraform destroy initiated.", 3000, Notification.Position.TOP_END)
                    .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        });

        approvalControls.add(approveBtn, rejectBtn, destroyBtn);
        topBar.add(titles, approvalControls);

        stagesContainer.setWidthFull();
        stagesContainer.setPadding(false);
        stagesContainer.setSpacing(true);

        renderStages();

        add(topBar, stagesContainer);
    }

    private void renderStages() {
        stagesContainer.removeAll();

        List<StageItem> stages = List.of(
                new StageItem("Checkout & Git Pull", "passed", 4, "Source"),
                new StageItem("Read infrapilot.yml Contract", "passed", 1, "Config"),
                new StageItem("Maven Compile & Build", "passed", 42, "Build"),
                new StageItem("JUnit 5 Unit Tests", "passed", 18, "Test"),
                new StageItem("SonarQube Quality Gate", "passed", 35, "Quality"),
                new StageItem("Gitleaks Secret Scan", "passed", 6, "Security"),
                new StageItem("Trivy Filesystem & Deps Scan", "passed", 28, "Security"),
                new StageItem("Checkov Infrastructure Scan", "passed", 14, "Security"),
                new StageItem("Docker Image Build", "passed", 52, "Package"),
                new StageItem("Trivy Image Vulnerability Scan", "passed", 24, "Security"),
                new StageItem("Infracost Monthly Cost Analysis", "passed", 9, "FinOps"),
                new StageItem("Terraform Plan Generation", "passed", 15, "IaC"),
                new StageItem("DevOps Production Gate", "passed", 5, "Approval"),
                new StageItem("Terraform Apply (EC2 & VPC)", "passed", 80, "Provision"),
                new StageItem("Ansible Compose Deployment", "passed", 32, "Deploy"),
                new StageItem("Newman API & k6 Load Tests", "passed", 36, "Verification")
        );

        for (StageItem stage : stages) {
            HorizontalLayout row = new HorizontalLayout();
            row.setWidthFull();
            row.setAlignItems(FlexComponent.Alignment.CENTER);
            row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
            row.getStyle()
                    .set("background", "var(--lumo-base-color)")
                    .set("border", "1px solid var(--lumo-contrast-10pct)")
                    .set("border-radius", "8px")
                    .set("padding", "0.75rem 1.25rem");

            HorizontalLayout left = new HorizontalLayout();
            left.setAlignItems(FlexComponent.Alignment.CENTER);

            Icon icon = VaadinIcon.CHECK_CIRCLE.create();
            icon.setColor("var(--lumo-success-color)");

            Span stageName = new Span(stage.name());
            stageName.getStyle().set("font-weight", "600");

            Span categoryBadge = new Span(stage.category());
            categoryBadge.getElement().getThemeList().add("badge small");

            left.add(icon, stageName, categoryBadge);

            HorizontalLayout right = new HorizontalLayout();
            right.setAlignItems(FlexComponent.Alignment.CENTER);

            Span duration = new Span(stage.durationSec() + "s");
            duration.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.85rem");

            Span statusBadge = new Span("PASSED");
            statusBadge.getElement().getThemeList().add("badge success small");

            right.add(duration, statusBadge);

            row.add(left, right);
            stagesContainer.add(row);
        }
    }
}
