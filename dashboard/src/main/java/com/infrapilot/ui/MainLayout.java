package com.infrapilot.ui;

import com.infrapilot.ui.views.*;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;

public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("InfraPilot");
        logo.addClassNames("text-l", "m-m");
        logo.getStyle().set("font-size", "1.25rem")
                .set("font-weight", "700")
                .set("letter-spacing", "-0.025em")
                .set("color", "#3b82f6");

        Span versionBadge = new Span("v2.1");
        versionBadge.getElement().getThemeList().add("badge success small");
        versionBadge.getStyle().set("margin-left", "0.5rem");

        Span envIndicator = new Span("AWS ap-south-1 (EC2)");
        envIndicator.getElement().getThemeList().add("badge contrast small");
        envIndicator.getStyle().set("margin-left", "auto").set("margin-right", "1rem");

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo, versionBadge, envIndicator);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

    private void createDrawer() {
        SideNav nav = new SideNav();

        nav.addItem(new SideNavItem("Dashboard", DashboardView.class, VaadinIcon.DASHBOARD.create()));
        nav.addItem(new SideNavItem("New Run", NewRunView.class, VaadinIcon.ROCKET.create()));
        nav.addItem(new SideNavItem("Pipeline & Stages", PipelineView.class, VaadinIcon.TASKS.create()));
        nav.addItem(new SideNavItem("Security Scans", SecurityView.class, VaadinIcon.SHIELD.create()));
        nav.addItem(new SideNavItem("Test Results", TestsView.class, VaadinIcon.CHECK_CIRCLE.create()));
        nav.addItem(new SideNavItem("FinOps & Cost", CostView.class, VaadinIcon.MONEY.create()));
        nav.addItem(new SideNavItem("Observability", MonitorView.class, VaadinIcon.CHART_LINE.create()));
        nav.addItem(new SideNavItem("Settings", SettingsView.class, VaadinIcon.COG.create()));
        nav.addItem(new SideNavItem("Sign Out", LoginView.class, VaadinIcon.SIGN_OUT.create()));

        VerticalLayout layout = new VerticalLayout(nav);
        layout.setSizeFull();
        layout.setPadding(true);
        layout.setSpacing(true);

        addToDrawer(layout);
    }
}
