import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PayrollCheckGenerator {

    // ==========================================
    // IMAGE SIZE
    // ==========================================

    // US Letter paper at 300 DPI
    private static final int WIDTH = 2550;
    private static final int HEIGHT = 3300;

    // Original design size
    private static final double DESIGN_WIDTH = 584.0;
    private static final double DESIGN_HEIGHT = 755.0;

    private static final Color BACKGROUND = new Color(255, 255, 255);
    private static final Color TEXT = new Color(0, 0, 0);
    private static final Color LINE = new Color(60, 60, 60);
    private static final Color LIGHT_LINE = new Color(215, 215, 215);
    private static final Color HEADER = new Color(255, 255, 255);


    // ==========================================
    // GENERATE CHECK
    // ==========================================

    public static File generateChecks(
            EmployeeManager employeeManager,
            PayrollTableManager tableManager,
            int selectedIndex
    ) throws IOException {

        boolean[] currentPeriod = new boolean[PayrollData.PAY_PERIODS.length];
        currentPeriod[selectedIndex] = true;

        boolean[] ytdPeriods = new boolean[PayrollData.PAY_PERIODS.length];
        for (int period = 0; period <= selectedIndex; period++) {
            ytdPeriods[period] = true;
        }

        List<BufferedImage> pages = new ArrayList<>();
        String payDate = PayrollData.PAY_PERIODS[selectedIndex][3];
        String displayPayDate = formatCheckDate(payDate);

        for (Employee employee : employeeManager.getEmployees()) {

            Map<String, Double> current = tableManager.calculateEmployeeTotals(employee, currentPeriod);
            Map<String, Double> ytd = tableManager.calculateEmployeeTotals(employee, ytdPeriods);

            pages.add(renderCheck(
                    employee.name,
                    displayPayDate,
                    Integer.parseInt(PayrollData.PAY_PERIODS[selectedIndex][0]),
                    PayrollData.PAY_PERIODS[selectedIndex][1],
                    PayrollData.PAY_PERIODS[selectedIndex][2],
                    employee.address, employee.city, employee.zip,
                    current.get("Hours"), current.get("OT Hours"),
                    current.get("Regular Pay"), current.get("OT Pay"),
                    ytd.get("Regular Pay"), ytd.get("OT Pay"),
                    current.get("Federal"), current.get("Social Security"), current.get("Medicare"), current.get("SLG Tax"),
                    current.get("Total Deductions"), current.get("Net Pay"),
                    ytd.get("Federal"), ytd.get("Social Security"), ytd.get("Medicare"), ytd.get("SLG Tax"),
                    ytd.get("Total Deductions"), ytd.get("Net Pay"),
                    current.get("Bonus"), ytd.get("Bonus")
            ));
        }

        if (pages.isEmpty()) {
            throw new IOException("There are no employees to include in the check PDF.");
        }

        File directory = new File(System.getProperty("user.home"), "Documents/Payroll Manager/Checks");
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create checks folder: " + directory.getAbsolutePath());
        }

        String safePayDate = payDate.replaceAll("[\\\\/:*?\"<>|]", "_");
        File output = new File(directory, "Payroll Checks - PP " + (selectedIndex + 1) + " - " + safePayDate + ".pdf");
        writeChecksPdf(pages, output);
        return output;
    }

    // Matches the long check-date style shown on the approved check layout.
    private static String formatCheckDate(String payDate) {

        try {
            LocalDate date = LocalDate.parse(payDate, DateTimeFormatter.ofPattern("M/d/yy"));
            return date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
        } catch (Exception ignored) {
            return payDate;
        }
    }

    private static BufferedImage renderCheck(
            String name,
            String pay_date,
            int pay_period,
            String start_date,
            String end_date,
            String address,
            String city,
            String zip,
            double reg_hours,
            double ot_hours,
            double reg_pay,
            double ot_pay,
            double reg_ytd,
            double ot_ytd,
            double cur_fed,
            double cur_social,
            double cur_medicare,
            double cur_slg,
            double cur_deductions,
            double cur_net,
            double ytd_fed,
            double ytd_social,
            double ytd_medicare,
            double ytd_slg,
            double ytd_deductions,
            double ytd_net,
            double bonus,
            double ytd_bonus
    ) {

        BufferedImage image = new BufferedImage(
                WIDTH,
                HEIGHT,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g = image.createGraphics();


        // ==========================================
        // RENDERING SETTINGS
        // ==========================================

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        g.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );

        g.setRenderingHint(
                RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON
        );


        // ==========================================
        // BACKGROUND
        // ==========================================

        g.setColor(BACKGROUND);
        g.fillRect(0, 0, WIDTH, HEIGHT);


        // ==========================================
        // SCALE ORIGINAL DESIGN
        // ==========================================

        g.scale(
                WIDTH / DESIGN_WIDTH,
                HEIGHT / DESIGN_HEIGHT
        );


        // ==========================================
        // TOP CHECK INFORMATION
        // ==========================================

        drawCheck(
                g,
                name,
                pay_date,
                pay_period,
                start_date,
                end_date,
                cur_net
        );


        // ==========================================
        // FIRST TEAR LINE
        // ==========================================

        drawDashedLine(
                g,
                22,
                245,
                565,
                245
        );


        // ==========================================
        // TOP EARNINGS STATEMENT
        // ==========================================

        drawTopStatement(
                g,
                name,
                pay_date,
                pay_period,
                start_date,
                end_date,
                address,
                city,
                zip,
                reg_hours,
                ot_hours,
                reg_pay,
                ot_pay,
                reg_ytd,
                ot_ytd,
                cur_fed,
                cur_social,
                cur_medicare,
                cur_slg,
                cur_deductions,
                cur_net,
                ytd_fed,
                ytd_social,
                ytd_medicare,
                ytd_slg,
                ytd_deductions,
                ytd_net,
                bonus,
                ytd_bonus
        );


        // ==========================================
        // SECOND TEAR LINE
        // ==========================================

        drawDashedLine(
                g,
                22,
                491,
                565,
                491
        );


        // ==========================================
        // BOTTOM EARNINGS STATEMENT
        // ==========================================

        drawBottomStatement(
                g,
                name,
                pay_date,
                pay_period,
                start_date,
                end_date,
                reg_hours,
                ot_hours,
                reg_pay,
                ot_pay,
                reg_ytd,
                ot_ytd,
                cur_fed,
                cur_social,
                cur_medicare,
                cur_slg,
                cur_deductions,
                cur_net,
                ytd_fed,
                ytd_social,
                ytd_medicare,
                ytd_slg,
                ytd_deductions,
                ytd_net,
                bonus,
                ytd_bonus
        );


        // ==========================================
        // FINISH RENDERING
        // ==========================================

        g.dispose();


        return image;
    }


    // ==========================================
    // CHECK SECTION
    // ==========================================

    private static void drawCheck(
            Graphics2D g,
            String name,
            String pay_date,
            int pay_period,
            String start_date,
            String end_date,
            double cur_net
    ) {

        drawRight(
                g,
                pay_date,
                564,
                60,
                15,
                Font.PLAIN
        );

        drawText(
                g,
                name,
                70,
                94,
                17,
                Font.PLAIN
        );

        drawRight(
                g,
                String.format("%,.2f", cur_net),
                527,
                100,
                16,
                Font.BOLD
        );

        drawText(
                g,
                NumberToWords.convert(cur_net),
                54,
                120,
                16,
                Font.PLAIN
        );

        drawText(
                g,
                "PP " + pay_period + ": " + start_date + " - " + end_date,
                80,
                201,
                16,
                Font.PLAIN
        );
    }


    // ==========================================
    // TOP STATEMENT
    // ==========================================

    private static void drawTopStatement(
            Graphics2D g,
            String name,
            String pay_date,
            int pay_period,
            String start_date,
            String end_date,
            String address,
            String city,
            String zip,
            double reg_hours,
            double ot_hours,
            double reg_pay,
            double ot_pay,
            double reg_ytd,
            double ot_ytd,
            double cur_fed,
            double cur_social,
            double cur_medicare,
            double cur_slg,
            double cur_deductions,
            double cur_net,
            double ytd_fed,
            double ytd_social,
            double ytd_medicare,
            double ytd_slg,
            double ytd_deductions,
            double ytd_net,
            double cur_bonus,
            double ytd_bonus
    ) {

        int x = 21;
        int y = 258;
        int width = 542;
        int height = 223;

        drawBorder(g, x, y, width, height);

        g.setColor(HEADER);
        g.fillRect(
                x + 1,
                y + 1,
                width - 2,
                24
        );

        drawText(
                g,
                "Earnings Statement",
                32,
                269,
                12,
                Font.BOLD
        );

        drawCentered(
                g,
                pay_date,
                292,
                269,
                9,
                Font.PLAIN
        );

        drawText(
                g,
                name,
                32,
                294,
                11,
                Font.BOLD
        );

        drawText(
                g,
                "PP " + pay_period + ": " + start_date + " - " + end_date,
                32,
                311,
                8,
                Font.PLAIN
        );

        drawTopEarnings(
                g,
                reg_hours,
                ot_hours,
                reg_pay,
                ot_pay,
                reg_ytd,
                ot_ytd,
                cur_bonus,
                ytd_bonus
        );

        drawTopAddress(
                g,
                name,
                address,
                city,
                zip
        );

        drawTopDeductions(
                g,
                cur_fed,
                cur_social,
                cur_medicare,
                cur_slg,
                cur_deductions,
                cur_net,
                ytd_fed,
                ytd_social,
                ytd_medicare,
                ytd_slg,
                ytd_deductions,
                ytd_net
        );
    }


    // ==========================================
    // TOP EARNINGS
    // ==========================================

    private static void drawTopEarnings(
            Graphics2D g,
            double reg_hours,
            double ot_hours,
            double reg_pay,
            double ot_pay,
            double reg_ytd,
            double ot_ytd,
            double bonus,
            double bonus_ytd
    ) {

        int left = 32;
        int right = 544;

        drawText(g, "EARNINGS", 32, 331, 8, Font.BOLD);
        drawRight(g, "HOURS", 307, 331, 8, Font.BOLD);
        drawRight(g, "CURRENT", 411, 331, 8, Font.BOLD);
        drawRight(g, "YTD", 512, 331, 8, Font.BOLD);

        drawLine(g, left, 335, right, 335);

        drawText(g, "Regular Pay", 32, 349, 8, Font.PLAIN);
        drawRight(g, String.format("%.2f", reg_hours), 307, 349, 8, Font.PLAIN);
        drawRight(g, String.format("$%,.2f", reg_pay), 411, 349, 8, Font.PLAIN);
        drawRight(g, String.format("$%,.2f", reg_ytd), 512, 349, 8, Font.PLAIN);

        drawText(g, "Overtime Pay", 32, 360, 8, Font.PLAIN);
        drawRight(g, String.format("%.2f", ot_hours), 307, 360, 8, Font.PLAIN);
        drawRight(g, String.format("$%,.2f", ot_pay), 411, 360, 8, Font.PLAIN);
        drawRight(g, String.format("$%,.2f", ot_ytd), 512, 360, 8, Font.PLAIN);

        drawText(g, "Bonus / Other", 32, 371, 8, Font.PLAIN);
        drawRight(g, "$" + bonus, 411, 371, 8, Font.PLAIN);
        drawRight(g, "$" + bonus_ytd, 512, 371, 8, Font.PLAIN);

        drawText(g, "Gross Pay", 32, 382, 8, Font.PLAIN);
        drawRight(g, String.format("$%,.2f", reg_pay + ot_pay), 411, 382, 8, Font.PLAIN);
        drawRight(g, String.format("$%,.2f", reg_ytd + ot_ytd), 512, 382, 8, Font.PLAIN);
    }


    // ==========================================
    // TOP ADDRESS
    // ==========================================

    private static void drawTopAddress(
            Graphics2D g,
            String name,
            String address,
            String city,
            String zip
    ) {

        int x = 30;
        int y = 396;
        int width = 315;
        int height = 80;

        drawLightDashedBox(
                g,
                x,
                y,
                width,
                height
        );

        drawText(g, name, 43, 425, 12, Font.BOLD);
        drawText(g, address, 43, 442, 11, Font.PLAIN);
        drawText(g, city + ", " + zip, 43, 457, 11, Font.PLAIN);
    }


    // ==========================================
    // TOP DEDUCTIONS
    // ==========================================

    private static void drawTopDeductions(
            Graphics2D g,
            double cur_fed,
            double cur_social,
            double cur_medicare,
            double cur_slg,
            double cur_deductions,
            double cur_net,
            double ytd_fed,
            double ytd_social,
            double ytd_medicare,
            double ytd_slg,
            double ytd_deductions,
            double ytd_net
    ) {

        int x = 357;
        int y = 397;
        int width = 195;
        int height = 79;

        drawBorder(g, x, y, width, height);

        drawText(g, "DEDUCTIONS", 364, 409, 7, Font.BOLD);
        drawRight(g, "CURRENT", 492, 409, 7, Font.BOLD);
        drawRight(g, "YTD", 545, 409, 7, Font.BOLD);

        drawLine(g, 362, 413, 548, 413);

        String[] labels = {
                "Federal",
                "Social Security",
                "Medicare",
                "State & Local",
                "Total Deductions",
                "Net Pay"
        };

        double[] currentValues = {
                cur_fed,
                cur_social,
                cur_medicare,
                cur_slg,
                cur_deductions,
                cur_net
        };

        double[] ytdValues = {
                ytd_fed,
                ytd_social,
                ytd_medicare,
                ytd_slg,
                ytd_deductions,
                ytd_net
        };

        int startY = 424;

        for (int i = 0; i < labels.length; i++) {

            int textY = startY + i * 9;

            drawText(
                    g,
                    labels[i],
                    364,
                    textY,
                    6,
                    Font.PLAIN
            );

            drawRight(
                    g,
                    String.format("$%,.2f", currentValues[i]),
                    492,
                    textY,
                    6,
                    Font.PLAIN
            );

            drawRight(
                    g,
                    String.format("$%,.2f", ytdValues[i]),
                    545,
                    textY,
                    6,
                    Font.PLAIN
            );
        }
    }


    // ==========================================
    // BOTTOM STATEMENT
    // ==========================================

    private static void drawBottomStatement(
            Graphics2D g,
            String name,
            String pay_date,
            int pay_period,
            String start_date,
            String end_date,
            double reg_hours,
            double ot_hours,
            double reg_pay,
            double ot_pay,
            double reg_ytd,
            double ot_ytd,
            double cur_fed,
            double cur_social,
            double cur_medicare,
            double cur_slg,
            double cur_deductions,
            double cur_net,
            double ytd_fed,
            double ytd_social,
            double ytd_medicare,
            double ytd_slg,
            double ytd_deductions,
            double ytd_net,
            double cur_bonus,
            double ytd_bonus
    ) {

        int x = 21;
        int y = 499;
        int width = 542;
        int height = 233;

        drawBorder(g, x, y, width, height);

        g.setColor(HEADER);
        g.fillRect(
                x + 1,
                y + 1,
                width - 2,
                24
        );

        drawText(
                g,
                "Earnings Statement",
                32,
                515,
                12,
                Font.BOLD
        );

        drawCentered(
                g,
                pay_date,
                292,
                515,
                9,
                Font.PLAIN
        );

        drawText(
                g,
                name,
                32,
                540,
                11,
                Font.BOLD
        );

        drawText(
                g,
                "PP " + pay_period + ": " + start_date + " - " + end_date,
                32,
                556,
                8,
                Font.PLAIN
        );

        drawBottomEarnings(
                g,
                reg_hours,
                ot_hours,
                reg_pay,
                ot_pay,
                reg_ytd,
                ot_ytd,
                cur_bonus,
                ytd_bonus
        );

        drawBottomDeductions(
                g,
                cur_fed,
                cur_social,
                cur_medicare,
                cur_slg,
                cur_deductions,
                cur_net,
                ytd_fed,
                ytd_social,
                ytd_medicare,
                ytd_slg,
                ytd_deductions,
                ytd_net
        );
    }


    // ==========================================
    // BOTTOM EARNINGS
    // ==========================================

    private static void drawBottomEarnings(
            Graphics2D g,
            double reg_hours,
            double ot_hours,
            double reg_pay,
            double ot_pay,
            double reg_ytd,
            double ot_ytd,
            double cur_bonus,
            double bonus_ytd
    ) {

        int left = 32;
        int right = 544;

        drawText(g, "EARNINGS", 32, 577, 8, Font.BOLD);
        drawRight(g, "HOURS", 307, 577, 8, Font.BOLD);
        drawRight(g, "CURRENT", 411, 577, 8, Font.BOLD);
        drawRight(g, "YTD", 512, 577, 8, Font.BOLD);

        drawLine(g, left, 581, right, 581);

        drawText(g, "Regular Pay", 32, 596, 8, Font.PLAIN);
        drawRight(g, String.format("%.2f", reg_hours), 307, 596, 8, Font.PLAIN);
        drawRight(g, String.format("$%,.2f", reg_pay), 411, 596, 8, Font.PLAIN);
        drawRight(g, String.format("$%,.2f", reg_ytd), 512, 596, 8, Font.PLAIN);

        drawText(g, "Overtime Pay", 32, 607, 8, Font.PLAIN);
        drawRight(g, String.format("%.2f", ot_hours), 307, 607, 8, Font.PLAIN);
        drawRight(g, String.format("$%,.2f", ot_pay), 411, 607, 8, Font.PLAIN);
        drawRight(g, String.format("$%,.2f", ot_ytd), 512, 607, 8, Font.PLAIN);

        drawText(g, "Bonus / Other", 32, 618, 8, Font.PLAIN);
        drawRight(g, "$" + cur_bonus, 411, 618, 8, Font.PLAIN);
        drawRight(g, "$" + bonus_ytd, 512, 618, 8, Font.PLAIN);

        drawText(g, "Gross Pay", 32, 629, 8, Font.PLAIN);
        drawRight(g, String.format("$%,.2f", reg_pay + ot_pay), 411, 629, 8, Font.PLAIN);
        drawRight(g, String.format("$%,.2f", reg_ytd + ot_ytd), 512, 629, 8, Font.PLAIN);
    }


    // ==========================================
    // BOTTOM DEDUCTIONS
    // ==========================================

    private static void drawBottomDeductions(
            Graphics2D g,
            double cur_fed,
            double cur_social,
            double cur_medicare,
            double cur_slg,
            double cur_deductions,
            double cur_net,
            double ytd_fed,
            double ytd_social,
            double ytd_medicare,
            double ytd_slg,
            double ytd_deductions,
            double ytd_net
    ) {

        int left = 32;
        int right = 544;

        drawText(g, "DEDUCTIONS", 32, 648, 8, Font.BOLD);
        drawRight(g, "CURRENT", 411, 648, 8, Font.BOLD);
        drawRight(g, "YTD", 512, 648, 8, Font.BOLD);

        drawLine(g, left, 652, right, 652);

        String[] labels = {
                "Federal Withholding",
                "Social Security",
                "Medicare",
                "State & Local Tax",
                "Total Deductions",
                "Net Pay"
        };

        double[] currentValues = {
                cur_fed,
                cur_social,
                cur_medicare,
                cur_slg,
                cur_deductions,
                cur_net
        };

        double[] ytdValues = {
                ytd_fed,
                ytd_social,
                ytd_medicare,
                ytd_slg,
                ytd_deductions,
                ytd_net
        };

        int startY = 668;

        for (int i = 0; i < labels.length; i++) {

            int textY = startY + i * 11;

            drawText(
                    g,
                    labels[i],
                    32,
                    textY,
                    8,
                    Font.PLAIN
            );

            drawRight(
                    g,
                    String.format("$%,.2f", currentValues[i]),
                    411,
                    textY,
                    8,
                    Font.PLAIN
            );

            drawRight(
                    g,
                    String.format("$%,.2f", ytdValues[i]),
                    512,
                    textY,
                    8,
                    Font.PLAIN
            );
        }
    }


    // ==========================================
    // DRAW TEXT
    // ==========================================

    private static void drawText(
            Graphics2D g,
            String text,
            int x,
            int y,
            int size,
            int style
    ) {

        g.setColor(TEXT);
        g.setFont(new Font("Arial", style, size));
        g.drawString(text, x, y);
    }


    // ==========================================
    // DRAW RIGHT
    // ==========================================

    private static void drawRight(
            Graphics2D g,
            String text,
            int rightX,
            int y,
            int size,
            int style
    ) {

        g.setColor(TEXT);
        g.setFont(new Font("Arial", style, size));

        FontMetrics metrics = g.getFontMetrics();
        int width = metrics.stringWidth(text);

        g.drawString(
                text,
                rightX - width,
                y
        );
    }


    // ==========================================
    // DRAW CENTERED
    // ==========================================

    private static void drawCentered(
            Graphics2D g,
            String text,
            int centerX,
            int y,
            int size,
            int style
    ) {

        g.setColor(TEXT);
        g.setFont(new Font("Arial", style, size));

        FontMetrics metrics = g.getFontMetrics();
        int width = metrics.stringWidth(text);

        g.drawString(
                text,
                centerX - width / 2,
                y
        );
    }


    // ==========================================
    // DRAW LINE
    // ==========================================

    private static void drawLine(
            Graphics2D g,
            int x1,
            int y1,
            int x2,
            int y2
    ) {

        g.setColor(LINE);
        g.setStroke(new BasicStroke(1));
        g.drawLine(
                x1,
                y1,
                x2,
                y2
        );
    }


    // ==========================================
    // DRAW BORDER
    // ==========================================

    private static void drawBorder(
            Graphics2D g,
            int x,
            int y,
            int width,
            int height
    ) {

        g.setColor(LINE);
        g.setStroke(new BasicStroke(1));
        g.drawRect(
                x,
                y,
                width,
                height
        );
    }


    // ==========================================
    // DASHED LINE
    // ==========================================

    private static void drawDashedLine(
            Graphics2D g,
            int x1,
            int y1,
            int x2,
            int y2
    ) {

        Stroke oldStroke = g.getStroke();

        g.setStroke(
                new BasicStroke(
                        1,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER,
                        10,
                        new float[]{3, 3},
                        0
                )
        );

        g.setColor(LIGHT_LINE);

        g.drawLine(
                x1,
                y1,
                x2,
                y2
        );

        g.setStroke(oldStroke);
    }


    // ==========================================
    // LIGHT DASHED BOX
    // ==========================================

    private static void drawLightDashedBox(
            Graphics2D g,
            int x,
            int y,
            int width,
            int height
    ) {

        Stroke oldStroke = g.getStroke();

        g.setStroke(
                new BasicStroke(
                        1,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER,
                        10,
                        new float[]{2, 2},
                        0
                )
        );

        g.setColor(LIGHT_LINE);

        g.drawRect(
                x,
                y,
                width,
                height
        );

        g.setStroke(oldStroke);
    }

    // Writes one letter-sized PDF page per employee check.
    private static void writeChecksPdf(List<BufferedImage> pages, File output) throws IOException {

        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        pdf.write("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));

        int pageCount = pages.size();
        int objectCount = 4 + pageCount * 3;
        int[] offsets = new int[objectCount + 1];

        offsets[1] = pdf.size();
        write(pdf, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < pageCount; i++) {
            kids.append(5 + i * 3).append(" 0 R ");
        }
        offsets[2] = pdf.size();
        write(pdf, "2 0 obj\n<< /Type /Pages /Kids [" + kids + "] /Count " + pageCount + " >>\nendobj\n");

        offsets[3] = pdf.size();
        write(pdf, "3 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");
        offsets[4] = pdf.size();
        write(pdf, "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n");

        for (int i = 0; i < pageCount; i++) {
            int pageObject = 5 + i * 3;
            int contentObject = pageObject + 1;
            int imageObject = pageObject + 2;
            BufferedImage page = pages.get(i);
            ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
            ImageIO.write(page, "jpg", imageBytes);
            byte[] jpeg = imageBytes.toByteArray();

            String content = "q\n612 0 0 792 0 0 cm\n/Im" + i + " Do\nQ\n";

            offsets[pageObject] = pdf.size();
            write(pdf, pageObject + " 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /XObject << /Im" + i + " " + imageObject + " 0 R >> >> /Contents " + contentObject + " 0 R >>\nendobj\n");

            offsets[contentObject] = pdf.size();
            write(pdf, contentObject + " 0 obj\n<< /Length " + content.getBytes(StandardCharsets.US_ASCII).length + " >>\nstream\n" + content + "endstream\nendobj\n");

            offsets[imageObject] = pdf.size();
            write(pdf, imageObject + " 0 obj\n<< /Type /XObject /Subtype /Image /Width " + page.getWidth() + " /Height " + page.getHeight() + " /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length " + jpeg.length + " >>\nstream\n");
            pdf.write(jpeg);
            write(pdf, "\nendstream\nendobj\n");
        }

        int xref = pdf.size();
        write(pdf, "xref\n0 " + (objectCount + 1) + "\n0000000000 65535 f \n");
        for (int object = 1; object <= objectCount; object++) {
            write(pdf, String.format("%010d 00000 n \n", offsets[object]));
        }
        write(pdf, "trailer\n<< /Size " + (objectCount + 1) + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF\n");

        try (FileOutputStream stream = new FileOutputStream(output)) {
            pdf.writeTo(stream);
        }
    }

    private static void write(ByteArrayOutputStream output, String value) throws IOException {
        output.write(value.getBytes(StandardCharsets.US_ASCII));
    }
}
