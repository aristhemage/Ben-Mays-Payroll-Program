import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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

    private static final Color BACKGROUND =
            new Color(255, 255, 255);

    private static final Color TEXT =
            new Color(0, 0, 0);

    private static final Color LINE =
            new Color(60, 60, 60);

    private static final Color LIGHT_LINE =
            new Color(215, 215, 215);

    private static final Color HEADER =
            new Color(255, 255, 255);


    // ==========================================
    // GENERATE CHECK
    // ==========================================

    public static void generateCheck(
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

        BufferedImage image =
                new BufferedImage(
                        WIDTH,
                        HEIGHT,
                        BufferedImage.TYPE_INT_RGB
                );

        Graphics2D g =
                image.createGraphics();


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

        g.fillRect(
                0,
                0,
                WIDTH,
                HEIGHT
        );


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
        // FINISH
        // ==========================================

        g.dispose();


        try {

            ImageIO.write(
                    image,
                    "png",
                    new File("GeneratedCheck.png")
            );

            System.out.println(
                    "Created GeneratedCheck.png"
            );

        } catch (IOException e) {

            e.printStackTrace();
        }
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

        // ==========================================
        // DATE
        // ==========================================

        drawRight(
                g,
                pay_date,
                561,
                49,
                15,
                Font.PLAIN
        );


        // ==========================================
        // EMPLOYEE NAME
        // ==========================================

        drawText(
                g,
                name,
                70,
                83,
                17,
                Font.PLAIN
        );


        // ==========================================
        // NET PAY
        // ==========================================

        drawRight(
                g,
                String.format(
                        "$%,.2f",
                        cur_net
                ),
                561,
                92,
                16,
                Font.BOLD
        );


        // ==========================================
        // AMOUNT IN WORDS
        // ==========================================

        drawText(
                g,
                NumberToWords.convert(cur_net),
                88,
                115,
                16,
                Font.PLAIN
        );


        // ==========================================
        // PAY PERIOD
        // ==========================================

        drawText(
                g,
                "PP " +
                        pay_period +
                        ": " +
                        start_date +
                        " - " +
                        end_date,
                45,
                184,
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
        int y = 253;
        int width = 542;
        int height = 233;


        drawBorder(
                g,
                x,
                y,
                width,
                height
        );


        // ==========================================
        // HEADER
        // ==========================================

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


        // ==========================================
        // EMPLOYEE NAME
        // ==========================================

        drawText(
                g,
                name,
                32,
                294,
                11,
                Font.BOLD
        );


        // ==========================================
        // PAY PERIOD
        // ==========================================

        drawText(
                g,
                "PP " +
                        pay_period +
                        ": " +
                        start_date +
                        " - " +
                        end_date,
                32,
                311,
                8,
                Font.PLAIN
        );


        // ==========================================
        // EARNINGS
        // ==========================================

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


        // ==========================================
        // ADDRESS
        // ==========================================

        drawTopAddress(
                g,
                name,
                address,
                city,
                zip
        );


        // ==========================================
        // DEDUCTIONS
        // ==========================================

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


        drawText(
                g,
                "EARNINGS",
                32,
                331,
                8,
                Font.BOLD
        );

        drawRight(
                g,
                "HOURS",
                307,
                331,
                8,
                Font.BOLD
        );

        drawRight(
                g,
                "CURRENT",
                411,
                331,
                8,
                Font.BOLD
        );

        drawRight(
                g,
                "YTD",
                512,
                331,
                8,
                Font.BOLD
        );


        drawLine(
                g,
                left,
                335,
                right,
                335
        );


        // ==========================================
        // REGULAR PAY
        // ==========================================

        drawText(
                g,
                "Regular Pay",
                32,
                349,
                8,
                Font.PLAIN
        );

        drawRight(
                g,
                String.format(
                        "%.2f",
                        reg_hours
                ),
                307,
                349,
                8,
                Font.PLAIN
        );

        drawRight(
                g,
                String.format(
                        "$%,.2f",
                        reg_pay
                ),
                411,
                349,
                8,
                Font.PLAIN
        );

        drawRight(
                g,
                String.format(
                        "$%,.2f",
                        reg_ytd
                ),
                512,
                349,
                8,
                Font.PLAIN
        );


        // ==========================================
        // OVERTIME PAY
        // ==========================================

        drawText(
                g,
                "Overtime Pay",
                32,
                360,
                8,
                Font.PLAIN
        );

        drawRight(
                g,
                String.format(
                        "%.2f",
                        ot_hours
                ),
                307,
                360,
                8,
                Font.PLAIN
        );

        drawRight(
                g,
                String.format(
                        "$%,.2f",
                        ot_pay
                ),
                411,
                360,
                8,
                Font.PLAIN
        );

        drawRight(
                g,
                String.format(
                        "$%,.2f",
                        ot_ytd
                ),
                512,
                360,
                8,
                Font.PLAIN
        );


        // ==========================================
        // BONUS / OTHER
        // ==========================================

        drawText(
                g,
                "Bonus / Other",
                32,
                371,
                8,
                Font.PLAIN
        );

        drawRight(
                g,
                "$" + bonus,
                411,
                371,
                8,
                Font.PLAIN
        );

        drawRight(
                g,
                "$" + bonus_ytd,
                512,
                371,
                8,
                Font.PLAIN
        );


        // ==========================================
        // GROSS PAY
        // ==========================================

        drawText(
                g,
                "Gross Pay",
                32,
                382,
                8,
                Font.PLAIN
        );

        drawRight(
                g,
                String.format(
                        "$%,.2f",
                        reg_pay + ot_pay
                ),
                411,
                382,
                8,
                Font.PLAIN
        );

        drawRight(
                g,
                String.format(
                        "$%,.2f",
                        reg_ytd + ot_ytd
                ),
                512,
                382,
                8,
                Font.PLAIN
        );
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


        drawText(
                g,
                name,
                43,
                425,
                12,
                Font.BOLD
        );

        drawText(
                g,
                address,
                43,
                442,
                11,
                Font.PLAIN
        );

        drawText(
                g,
                city + ", " + zip,
                43,
                457,
                11,
                Font.PLAIN
        );
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


        drawBorder(
                g,
                x,
                y,
                width,
                height
        );


        drawText(
                g,
                "DEDUCTIONS",
                364,
                409,
                7,
                Font.BOLD
        );

        drawRight(
                g,
                "CURRENT",
                492,
                409,
                7,
                Font.BOLD
        );

        drawRight(
                g,
                "YTD",
                545,
                409,
                7,
                Font.BOLD
        );


        drawLine(
                g,
                362,
                413,
                548,
                413
        );


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


        for (
                int i = 0;
                i < labels.length;
                i++
        ) {

            int textY =
                    startY + i * 9;


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
                    String.format(
                            "$%,.2f",
                            currentValues[i]
                    ),
                    492,
                    textY,
                    6,
                    Font.PLAIN
            );


            drawRight(
                    g,
                    String.format(
                            "$%,.2f",
                            ytdValues[i]
                    ),
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


        drawBorder(
                g,
                x,
                y,
                width,
                height
        );


        g.setColor(HEADER);

        g.fillRect(
                x + 1,
                y + 1,
                width - 2,
                24
        );


        // ==========================================
        // HEADER
        // ==========================================

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


        // ==========================================
        // EMPLOYEE
        // ==========================================

        drawText(
                g,
                name,
                32,
                540,
                11,
                Font.BOLD
        );


        // ==========================================
        // PAY PERIOD
        // ==========================================

        drawText(
                g,
                "PP " +
                        pay_period +
                        ": " +
                        start_date +
                        " - " +
                        end_date,
                32,
                556,
                8,
                Font.PLAIN
        );


        // ==========================================
        // EARNINGS
        // ==========================================

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


        // ==========================================
        // DEDUCTIONS
        // ==========================================

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


        drawText(
                g,
                "EARNINGS",
                32,
                577,
                8,
                Font.BOLD
        );

        drawRight(
                g,
                "HOURS",
                307,
                577,
                8,
                Font.BOLD
        );

        drawRight(
                g,
                "CURRENT",
                411,
                577,
                8,
                Font.BOLD
        );

        drawRight(
                g,
                "YTD",
                512,
                577,
                8,
                Font.BOLD
        );


        drawLine(
                g,
                left,
                581,
                right,
                581
        );


        // ==========================================
        // REGULAR
        // ==========================================

        drawText(
                g,
                "Regular Pay",
                32,
                596,
                8,
                Font.PLAIN
        );

        drawRight(
                g,
                String.format(
                        "%.2f",
                        reg_hours
                ),
                307,
                596,
                8,
                Font.PLAIN
        );

        drawRight(
                g,
                String.format(
                        "$%,.2f",
                        reg_pay
                ),
                411,
                596,
                8,
                Font.PLAIN
        );

        drawRight(
                g,
                String.format(
                        "$%,.2f",
                        reg_ytd
                ),
                512,
                596,
                8,
                Font.PLAIN
        );


        // ==========================================
        // OVERTIME
        // ==========================================

        drawText(
                g,
                "Overtime Pay",
                32,
                607,
                8,
                Font.PLAIN
        );

        drawRight(
                g,
                String.format(
                        "%.2f",
                        ot_hours
                ),
                307,
                607,
                8,
                Font.PLAIN
        );

        drawRight(
                g,
                String.format(
                        "$%,.2f",
                        ot_pay
                ),
                411,
                607,
                8,
                Font.PLAIN
        );

        drawRight(
                g,
                String.format(
                        "$%,.2f",
                        ot_ytd
                ),
                512,
                607,
                8,
                Font.PLAIN
        );


        // ==========================================
        // BONUS
        // ==========================================

        drawText(
                g,
                "Bonus / Other",
                32,
                618,
                8,
                Font.PLAIN
        );

        drawRight(
                g,
                "$" + cur_bonus,
                411,
                618,
                8,
                Font.PLAIN
        );

        drawRight(
                g,
                "$" +bonus_ytd,
                512,
                618,
                8,
                Font.PLAIN
        );


        // ==========================================
        // GROSS
        // ==========================================

        drawText(
                g,
                "Gross Pay",
                32,
                629,
                8,
                Font.PLAIN
        );

        drawRight(
                g,
                String.format(
                        "$%,.2f",
                        reg_pay + ot_pay
                ),
                411,
                629,
                8,
                Font.PLAIN
        );

        drawRight(
                g,
                String.format(
                        "$%,.2f",
                        reg_ytd + ot_ytd
                ),
                512,
                629,
                8,
                Font.PLAIN
        );
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


        drawText(
                g,
                "DEDUCTIONS",
                32,
                648,
                8,
                Font.BOLD
        );


        drawRight(
                g,
                "CURRENT",
                411,
                648,
                8,
                Font.BOLD
        );


        drawRight(
                g,
                "YTD",
                512,
                648,
                8,
                Font.BOLD
        );


        drawLine(
                g,
                left,
                652,
                right,
                652
        );


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


        for (
                int i = 0;
                i < labels.length;
                i++
        ) {

            int textY =
                    startY + i * 11;


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
                    String.format(
                            "$%,.2f",
                            currentValues[i]
                    ),
                    411,
                    textY,
                    8,
                    Font.PLAIN
            );


            drawRight(
                    g,
                    String.format(
                            "$%,.2f",
                            ytdValues[i]
                    ),
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

        g.setFont(
                new Font(
                        "Arial",
                        style,
                        size
                )
        );

        g.drawString(
                text,
                x,
                y
        );
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

        g.setFont(
                new Font(
                        "Arial",
                        style,
                        size
                )
        );

        FontMetrics metrics =
                g.getFontMetrics();

        int width =
                metrics.stringWidth(text);

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

        g.setFont(
                new Font(
                        "Arial",
                        style,
                        size
                )
        );

        FontMetrics metrics =
                g.getFontMetrics();

        int width =
                metrics.stringWidth(text);

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

        g.setStroke(
                new BasicStroke(1)
        );

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

        g.setStroke(
                new BasicStroke(1)
        );

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

        Stroke oldStroke =
                g.getStroke();


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


        g.setStroke(
                oldStroke
        );
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

        Stroke oldStroke =
                g.getStroke();


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


        g.setStroke(
                oldStroke
        );
    }

}