package com.infrapilot.ui.views;

import com.infrapilot.ui.MainLayout;
import com.infrapilot.ui.client.InfraPilotApiClient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Value;

@Route(value = "monitor", layout = MainLayout.class)
@PageTitle("Observability — InfraPilot")
public class MonitorView extends VerticalLayout {

    public MonitorView(
            InfraPilotApiClient apiClient,
            @Value("${infrapilot.grafana.url:http://localhost:3000}") String grafanaBaseUrl
    ) {
        setPadding(true);
        setSpacing(true);
        setSizeFull();

        H2 heading = new H2("Live Observability & SRE Telemetry");
        Paragraph sub = new Paragraph("Embedded Grafana dashboards showing Prometheus metrics: 4 Golden Signals, JVM internals, and host cAdvisor utilization.");
        sub.getStyle().set("color", "var(--lumo-secondary-text-color)");

        // Top bar: Endpoint URL and Health Dot
        HorizontalLayout liveBar = new HorizontalLayout();
        liveBar.setWidthFull();
        liveBar.setAlignItems(FlexComponent.Alignment.CENTER);
        liveBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        liveBar.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "8px")
                .set("padding", "0.75rem 1.25rem");

        HorizontalLayout statusLeft = new HorizontalLayout();
        statusLeft.setAlignItems(FlexComponent.Alignment.CENTER);

        Span healthDot = new Span();
        healthDot.getStyle()
                .set("width", "10px")
                .set("height", "10px")
                .set("background", "var(--lumo-success-color)")
                .set("border-radius", "50%")
                .set("display", "inline-block")
                .set("box-shadow", "0 0 8px var(--lumo-success-color)");

        Span healthText = new Span("Application Health: UP (HTTP 200 OK)");
        healthText.getStyle().set("font-weight", "600");

        Anchor endpointLink = new Anchor("http://localhost:8081", "http://localhost:8081");
        endpointLink.setTarget("_blank");
        endpointLink.getStyle().set("color", "var(--lumo-primary-color)");

        statusLeft.add(healthDot, healthText, new Span("• Target:"), endpointLink);

        Button prometheusBtn = new Button("Open Prometheus (9090)", VaadinIcon.EXTERNAL_LINK.create(), e -> {
            getUI().ifPresent(ui -> ui.getPage().open("http://localhost:9090", "_blank"));
        });
        prometheusBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        liveBar.add(statusLeft, prometheusBtn);

        // Tabs for 4 Grafana Dashboards
        Tab tab1 = new Tab("1. Application Health");
        Tab tab2 = new Tab("2. Resource Utilization");
        Tab tab3 = new Tab("3. Pipeline & Quality");
        Tab tab4 = new Tab("4. Security & Cost");
        Tabs tabs = new Tabs(tab1, tab2, tab3, tab4);
        tabs.setWidthFull();

        // Embedded Grafana IFrame per Section 12.5
        IFrame grafanaFrame = new IFrame();
        grafanaFrame.setWidthFull();
        grafanaFrame.setHeight("650px");
        grafanaFrame.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)").set("border-radius", "8px");

        String defaultUrl = grafanaBaseUrl + "/d/app-health/application-health?kiosk&theme=light&refresh=10s";
        grafanaFrame.setSrc(defaultUrl);

        tabs.addSelectedChangeListener(e -> {
            Tab sel = e.getSelectedTab();
            if (sel == tab1) {
                grafanaFrame.setSrc(grafanaBaseUrl + "/d/app-health/application-health?kiosk&theme=light&refresh=10s");
            } else if (sel == tab2) {
                grafanaFrame.setSrc(grafanaBaseUrl + "/d/res-util/resource-utilization?kiosk&theme=light&refresh=10s");
            } else if (sel == tab3) {
                grafanaFrame.setSrc(grafanaBaseUrl + "/d/pipeline-quality/pipeline-and-quality?kiosk&theme=light&refresh=10s");
            } else if (sel == tab4) {
                grafanaFrame.setSrc(grafanaBaseUrl + "/d/security-cost/security-and-cost?kiosk&theme=light&refresh=10s");
            }
        });

        add(heading, sub, liveBar, tabs, grafanaFrame);
    }
}
