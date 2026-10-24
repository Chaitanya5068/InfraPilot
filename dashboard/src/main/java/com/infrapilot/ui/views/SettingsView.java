package com.infrapilot.ui.views;

import com.infrapilot.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "settings", layout = MainLayout.class)
@PageTitle("Settings — InfraPilot")
public class SettingsView extends VerticalLayout {

    public SettingsView() {
        setPadding(true);
        setSpacing(true);
        setMaxWidth("800px");

        H2 heading = new H2("Platform Governance & Integration Settings");
        Paragraph sub = new Paragraph("Manage CI/CD platform credentials, quality gate thresholds, and alerting webhooks.");
        sub.getStyle().set("color", "var(--lumo-secondary-text-color)");

        H3 tokenSection = new H3("1. CI/CD Access Tokens");
        PasswordField jenkinsToken = new PasswordField("Jenkins API Token");
        jenkinsToken.setValue("••••••••••••••••••••");
        jenkinsToken.setWidthFull();

        PasswordField githubToken = new PasswordField("GitHub Personal Access Token (repo, workflow)");
        githubToken.setValue("••••••••••••••••••••");
        githubToken.setWidthFull();

        H3 thresholdSection = new H3("2. Quality & Security Gate Thresholds");
        IntegerField maxCrit = new IntegerField("Max Critical Vulnerabilities Allowed");
        maxCrit.setValue(0);
        maxCrit.setHelperText("Build immediately aborts if any CRITICAL vulnerability is detected.");

        IntegerField maxHigh = new IntegerField("Max High Vulnerabilities Allowed");
        maxHigh.setValue(5);

        IntegerField minCoverage = new IntegerField("Minimum Code Coverage (%)");
        minCoverage.setValue(60);

        H3 alertSection = new H3("3. Incident & Alerting Channels");
        TextField slackWebhook = new TextField("Slack Incoming Webhook URL");
        slackWebhook.setPlaceholder("https://hooks.slack.com/services/T00/B00/XXXXX");
        slackWebhook.setWidthFull();

        TextField emailAlerts = new TextField("Alert Notification Recipient Email");
        emailAlerts.setValue("devops-alerts@infrapilot.io");
        emailAlerts.setWidthFull();

        Button saveBtn = new Button("Save Configuration", e -> {
            Notification.show("Platform settings updated successfully", 3000, Notification.Position.TOP_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.getStyle().set("margin-top", "1rem");

        add(heading, sub, tokenSection, jenkinsToken, githubToken, thresholdSection, maxCrit, maxHigh, minCoverage, alertSection, slackWebhook, emailAlerts, saveBtn);
    }
}
