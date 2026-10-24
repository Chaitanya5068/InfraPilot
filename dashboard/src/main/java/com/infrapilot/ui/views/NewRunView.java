package com.infrapilot.ui.views;

import com.infrapilot.ui.MainLayout;
import com.infrapilot.ui.client.InfraPilotApiClient;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.Map;

@Route(value = "new-run", layout = MainLayout.class)
@PageTitle("New Run — InfraPilot")
public class NewRunView extends VerticalLayout {

    private final InfraPilotApiClient apiClient;

    public NewRunView(InfraPilotApiClient apiClient) {
        this.apiClient = apiClient;
        setPadding(true);
        setSpacing(true);
        setMaxWidth("800px");

        H2 title = new H2("Trigger New Pipeline Execution");
        Paragraph desc = new Paragraph("Select the bundled demo application or onboard any GitHub / GitLab repository.");
        desc.getStyle().set("color", "var(--lumo-secondary-text-color)");

        RadioButtonGroup<String> modeRadio = new RadioButtonGroup<>();
        modeRadio.setLabel("Application Source");
        modeRadio.setItems("Use demo app", "Use my repo");
        modeRadio.setValue("Use demo app");

        TextField repoUrlField = new TextField("Repository URL");
        repoUrlField.setWidthFull();
        repoUrlField.setValue("https://github.com/infrapilot/demo-app");
        repoUrlField.setEnabled(false);

        TextField branchField = new TextField("Git Branch");
        branchField.setValue("main");
        branchField.setWidthFull();

        ComboBox<String> envSelect = new ComboBox<>("Target Environment");
        envSelect.setItems("dev", "staging", "prod");
        envSelect.setValue("dev");
        envSelect.setWidthFull();

        ComboBox<String> ciSelect = new ComboBox<>("Orchestration Pipeline");
        ciSelect.setItems("Jenkins (Orchestrated)", "GitHub Actions", "GitLab CI");
        ciSelect.setValue("Jenkins (Orchestrated)");
        ciSelect.setWidthFull();

        // Readiness report preview card
        VerticalLayout readinessCard = new VerticalLayout();
        readinessCard.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "8px")
                .set("padding", "1rem")
                .set("margin-top", "1rem");

        H3 reportTitle = new H3("Repository Readiness & Stack Inspection");
        reportTitle.getStyle().set("font-size", "1rem").set("margin", "0 0 0.5rem 0");

        VerticalLayout notesList = new VerticalLayout();
        notesList.setPadding(false);
        notesList.setSpacing(false);

        Runnable updateInspection = () -> {
            notesList.removeAll();
            boolean isDemo = "Use demo app".equals(modeRadio.getValue());
            String url = repoUrlField.getValue();
            String branch = branchField.getValue();

            Map<String, Object> analysis = apiClient.createProjectAndAnalyze(url, branch, isDemo);
            List<String> notes = (List<String>) analysis.getOrDefault("readinessReport", List.of());
            for (String note : notes) {
                HorizontalLayout row = new HorizontalLayout(VaadinIcon.CHECK_CIRCLE.create(), new Span(note));
                row.getStyle().set("font-size", "0.85rem").set("color", "var(--lumo-success-text-color)");
                notesList.add(row);
            }
        };

        readinessCard.add(reportTitle, notesList);
        updateInspection.run();

        modeRadio.addValueChangeListener(e -> {
            if ("Use demo app".equals(e.getValue())) {
                repoUrlField.setValue("https://github.com/infrapilot/demo-app");
                repoUrlField.setEnabled(false);
            } else {
                repoUrlField.setValue("");
                repoUrlField.setPlaceholder("https://github.com/your-org/your-service");
                repoUrlField.setEnabled(true);
            }
            updateInspection.run();
        });

        repoUrlField.addValueChangeListener(e -> updateInspection.run());

        Button startBtn = new Button("Start Pipeline Run", VaadinIcon.ROCKET.create(), e -> {
            boolean isDemo = "Use demo app".equals(modeRadio.getValue());
            String ciSystem = ciSelect.getValue().toLowerCase().contains("github") ? "github_actions" :
                    (ciSelect.getValue().toLowerCase().contains("gitlab") ? "gitlab_ci" : "jenkins");

            Map<String, Object> run = apiClient.startRun(null, envSelect.getValue(), ciSystem);

            Notification.show("Pipeline started successfully! Run ID: " + run.get("id"), 4000, Notification.Position.TOP_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            UI.getCurrent().navigate(PipelineView.class);
        });
        startBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        startBtn.getStyle().set("margin-top", "1rem");

        add(title, desc, modeRadio, repoUrlField, branchField, envSelect, ciSelect, readinessCard, startBtn);
    }
}
