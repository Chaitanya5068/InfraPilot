package com.infrapilot.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Sign in | InfraPilot")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    public LoginView() {
        TextField username = new TextField("Username");
        username.setAutocomplete("username");
        PasswordField password = new PasswordField("Password");
        password.setAutocomplete("current-password");
        Button signIn = new Button("Continue", event -> getUI().ifPresent(ui -> ui.navigate("")));

        add(
                new H1("InfraPilot"),
                new Paragraph("Local development sign-in shell. Authentication is added in Phase 3."),
                username,
                password,
                signIn);
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
    }
}
