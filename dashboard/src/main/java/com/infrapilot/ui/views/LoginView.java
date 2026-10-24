package com.infrapilot.ui.views;

import com.infrapilot.ui.client.InfraPilotApiClient;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.util.Map;

@Route("login")
@PageTitle("Login — InfraPilot")
public class LoginView extends VerticalLayout {

    public LoginView(InfraPilotApiClient apiClient) {
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        VerticalLayout card = new VerticalLayout();
        card.setWidth("380px");
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "12px")
                .set("padding", "2rem")
                .set("box-shadow", "0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1)");

        H2 title = new H2("InfraPilot");
        title.getStyle().set("margin", "0").set("color", "var(--lumo-primary-color)");

        Paragraph subtitle = new Paragraph("Post-Development Post-Deployment Control Plane");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.85rem");

        TextField emailField = new TextField("Email");
        emailField.setValue("devops@infrapilot.io");
        emailField.setWidthFull();

        PasswordField passwordField = new PasswordField("Password");
        passwordField.setValue("password123");
        passwordField.setWidthFull();

        Button loginBtn = new Button("Sign In", event -> {
            String email = emailField.getValue().trim();
            String pwd = passwordField.getValue();

            Map<String, Object> resp = apiClient.login(email, pwd);
            if (resp != null && resp.containsKey("accessToken")) {
                VaadinSession.getCurrent().setAttribute("jwt_token", resp.get("accessToken"));
                VaadinSession.getCurrent().setAttribute("user_email", resp.get("email"));
                VaadinSession.getCurrent().setAttribute("user_role", resp.get("role"));

                Notification.show("Welcome back, " + resp.get("email"), 3000, Notification.Position.TOP_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                UI.getCurrent().navigate(DashboardView.class);
            } else {
                Notification.show("Invalid credentials", 4000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        loginBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginBtn.setWidthFull();

        Span hint = new Span("Demo Accounts: devops@infrapilot.io / admin@infrapilot.io (pw: password123)");
        hint.getStyle().set("font-size", "0.75rem").set("color", "var(--lumo-tertiary-text-color)").set("margin-top", "1rem");

        card.add(title, subtitle, emailField, passwordField, loginBtn, hint);
        add(card);
    }
}
