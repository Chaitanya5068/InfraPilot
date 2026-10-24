package com.infrapilot.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.component.page.Push;
import com.infrapilot.ui.views.DashboardView;

@Push
@AnonymousAllowed
public class MainLayout extends AppLayout {

    public MainLayout() {
        addToNavbar(createHeader());
        addToDrawer(createNavigation());
    }

    private Component createHeader() {
        DrawerToggle drawerToggle = new DrawerToggle();
        H1 title = new H1("InfraPilot");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)");
        title.getStyle().set("margin", "0");

        Span username = new Span("Local developer");
        Button logout = new Button("Logout", event -> UI.getCurrent().navigate("login"));
        HorizontalLayout header = new HorizontalLayout(drawerToggle, title, username, logout);
        header.setWidthFull();
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(title);
        return header;
    }

    private Component createNavigation() {
        VerticalLayout navigation = new VerticalLayout();
        navigation.setPadding(true);
        navigation.setSpacing(false);
        navigation.add(new RouterLink("Dashboard", DashboardView.class));
        navigation.add(new Span("Deploy · Coming soon"));
        navigation.add(new Span("Pipelines · Coming soon"));
        navigation.add(new Span("Infrastructure · Coming soon"));
        navigation.add(new Span("Security · Coming soon"));
        navigation.add(new Span("Monitoring · Coming soon"));
        navigation.add(new Span("Audit · Coming soon"));
        navigation.add(new Span("Cost · Coming soon"));
        navigation.add(new Span("Settings · Coming soon"));
        navigation.add(new Anchor("login", "Login"));
        return navigation;
    }
}
