import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;


public final class EmployeeTotalsReportGenerator {

    private static final int WIDTH = 2200;
    private static final int MARGIN = 50;
    private static final int ROW_HEIGHT = 38;

    private EmployeeTotalsReportGenerator() {
    }

    public static ReportFiles generate(Employee employee) throws IOException {
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null.");
        }

        BufferedImage report = createReportImage(employee);
        File folder = new File(System.getProperty("user.home"), "Documents/Payroll Manager/Reports");
        if (!folder.exists() && !folder.mkdirs()) {
            throw new IOException("Could not create report folder: " + folder.getAbsolutePath());
        }

        String stamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String name = safeFileName(employee.name) + "_Payroll_Totals_" + stamp;
        File png = new File(folder, name + ".png");
        File pdf = new File(folder, name + ".pdf");

        ImageIO.write(report, "png", png);
        writeVectorPdf(employee, pdf);
        return new ReportFiles(pdf, png);
    }

    private static BufferedImage createReportImage(Employee employee) {
        // Room for the title, annual totals, full pay-period table, and footer.
        int height = 800 + PayrollData.PAY_PERIODS.length * ROW_HEIGHT;
        BufferedImage image = new BufferedImage(WIDTH, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, WIDTH, height);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int y = MARGIN;
        y = drawTitle(graphics, employee, y);
        y = drawTotals(graphics, employee, y + 28);
        y = drawPayPeriodTable(graphics, employee, y + 35);

        graphics.setColor(new Color(90, 90, 90));
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 16));
        graphics.drawString("Generated " + new SimpleDateFormat("MMMM d, yyyy h:mm a").format(new Date()), MARGIN, y + 20);
        graphics.dispose();
        return image;
    }

    private static int drawTitle(Graphics2D g, Employee employee, int y) {
        g.setColor(new Color(27, 67, 100));
        g.fillRoundRect(MARGIN, y, WIDTH - MARGIN * 2, 115, 16, 16);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 34));
        g.drawString("Employee Payroll Totals", MARGIN + 28, y + 45);
        g.setFont(new Font("SansSerif", Font.PLAIN, 21));
        g.drawString("Employee: " + text(employee.name) + "   |   Federal rate: " + text(employee.fed_rate) + "%", MARGIN + 28, y + 82);
        return y + 115;
    }

    private static int drawTotals(Graphics2D g, Employee employee, int y) {
        Map<String, Double> totals = emptyTotals();
        for (int i = 0; i < PayrollData.PAY_PERIODS.length; i++) {
            add(totals, calculatePeriod(employee, i));
        }
        String[][] values = {
                {"Name", text(employee.name)}, {"Address", text(employee.address)}, {"City / ZIP", text(employee.city) + " " + text(employee.zip)},
                {"Total Hours", number(totals.get("Hours"))}, {"Total OT Hours", number(totals.get("OT Hours"))}, {"Regular Pay", money(totals.get("Regular Pay"))},
                {"OT Pay", money(totals.get("OT Pay"))}, {"Bonus / Extra", money(totals.get("Bonus"))}, {"Total Gross", money(totals.get("Total Gross"))},
                {"Federal Tax", money(totals.get("Federal"))}, {"Social Security", money(totals.get("Social Security"))}, {"Medicare", money(totals.get("Medicare"))},
                {"Maryland Tax", money(totals.get("MD Tax"))}, {"Baltimore County", money(totals.get("BC Tax"))}, {"SLG Tax", money(totals.get("SLG Tax"))},
                {"Total Deductions", money(totals.get("Total Deductions"))}, {"Net Pay", money(totals.get("Net Pay"))}
        };
        drawSectionTitle(g, "Employee Information and Annual Totals", y);
        y += 38;
        int columns = 3;
        int boxWidth = (WIDTH - MARGIN * 2 - 24) / columns;
        for (int i = 0; i < values.length; i++) {
            int x = MARGIN + (i % columns) * (boxWidth + 12);
            int rowY = y + (i / columns) * 66;
            g.setColor(new Color(242, 246, 250));
            g.fillRoundRect(x, rowY, boxWidth, 56, 8, 8);
            g.setColor(new Color(55, 75, 95));
            g.setFont(new Font("SansSerif", Font.BOLD, 15));
            g.drawString(values[i][0], x + 12, rowY + 21);
            g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.PLAIN, 18));
            g.drawString(clip(values[i][1], 42), x + 12, rowY + 45);
        }
        return y + ((values.length + columns - 1) / columns) * 66;
    }

    private static int drawPayPeriodTable(Graphics2D g, Employee employee, int y) {
        String[] headers = {"Period", "Start", "End", "Paid On", "Quarter", "Hours", "OT Hrs", "Rate", "OT Rate", "Extra", "Regular", "OT Pay", "Gross", "Federal", "Soc. Sec.", "Medicare", "MD Tax", "BC Tax", "SLG", "Deductions", "Net Pay"};
        drawSectionTitle(g, "Pay Period Details", y);
        y += 38;
        int tableWidth = WIDTH - MARGIN * 2;
        int columnWidth = tableWidth / headers.length;
        drawTableRow(g, headers, y, columnWidth, true);
        y += ROW_HEIGHT;
        for (int i = 0; i < PayrollData.PAY_PERIODS.length; i++) {
            Map<String, Double> p = calculatePeriod(employee, i);
            String[] row = {PayrollData.PAY_PERIODS[i][0], PayrollData.PAY_PERIODS[i][1], PayrollData.PAY_PERIODS[i][2], PayrollData.PAY_PERIODS[i][3], PayrollData.PAY_PERIODS[i][4], text(employee.hours[i]), text(employee.ot_hours[i]), text(employee.hourly_rates[i]), text(employee.ot_rates[i]), text(employee.extra[i]), money(p.get("Regular Pay")), money(p.get("OT Pay")), money(p.get("Total Gross")), money(p.get("Federal")), money(p.get("Social Security")), money(p.get("Medicare")), money(p.get("MD Tax")), money(p.get("BC Tax")), money(p.get("SLG Tax")), money(p.get("Total Deductions")), money(p.get("Net Pay"))};
            drawTableRow(g, row, y, columnWidth, false);
            y += ROW_HEIGHT;
        }
        return y;
    }

    private static void drawSectionTitle(Graphics2D g, String title, int y) {
        g.setColor(new Color(27, 67, 100));
        g.setFont(new Font("SansSerif", Font.BOLD, 23));
        g.drawString(title, MARGIN, y + 25);
        g.setStroke(new BasicStroke(2));
        g.drawLine(MARGIN, y + 34, WIDTH - MARGIN, y + 34);
    }

    private static void drawTableRow(Graphics2D g, String[] values, int y, int columnWidth, boolean header) {
        if (header) g.setColor(new Color(67, 92, 116)); else g.setColor(y / ROW_HEIGHT % 2 == 0 ? new Color(247, 249, 251) : Color.WHITE);
        g.fillRect(MARGIN, y, columnWidth * values.length, ROW_HEIGHT);
        g.setColor(header ? Color.WHITE : Color.DARK_GRAY);
        g.setFont(new Font("SansSerif", header ? Font.BOLD : Font.PLAIN, header ? 12 : 11));
        for (int i = 0; i < values.length; i++) {
            g.drawString(clip(values[i], 12), MARGIN + i * columnWidth + 5, y + 25);
        }
    }

    private static Map<String, Double> calculatePeriod(Employee employee, int i) {
        PayrollCalculator c = new PayrollCalculator();
        double regular = c.calculateRegularPay(employee.hours[i], employee.hourly_rates[i]);
        double overtime = c.calculateOTPay(employee.ot_hours[i], employee.ot_rates[i]);
        double bonus = c.calculateExtraPay(employee.extra[i]);
        double gross = regular + overtime + bonus;
        double federal = c.calculateFedRate(gross, employee.fed_rate);
        double socialSecurity = c.calculateSocialSecurity(gross);
        double medicare = c.calculateMedicare(gross);
        double maryland = c.calculateMaryland(gross);
        double baltimore = c.calculateBaltimore(gross);
        double slg = maryland + baltimore;
        double net = c.calculateNet(gross, federal, socialSecurity, medicare, maryland, baltimore);
        Map<String, Double> result = emptyTotals();
        result.put("Hours", value(employee.hours[i])); result.put("OT Hours", value(employee.ot_hours[i])); result.put("Regular Pay", regular); result.put("OT Pay", overtime); result.put("Bonus", bonus); result.put("Total Gross", gross); result.put("Federal", federal); result.put("Social Security", socialSecurity); result.put("Medicare", medicare); result.put("MD Tax", maryland); result.put("BC Tax", baltimore); result.put("SLG Tax", slg); result.put("Total Deductions", gross - net); result.put("Net Pay", net);
        return result;
    }

    private static Map<String, Double> emptyTotals() {
        Map<String, Double> totals = new LinkedHashMap<>();
        String[] names = {"Hours", "OT Hours", "Regular Pay", "OT Pay", "Bonus", "Total Gross", "Federal", "Social Security", "Medicare", "MD Tax", "BC Tax", "SLG Tax", "Total Deductions", "Net Pay"};
        for (String name : names) totals.put(name, 0.0);
        return totals;
    }

    private static void add(Map<String, Double> totals, Map<String, Double> values) { for (String name : totals.keySet()) totals.put(name, totals.get(name) + values.get(name)); }
    private static double value(String text) { try { return text == null || text.trim().isEmpty() ? 0 : Double.parseDouble(text.trim()); } catch (NumberFormatException ignored) { return 0; } }
    private static String text(String value) { return value == null ? "" : value; }
    private static String number(Double value) { return String.format("%.2f", value == null ? 0 : value); }
    private static String money(Double value) { return String.format("$%.2f", value == null ? 0 : value); }
    private static String clip(String value, int length) { return value.length() <= length ? value : value.substring(0, Math.max(0, length - 1)) + "…"; }
    private static String safeFileName(String name) { String cleaned = text(name).replaceAll("[\\\\/:*?\"<>|]", "_").trim(); return cleaned.isEmpty() ? "Employee" : cleaned; }

    /** Writes the report as native PDF text and shapes, so it stays sharp at every zoom level. */
    private static void writeVectorPdf(Employee employee, File output) throws IOException {
        float scale = 0.48f;
        float pageWidth = WIDTH * scale;
        float pageHeight = (800 + PayrollData.PAY_PERIODS.length * ROW_HEIGHT) * scale;
        PdfCanvas canvas = new PdfCanvas(pageHeight, scale);
        int y = MARGIN;

        canvas.fill(new Color(27, 67, 100), MARGIN, y, WIDTH - MARGIN * 2, 115);
        canvas.text("F2", 34, Color.WHITE, MARGIN + 28, y + 45, "Employee Payroll Totals");
        canvas.text("F1", 21, Color.WHITE, MARGIN + 28, y + 82,
                "Employee: " + text(employee.name) + "   |   Federal rate: " + text(employee.fed_rate) + "%");
        y += 143;

        Map<String, Double> totals = emptyTotals();
        for (int i = 0; i < PayrollData.PAY_PERIODS.length; i++) add(totals, calculatePeriod(employee, i));
        drawPdfSectionTitle(canvas, "Employee Information and Annual Totals", y);
        y += 38;
        String[][] values = {
                {"Name", text(employee.name)}, {"Address", text(employee.address)}, {"City / ZIP", text(employee.city) + " " + text(employee.zip)},
                {"Total Hours", number(totals.get("Hours"))}, {"Total OT Hours", number(totals.get("OT Hours"))}, {"Regular Pay", money(totals.get("Regular Pay"))},
                {"OT Pay", money(totals.get("OT Pay"))}, {"Bonus / Extra", money(totals.get("Bonus"))}, {"Total Gross", money(totals.get("Total Gross"))},
                {"Federal Tax", money(totals.get("Federal"))}, {"Social Security", money(totals.get("Social Security"))}, {"Medicare", money(totals.get("Medicare"))},
                {"Maryland Tax", money(totals.get("MD Tax"))}, {"Baltimore County", money(totals.get("BC Tax"))}, {"SLG Tax", money(totals.get("SLG Tax"))},
                {"Total Deductions", money(totals.get("Total Deductions"))}, {"Net Pay", money(totals.get("Net Pay"))}
        };
        int boxWidth = (WIDTH - MARGIN * 2 - 24) / 3;
        for (int i = 0; i < values.length; i++) {
            int x = MARGIN + (i % 3) * (boxWidth + 12);
            int rowY = y + (i / 3) * 66;
            canvas.fill(new Color(242, 246, 250), x, rowY, boxWidth, 56);
            canvas.text("F2", 15, new Color(55, 75, 95), x + 12, rowY + 21, values[i][0]);
            canvas.text("F1", 18, Color.BLACK, x + 12, rowY + 45, clip(values[i][1], 42));
        }
        y += ((values.length + 2) / 3) * 66 + 35;

        drawPdfSectionTitle(canvas, "Pay Period Details", y);
        y += 38;
        String[] headers = {"Period", "Start", "End", "Paid On", "Quarter", "Hours", "OT Hrs", "Rate", "OT Rate", "Extra", "Regular", "OT Pay", "Gross", "Federal", "Soc. Sec.", "Medicare", "MD Tax", "BC Tax", "SLG", "Deductions", "Net Pay"};
        int columnWidth = (WIDTH - MARGIN * 2) / headers.length;
        drawPdfTableRow(canvas, headers, y, columnWidth, true);
        y += ROW_HEIGHT;
        for (int i = 0; i < PayrollData.PAY_PERIODS.length; i++) {
            Map<String, Double> p = calculatePeriod(employee, i);
            String[] row = {PayrollData.PAY_PERIODS[i][0], PayrollData.PAY_PERIODS[i][1], PayrollData.PAY_PERIODS[i][2], PayrollData.PAY_PERIODS[i][3], PayrollData.PAY_PERIODS[i][4], text(employee.hours[i]), text(employee.ot_hours[i]), text(employee.hourly_rates[i]), text(employee.ot_rates[i]), text(employee.extra[i]), money(p.get("Regular Pay")), money(p.get("OT Pay")), money(p.get("Total Gross")), money(p.get("Federal")), money(p.get("Social Security")), money(p.get("Medicare")), money(p.get("MD Tax")), money(p.get("BC Tax")), money(p.get("SLG Tax")), money(p.get("Total Deductions")), money(p.get("Net Pay"))};
            drawPdfTableRow(canvas, row, y, columnWidth, false);
            y += ROW_HEIGHT;
        }
        canvas.text("F1", 16, new Color(90, 90, 90), MARGIN, y + 25,
                "Generated " + new SimpleDateFormat("MMMM d, yyyy h:mm a").format(new Date()));

        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        pdf.write("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));
        int[] offsets = new int[7];
        offsets[1] = pdf.size(); write(pdf, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
        offsets[2] = pdf.size(); write(pdf, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");
        offsets[3] = pdf.size(); write(pdf, "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + pageWidth + " " + pageHeight + "] /Resources << /Font << /F1 5 0 R /F2 6 0 R >> >> /Contents 4 0 R >>\nendobj\n");
        String content = canvas.content();
        offsets[4] = pdf.size(); write(pdf, "4 0 obj\n<< /Length " + content.getBytes(StandardCharsets.US_ASCII).length + " >>\nstream\n" + content + "endstream\nendobj\n");
        offsets[5] = pdf.size(); write(pdf, "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");
        offsets[6] = pdf.size(); write(pdf, "6 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n");
        int xref = pdf.size(); write(pdf, "xref\n0 7\n0000000000 65535 f \n");
        for (int i = 1; i <= 6; i++) write(pdf, String.format("%010d 00000 n \n", offsets[i]));
        write(pdf, "trailer\n<< /Size 7 /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF\n");
        try (FileOutputStream stream = new FileOutputStream(output)) { pdf.writeTo(stream); }
    }

    private static void drawPdfSectionTitle(PdfCanvas canvas, String title, int y) {
        canvas.text("F2", 23, new Color(27, 67, 100), MARGIN, y + 25, title);
        canvas.line(new Color(27, 67, 100), 2, MARGIN, y + 34, WIDTH - MARGIN, y + 34);
    }

    private static void drawPdfTableRow(PdfCanvas canvas, String[] values, int y, int columnWidth, boolean header) {
        canvas.fill(header ? new Color(67, 92, 116) : (y / ROW_HEIGHT % 2 == 0 ? new Color(247, 249, 251) : Color.WHITE), MARGIN, y, columnWidth * values.length, ROW_HEIGHT);
        for (int i = 0; i < values.length; i++) canvas.text(header ? "F2" : "F1", header ? 12 : 11, header ? Color.WHITE : Color.DARK_GRAY, MARGIN + i * columnWidth + 5, y + 25, clip(values[i], 12));
    }

    private static final class PdfCanvas {
        private final StringBuilder commands = new StringBuilder();
        private final float pageHeight;
        private final float scale;

        private PdfCanvas(float pageHeight, float scale) { this.pageHeight = pageHeight; this.scale = scale; }
        private void fill(Color color, float x, float y, float width, float height) { color(color); commands.append(n(x * scale)).append(' ').append(n(pdfY(y + height))).append(' ').append(n(width * scale)).append(' ').append(n(height * scale)).append(" re f\n"); }
        private void line(Color color, float width, float x1, float y1, float x2, float y2) { color(color); commands.append(n(width * scale)).append(" w ").append(n(x1 * scale)).append(' ').append(n(pdfY(y1))).append(" m ").append(n(x2 * scale)).append(' ').append(n(pdfY(y2))).append(" l S\n"); }
        private void text(String font, float size, Color color, float x, float y, String text) { color(color); commands.append("BT /").append(font).append(' ').append(n(size * scale)).append(" Tf ").append(n(x * scale)).append(' ').append(n(pdfY(y))).append(" Td (").append(escapePdf(text)).append(") Tj ET\n"); }
        private void color(Color color) { commands.append(n(color.getRed() / 255f)).append(' ').append(n(color.getGreen() / 255f)).append(' ').append(n(color.getBlue() / 255f)).append(" rg\n"); }
        private float pdfY(float topY) { return pageHeight - topY * scale; }
        private String content() { return commands.toString(); }
    }

    private static String escapePdf(String value) { return value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)").replaceAll("[^\\x20-\\x7E]", "?"); }
    private static String n(float value) { return String.format(java.util.Locale.US, "%.2f", value); }

    private static void write(ByteArrayOutputStream output, String value) throws IOException { output.write(value.getBytes(StandardCharsets.US_ASCII)); }

    public static final class ReportFiles {
        public final File pdf;
        public final File png;
        private ReportFiles(File pdf, File png) { this.pdf = pdf; this.png = png; }
    }
}
