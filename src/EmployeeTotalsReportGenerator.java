import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Creates a sharp, one-page PDF report for one employee. */
public final class EmployeeTotalsReportGenerator {

    // The report is wide enough to show every payroll column clearly.
    private static final int REPORT_WIDTH = 2200;

    // The blank border around report content.
    private static final int MARGIN = 50;

    // The height of each pay-period row in the report table.
    private static final int ROW_HEIGHT = 38;

    // Converts screen-sized report measurements into PDF page measurements.
    private static final float PDF_SCALE = 0.48f;

    // This is a utility class, so it should not be created as an object.
    private EmployeeTotalsReportGenerator() {
    }

    /** Creates and returns the employee's PDF report file. */
    public static File generate(Employee employee) throws IOException {

        // A report cannot be made without an employee to report on.
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null.");
        }

        File reportFolder = getReportFolder();
        File pdfFile = new File(reportFolder, buildReportFileName(employee) + ".pdf");

        writeVectorPdf(employee, pdfFile);

        return pdfFile;
    }

    /** Returns the folder where employee PDF reports are saved. */
    private static File getReportFolder() throws IOException {

        File folder = new File(System.getProperty("user.home"), "Documents/Payroll Manager/Reports");

        if (!folder.exists() && !folder.mkdirs()) {
            throw new IOException("Could not create report folder: " + folder.getAbsolutePath());
        }

        return folder;
    }

    /** Builds a unique, Windows-safe PDF file name. */
    private static String buildReportFileName(Employee employee) {

        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());

        return safeFileName(employee.name) + "_Payroll_Totals_" + timestamp;
    }

    /** Draws every part of the report as native PDF text and shapes. */
    private static void writeVectorPdf(Employee employee, File output) throws IOException {

        float pageWidth = REPORT_WIDTH * PDF_SCALE;
        float pageHeight = getReportHeight() * PDF_SCALE;

        PdfCanvas canvas = new PdfCanvas(pageHeight, PDF_SCALE);

        int y = MARGIN;
        y = drawPdfHeader(canvas, employee, y);
        y = drawPdfTotals(canvas, employee, y + 28);
        y = drawPdfPayPeriodTable(canvas, employee, y + 35);

        canvas.text(
                "F1", 16, new Color(90, 90, 90), MARGIN, y + 25,
                "Generated " + new SimpleDateFormat("MMMM d, yyyy h:mm a").format(new Date())
        );

        PdfDocument document = new PdfDocument();
        document.addCatalog();
        document.addPages();
        document.addPage(pageWidth, pageHeight);
        document.addPageContent(canvas.content());
        document.addStandardFonts();
        document.save(output);
    }

    /** Calculates the page height from the fixed sections and 26 pay periods. */
    private static int getReportHeight() {

        return 800 + PayrollData.PAY_PERIODS.length * ROW_HEIGHT;
    }

    /** Draws the blue report banner and employee identification. */
    private static int drawPdfHeader(PdfCanvas canvas, Employee employee, int y) {

        canvas.fill(new Color(27, 67, 100), MARGIN, y, REPORT_WIDTH - MARGIN * 2, 115);

        canvas.text("F2", 34, Color.WHITE, MARGIN + 28, y + 45, "Employee Payroll Totals");

        canvas.text(
                "F1", 21, Color.WHITE, MARGIN + 28, y + 82,
                "Employee: " + text(employee.name)
                        + "   |   Federal rate: " + text(employee.fed_rate) + "%"
        );

        return y + 115;
    }

    /** Draws the employee information and annual-total summary boxes. */
    private static int drawPdfTotals(PdfCanvas canvas, Employee employee, int y) {

        Map<String, Double> totals = calculateAnnualTotals(employee);

        drawPdfSectionTitle(canvas, "Employee Information and Annual Totals", y);

        y += 38;

        String[][] values = {
                {"Name", text(employee.name)},
                {"Address", text(employee.address)},
                {"City / ZIP", text(employee.city) + " " + text(employee.zip)},
                {"Total Hours", number(totals.get("Hours"))},
                {"Total OT Hours", number(totals.get("OT Hours"))},
                {"Regular Pay", money(totals.get("Regular Pay"))},
                {"OT Pay", money(totals.get("OT Pay"))},
                {"Bonus / Extra", money(totals.get("Bonus"))},
                {"Total Gross", money(totals.get("Total Gross"))},
                {"Federal Tax", money(totals.get("Federal"))},
                {"Social Security", money(totals.get("Social Security"))},
                {"Medicare", money(totals.get("Medicare"))},
                {"Maryland Tax", money(totals.get("MD Tax"))},
                {"Baltimore County", money(totals.get("BC Tax"))},
                {"SLG Tax", money(totals.get("SLG Tax"))},
                {"Total Deductions", money(totals.get("Total Deductions"))},
                {"Net Pay", money(totals.get("Net Pay"))}
        };

        int columns = 3;
        int boxWidth = (REPORT_WIDTH - MARGIN * 2 - 24) / columns;

        for (int i = 0; i < values.length; i++) {

            int x = MARGIN + (i % columns) * (boxWidth + 12);
            int rowY = y + (i / columns) * 66;

            canvas.fill(new Color(242, 246, 250), x, rowY, boxWidth, 56);
            canvas.text("F2", 15, new Color(55, 75, 95), x + 12, rowY + 21, values[i][0]);
            canvas.text("F1", 18, Color.BLACK, x + 12, rowY + 45, clip(values[i][1], 42));
        }

        return y + ((values.length + columns - 1) / columns) * 66;
    }

    /** Draws the detailed 26-pay-period payroll table. */
    private static int drawPdfPayPeriodTable(PdfCanvas canvas, Employee employee, int y) {

        String[] headers = {
                "Period", "Start", "End", "Paid On", "Quarter", "Hours", "OT Hrs",
                "Rate", "OT Rate", "Extra", "Regular", "OT Pay", "Gross", "Federal",
                "Soc. Sec.", "Medicare", "MD Tax", "BC Tax", "SLG", "Deductions", "Net Pay"
        };

        drawPdfSectionTitle(canvas, "Pay Period Details", y);

        y += 38;

        int tableWidth = REPORT_WIDTH - MARGIN * 2;
        int columnWidth = tableWidth / headers.length;

        drawPdfTableRow(canvas, headers, y, columnWidth, true);

        y += ROW_HEIGHT;

        for (int period = 0; period < PayrollData.PAY_PERIODS.length; period++) {

            Map<String, Double> values = calculatePeriod(employee, period);

            String[] row = {
                    PayrollData.PAY_PERIODS[period][0],
                    PayrollData.PAY_PERIODS[period][1],
                    PayrollData.PAY_PERIODS[period][2],
                    PayrollData.PAY_PERIODS[period][3],
                    PayrollData.PAY_PERIODS[period][4],
                    text(employee.hours[period]),
                    text(employee.ot_hours[period]),
                    text(employee.hourly_rates[period]),
                    text(employee.ot_rates[period]),
                    text(employee.extra[period]),
                    money(values.get("Regular Pay")),
                    money(values.get("OT Pay")),
                    money(values.get("Total Gross")),
                    money(values.get("Federal")),
                    money(values.get("Social Security")),
                    money(values.get("Medicare")),
                    money(values.get("MD Tax")),
                    money(values.get("BC Tax")),
                    money(values.get("SLG Tax")),
                    money(values.get("Total Deductions")),
                    money(values.get("Net Pay"))
            };

            drawPdfTableRow(canvas, row, y, columnWidth, false);

            y += ROW_HEIGHT;
        }

        return y;
    }

    /** Draws a section label with a horizontal line beneath it. */
    private static void drawPdfSectionTitle(PdfCanvas canvas, String title, int y) {

        canvas.text("F2", 23, new Color(27, 67, 100), MARGIN, y + 25, title);
        canvas.line(new Color(27, 67, 100), 2, MARGIN, y + 34, REPORT_WIDTH - MARGIN, y + 34);
    }

    /** Draws either a column-heading row or a normal payroll data row. */
    private static void drawPdfTableRow(PdfCanvas canvas, String[] values, int y, int columnWidth, boolean header) {

        Color background = header
                ? new Color(67, 92, 116)
                : (y / ROW_HEIGHT % 2 == 0 ? new Color(247, 249, 251) : Color.WHITE);

        Color textColor = header ? Color.WHITE : Color.DARK_GRAY;
        String font = header ? "F2" : "F1";
        int fontSize = header ? 12 : 11;

        canvas.fill(background, MARGIN, y, columnWidth * values.length, ROW_HEIGHT);

        for (int column = 0; column < values.length; column++) {

            canvas.text(
                    font,
                    fontSize,
                    textColor,
                    MARGIN + column * columnWidth + 5,
                    y + 25,
                    clip(values[column], 12)
            );
        }
    }

    /** Adds all 26 pay periods together to create annual totals. */
    private static Map<String, Double> calculateAnnualTotals(Employee employee) {

        Map<String, Double> totals = emptyTotals();

        for (int period = 0; period < PayrollData.PAY_PERIODS.length; period++) {
            add(totals, calculatePeriod(employee, period));
        }

        return totals;
    }

    /** Calculates every payroll value displayed for one pay period. */
    private static Map<String, Double> calculatePeriod(Employee employee, int period) {

        PayrollCalculator calculator = new PayrollCalculator();

        double regularPay = calculator.calculateRegularPay(employee.hours[period], employee.hourly_rates[period]);
        double overtimePay = calculator.calculateOTPay(employee.ot_hours[period], employee.ot_rates[period]);
        double bonus = calculator.calculateExtraPay(employee.extra[period]);
        double grossPay = regularPay + overtimePay + bonus;

        double federal = calculator.calculateFedRate(grossPay, employee.fed_rate);
        double socialSecurity = calculator.calculateSocialSecurity(grossPay);
        double medicare = calculator.calculateMedicare(grossPay);
        double maryland = calculator.calculateMaryland(grossPay);
        double baltimore = calculator.calculateBaltimore(grossPay);

        // SLG is displayed as the combined Maryland and Baltimore County taxes.
        double slg = maryland + baltimore;

        double netPay = calculator.calculateNet(
                grossPay, federal, socialSecurity, medicare, maryland, baltimore
        );

        Map<String, Double> values = emptyTotals();
        values.put("Hours", value(employee.hours[period]));
        values.put("OT Hours", value(employee.ot_hours[period]));
        values.put("Regular Pay", regularPay);
        values.put("OT Pay", overtimePay);
        values.put("Bonus", bonus);
        values.put("Total Gross", grossPay);
        values.put("Federal", federal);
        values.put("Social Security", socialSecurity);
        values.put("Medicare", medicare);
        values.put("MD Tax", maryland);
        values.put("BC Tax", baltimore);
        values.put("SLG Tax", slg);
        values.put("Total Deductions", grossPay - netPay);
        values.put("Net Pay", netPay);

        return values;
    }

    /** Creates all report-total labels with a starting value of zero. */
    private static Map<String, Double> emptyTotals() {

        Map<String, Double> totals = new LinkedHashMap<>();

        String[] names = {
                "Hours", "OT Hours", "Regular Pay", "OT Pay", "Bonus", "Total Gross",
                "Federal", "Social Security", "Medicare", "MD Tax", "BC Tax", "SLG Tax",
                "Total Deductions", "Net Pay"
        };

        for (String name : names) {
            totals.put(name, 0.0);
        }

        return totals;
    }

    /** Adds one pay period's values into the running annual totals. */
    private static void add(Map<String, Double> totals, Map<String, Double> values) {

        for (String name : totals.keySet()) {
            totals.put(name, totals.get(name) + values.get(name));
        }
    }

    /** Treats blank or invalid number text as zero. */
    private static double value(String text) {

        try {
            return text == null || text.trim().isEmpty() ? 0 : Double.parseDouble(text.trim());
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    /** Replaces missing text with a blank value. */
    private static String text(String value) {

        return value == null ? "" : value;
    }

    /** Formats a normal number with two decimal places. */
    private static String number(Double value) {

        return String.format("%.2f", value == null ? 0 : value);
    }

    /** Formats a number as a dollar amount. */
    private static String money(Double value) {

        return String.format("$%.2f", value == null ? 0 : value);
    }

    /** Shortens text that would overflow a report cell. */
    private static String clip(String value, int length) {

        return value.length() <= length ? value : value.substring(0, Math.max(0, length - 1)) + "...";
    }

    /** Removes characters that Windows does not allow in a file name. */
    private static String safeFileName(String name) {

        String cleaned = text(name).replaceAll("[\\\\/:*?\"<>|]", "_").trim();

        return cleaned.isEmpty() ? "Employee" : cleaned;
    }

    /** Represents the basic parts every one-page PDF needs. */
    private static final class PdfDocument {

        private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        private final int[] objectOffsets = new int[7];

        /** Starts the PDF document with its required version marker. */
        private PdfDocument() throws IOException {

            write("%PDF-1.4\n");
        }

        /** Adds the top-level PDF object that points to the page list. */
        private void addCatalog() throws IOException {

            addObject(1, "<< /Type /Catalog /Pages 2 0 R >>");
        }

        /** Adds the list containing this report's single page. */
        private void addPages() throws IOException {

            addObject(2, "<< /Type /Pages /Kids [3 0 R] /Count 1 >>");
        }

        /** Adds the page size, fonts, and drawing-content reference. */
        private void addPage(float pageWidth, float pageHeight) throws IOException {

            String page = "<< /Type /Page /Parent 2 0 R "
                    + "/MediaBox [0 0 " + number(pageWidth) + " " + number(pageHeight) + "] "
                    + "/Resources << /Font << /F1 5 0 R /F2 6 0 R >> >> "
                    + "/Contents 4 0 R >>";

            addObject(3, page);
        }

        /** Adds all text, colored-box, and line commands for the page. */
        private void addPageContent(String content) throws IOException {

            objectOffsets[4] = bytes.size();

            write("4 0 obj\n");
            write("<< /Length " + content.getBytes(StandardCharsets.US_ASCII).length + " >>\n");
            write("stream\n");
            write(content);
            write("endstream\nendobj\n");
        }

        /** Adds standard fonts built into ordinary PDF readers. */
        private void addStandardFonts() throws IOException {

            addObject(5, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>");
            addObject(6, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>");
        }

        /** Adds one normal PDF object and remembers where it begins. */
        private void addObject(int objectNumber, String contents) throws IOException {

            objectOffsets[objectNumber] = bytes.size();

            write(objectNumber + " 0 obj\n");
            write(contents + "\n");
            write("endobj\n");
        }

        /** Finishes the PDF index and saves the completed bytes to disk. */
        private void save(File output) throws IOException {

            int crossReferenceOffset = bytes.size();

            write("xref\n0 7\n0000000000 65535 f \n");

            for (int objectNumber = 1; objectNumber <= 6; objectNumber++) {
                write(String.format("%010d 00000 n \n", objectOffsets[objectNumber]));
            }

            write("trailer\n<< /Size 7 /Root 1 0 R >>\n");
            write("startxref\n" + crossReferenceOffset + "\n%%EOF\n");

            try (FileOutputStream stream = new FileOutputStream(output)) {
                bytes.writeTo(stream);
            }
        }

        /** Adds plain PDF text to the in-memory document. */
        private void write(String value) throws IOException {

            bytes.write(value.getBytes(StandardCharsets.US_ASCII));
        }
    }

    /** Collects easy-to-read report drawing calls as PDF commands. */
    private static final class PdfCanvas {

        private final StringBuilder commands = new StringBuilder();
        private final float pageHeight;
        private final float scale;

        /** Remembers the page size used to convert report positions to PDF positions. */
        private PdfCanvas(float pageHeight, float scale) {

            this.pageHeight = pageHeight;
            this.scale = scale;
        }

        /** Adds a filled colored rectangle to the PDF. */
        private void fill(Color color, float x, float y, float width, float height) {

            setColor(color);

            commands.append(number(x * scale)).append(' ')
                    .append(number(pdfY(y + height))).append(' ')
                    .append(number(width * scale)).append(' ')
                    .append(number(height * scale)).append(" re f\n");
        }

        /** Adds a straight line to the PDF. */
        private void line(Color color, float width, float x1, float y1, float x2, float y2) {

            setColor(color);

            commands.append(number(width * scale)).append(" w ")
                    .append(number(x1 * scale)).append(' ')
                    .append(number(pdfY(y1))).append(" m ")
                    .append(number(x2 * scale)).append(' ')
                    .append(number(pdfY(y2))).append(" l S\n");
        }

        /** Adds selectable, sharp text to the PDF. */
        private void text(String font, float size, Color color, float x, float y, String text) {

            setColor(color);

            commands.append("BT /").append(font).append(' ')
                    .append(number(size * scale)).append(" Tf ")
                    .append(number(x * scale)).append(' ')
                    .append(number(pdfY(y))).append(" Td (")
                    .append(escapePdfText(text)).append(") Tj ET\n");
        }

        /** Sets the fill and line color used by the next drawing command. */
        private void setColor(Color color) {

            String red = number(color.getRed() / 255f);
            String green = number(color.getGreen() / 255f);
            String blue = number(color.getBlue() / 255f);

            commands.append(red).append(' ').append(green).append(' ').append(blue).append(" rg\n");
            commands.append(red).append(' ').append(green).append(' ').append(blue).append(" RG\n");
        }

        /** Converts top-down report coordinates to PDF's bottom-up coordinates. */
        private float pdfY(float topY) {

            return pageHeight - topY * scale;
        }

        /** Returns all PDF drawing commands for the page. */
        private String content() {

            return commands.toString();
        }
    }

    /** Escapes characters that have special meanings inside PDF text commands. */
    private static String escapePdfText(String value) {

        return value.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replaceAll("[^\\x20-\\x7E]", "?");
    }

    /** Formats a PDF measurement using a period as the decimal separator. */
    private static String number(float value) {

        return String.format(Locale.US, "%.2f", value);
    }
}
