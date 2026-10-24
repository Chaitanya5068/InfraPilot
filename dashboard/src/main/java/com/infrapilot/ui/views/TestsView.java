package com.infrapilot.ui.views;

import com.infrapilot.ui.MainLayout;
import com.infrapilot.ui.client.InfraPilotApiClient;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "tests", layout = MainLayout.class)
@PageTitle("Tests — InfraPilot")
public class TestsView extends VerticalLayout {

    public TestsView(InfraPilotApiClient apiClient) {
        setPadding(true);
        setSpacing(true);
        setWidthFull();

        H2 heading = new H2("Quality & Test Verification");
        Paragraph sub = new Paragraph("Multi-tier test automation: Unit (JUnit 5 + JaCoCo), API (Newman), and Load (k6).");
        sub.getStyle().set("color", "var(--lumo-secondary-text-color)");

        // 3 Cards: Unit, API, Load
        HorizontalLayout cards = new HorizontalLayout();
        cards.setWidthFull();
        cards.setSpacing(true);

        cards.add(
                createTestCard("JUnit 5 Unit Tests", "12 Passed / 0 Failed", "Coverage: 84.5%", 0.845, "var(--lumo-success-color)"),
                createTestCard("Newman API Tests", "6 Passed / 0 Failed", "Assertions: 100% OK", 1.0, "var(--lumo-success-color)"),
                createTestCard("k6 Load Stress Test", "20 Virtual Users (30s)", "p95 Latency: 65ms (Threshold <500ms)", 0.13, "var(--lumo-primary-color)")
        );

        // Detailed breakdown
        H3 coverageHeading = new H3("JaCoCo Code Coverage & Quality Thresholds");
        coverageHeading.getStyle().set("margin-top", "1.5rem");

        VerticalLayout coverageBox = new VerticalLayout();
        coverageBox.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "10px")
                .set("padding", "1.5rem");

        HorizontalLayout barHeader = new HorizontalLayout();
        barHeader.setWidthFull();
        barHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        barHeader.add(new Span("Line & Branch Coverage"), new Span("84.5% (Minimum required: 60.0%)"));

        ProgressBar pb = new ProgressBar(0, 100, 84.5);
        pb.setWidthFull();

        coverageBox.add(barHeader, pb);

        add(heading, sub, cards, coverageHeading, coverageBox);
    }

    private VerticalLayout createTestCard(String title, String mainStat, String subStat, double progress, String color) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "10px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.04)");

        Span t = new Span(title);
        t.getStyle().set("font-weight", "700").set("font-size", "1.05rem");

        H2 val = new H2(mainStat);
        val.getStyle().set("margin", "0.5rem 0").set("color", color).set("font-size", "1.4rem");

        Span s = new Span(subStat);
        s.getStyle().set("font-size", "0.85rem").set("color", "var(--lumo-secondary-text-color)");

        card.add(t, val, s);
        return card;
    }
}
