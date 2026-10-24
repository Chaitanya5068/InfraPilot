package com.infrapilot.ui.views;

import com.infrapilot.ui.MainLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | InfraPilot")
@AnonymousAllowed
public class DashboardView extends VerticalLayout {

    public DashboardView() {
        add(
                new H2("Foundation is ready"),
                new Paragraph("The Phase 0 application shell is running. Deployment workflows arrive in later phases."));
    }
}
