import os
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
)
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.pdfgen import canvas

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_header_footer(num_pages)
            super().showPage()
        super().save()

    def draw_header_footer(self, page_count):
        self.saveState()
        self.setFont("Helvetica-Bold", 8)
        self.setFillColor(colors.HexColor("#246B50"))

        # Header (on pages > 1)
        if self._pageNumber > 1:
            self.drawString(54, 750, "BillSplit — End-to-End Project Documentation & Viva Voce Guide")
            self.setStrokeColor(colors.HexColor("#CBD5E1"))
            self.setLineWidth(0.5)
            self.line(54, 742, 558, 742)

        # Footer
        self.setFont("Helvetica", 8)
        self.setFillColor(colors.HexColor("#64748B"))
        self.drawString(54, 36, "Confidential — Prepared for Academic Viva Voce Examination")
        page_str = f"Page {self._pageNumber} of {page_count}"
        self.drawRightString(558, 36, page_str)
        self.setStrokeColor(colors.HexColor("#CBD5E1"))
        self.setLineWidth(0.5)
        self.line(54, 48, 558, 48)
        self.restoreState()

def build_pdf():
    pdf_filename = "D:/projects/BillSplit/BillSplit_Project_Documentation_and_Viva_Guide.pdf"
    doc = SimpleDocTemplate(
        pdf_filename,
        pagesize=letter,
        leftMargin=54,
        rightMargin=54,
        topMargin=54,
        bottomMargin=54
    )

    styles = getSampleStyleSheet()

    # Custom Palette
    PRIMARY = colors.HexColor("#246B50")
    SECONDARY = colors.HexColor("#172033")
    ACCENT = colors.HexColor("#E0F2E9")
    TEXT_DARK = colors.HexColor("#0F172A")
    TEXT_MUTED = colors.HexColor("#475569")
    BORDER_COLOR = colors.HexColor("#CBD5E1")
    CODE_BG = colors.HexColor("#F8FAFC")

    title_style = ParagraphStyle(
        'DocTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=26,
        leading=32,
        textColor=PRIMARY,
        spaceAfter=6
    )

    subtitle_style = ParagraphStyle(
        'DocSubTitle',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=14,
        leading=18,
        textColor=TEXT_MUTED,
        spaceAfter=20
    )

    h1_style = ParagraphStyle(
        'Heading1_Custom',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=16,
        leading=20,
        textColor=PRIMARY,
        spaceBefore=16,
        spaceAfter=8,
        keepWithNext=True
    )

    h2_style = ParagraphStyle(
        'Heading2_Custom',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=12,
        leading=16,
        textColor=SECONDARY,
        spaceBefore=12,
        spaceAfter=6,
        keepWithNext=True
    )

    body_style = ParagraphStyle(
        'Body_Custom',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9.5,
        leading=14,
        textColor=TEXT_DARK,
        spaceAfter=6
    )

    bullet_style = ParagraphStyle(
        'Bullet_Custom',
        parent=body_style,
        leftIndent=15,
        firstLineIndent=-10,
        spaceAfter=4
    )

    code_style = ParagraphStyle(
        'Code_Custom',
        parent=styles['Normal'],
        fontName='Courier',
        fontSize=8.5,
        leading=11,
        textColor=colors.HexColor("#1E293B"),
        backColor=CODE_BG,
        borderColor=BORDER_COLOR,
        borderWidth=0.5,
        borderPadding=6,
        spaceBefore=6,
        spaceAfter=8
    )

    q_style = ParagraphStyle(
        'Question_Style',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=10,
        leading=14,
        textColor=PRIMARY,
        spaceBefore=8,
        spaceAfter=2,
        keepWithNext=True
    )

    a_style = ParagraphStyle(
        'Answer_Style',
        parent=body_style,
        leftIndent=10,
        spaceAfter=8
    )

    story = []

    # --- COVER / TITLE HEADER ---
    story.append(Spacer(1, 10))
    story.append(Paragraph("BillSplit — Project Documentation", title_style))
    story.append(Paragraph("Full End-to-End System Architecture, High-Level Design (HLD), Data Flow Diagrams (DFD), Database Schema, Technology Stack Justification & Viva Voce Preparation Guide", subtitle_style))
    story.append(HRFlowable(width="100%", thickness=2, color=PRIMARY, spaceBefore=0, spaceAfter=15))

    # Meta Table
    meta_data = [
        [Paragraph("<b>Project Name:</b> BillSplit", body_style), Paragraph("<b>Target Surface:</b> Android (Java & XML)", body_style)],
        [Paragraph("<b>Backend:</b> Node.js, Express.js, MongoDB", body_style), Paragraph("<b>Networking:</b> Retrofit 2 & Gson", body_style)],
        [Paragraph("<b>Architecture:</b> 3-Tier MVC / RESTful API", body_style), Paragraph("<b>Financial Precision:</b> Integer Paise Engine", body_style)]
    ]
    t_meta = Table(meta_data, colWidths=[250, 254])
    t_meta.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), ACCENT),
        ('BOX', (0,0), (-1,-1), 1, colors.HexColor("#A3E2C3")),
        ('PADDING', (0,0), (-1,-1), 8),
        ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ]))
    story.append(t_meta)
    story.append(Spacer(1, 15))

    # --- SECTION 1: EXECUTIVE SUMMARY & PROBLEM STATEMENT ---
    story.append(Paragraph("1. Executive Summary & Problem Statement", h1_style))
    story.append(Paragraph(
        "Managing shared expenses among roommates, flats, or group trips is a common problem. Traditional manual expense tracking using paper or spreadsheets often leads to mathematical errors, forgotten debts, payment disputes, and difficulty calculating exact multi-person splits.",
        body_style
    ))
    story.append(Paragraph(
        "<b>BillSplit</b> is an end-to-end multi-user expense splitter application. It enables users to record shared household costs, compute exact equal and custom percentage/amount splits without rounding errors, track net individual balances, simplify debt transactions, record payment settlements, and view detailed monthly spending analytics.",
        body_style
    ))
    story.append(Spacer(1, 10))

    # --- SECTION 2: HIGH-LEVEL DESIGN (HLD) ARCHITECTURE ---
    story.append(Paragraph("2. High-Level System Design (HLD)", h1_style))
    story.append(Paragraph(
        "The system follows a classic 3-Tier Distributed Client-Server Architecture:",
        body_style
    ))

    hld_diagram_text = (
        "+-----------------------------------------------------------------------------------+\n"
        "|                             CLIENT TIER (Android App)                             |\n"
        "|  [Activities & Views]  <--->  [SessionManager]  <--->  [Retrofit REST Client]     |\n"
        "+-----------------------------------------+-----------------------------------------+\n"
        "                                          | HTTP / HTTPS (JSON Payloads)\n"
        "                                          v\n"
        "+-----------------------------------------------------------------------------------+\n"
        "|                            APPLICATION TIER (Node.js API)                         |\n"
        "|  [Express Router] -> [Auth Middleware (JWT)] -> [Controllers] -> [Split Engine]   |\n"
        "+-----------------------------------------+-----------------------------------------+\n"
        "                                          | Mongoose ODM Queries\n"
        "                                          v\n"
        "+-----------------------------------------------------------------------------------+\n"
        "|                          PERSISTENCE TIER (MongoDB Server)                        |\n"
        "|  [Users Collection]   [Groups Collection]   [Expenses]   [Settlements]             |\n"
        "+-----------------------------------------------------------------------------------+"
    )
    story.append(Paragraph(hld_diagram_text.replace("\n", "<br/>").replace(" ", "&nbsp;"), code_style))

    story.append(Paragraph("<b>Component Description:</b>", h2_style))
    story.append(Paragraph("• <b>Presentation Layer (Android App)</b>: Built with Java and XML. Includes activities for Login, Register, Dashboard, Add/Edit Expense, Group Details, Settlement, Reports, and Profile.", bullet_style))
    story.append(Paragraph("• <b>Networking Layer</b>: Retrofit 2 handles async HTTP network calls with Gson converter for automatic JSON serialization/deserialization.", bullet_style))
    story.append(Paragraph("• <b>Session Layer</b>: SessionManager persists JWT authentication tokens and user profile state securely in Android SharedPreferences.", bullet_style))
    story.append(Paragraph("• <b>Application Layer (Node.js & Express)</b>: Receives API requests, enforces authentication middleware, validates request bodies, and executes business logic.", bullet_style))
    story.append(Paragraph("• <b>Database Layer (MongoDB)</b>: Document-oriented MongoDB database accessed via Mongoose ODM models.", bullet_style))

    story.append(Spacer(1, 10))

    # --- SECTION 3: DATA FLOW DIAGRAMS (DFD) ---
    story.append(Paragraph("3. Data Flow Diagrams (DFD)", h1_style))

    story.append(Paragraph("<b>DFD Level 0 — Context Diagram:</b>", h2_style))
    dfd0_text = (
        "                    +--------------------------------+ \n"
        "                    |          USER / CLIENT         | \n"
        "                    +---------------+----------------+ \n"
        "                                    |                  \n"
        "    1. Credentials, Expenses,       | 2. Auth Tokens, Balances,\n"
        "       Group Data, Settlements      |    Reports & Transaction Logs\n"
        "                                    v                  \n"
        "                    +---------------+----------------+ \n"
        "                    |     BILLSPLIT SYSTEM SYSTEM    | \n"
        "                    +---------------+----------------+ \n"
        "                                    |                  \n"
        "    3. Mongoose ODM Read/Write      | 4. Saved Documents\n"
        "                                    v                  \n"
        "                    +---------------+----------------+ \n"
        "                    |        MONGODB DATABASE        | \n"
        "                    +--------------------------------+ "
    )
    story.append(Paragraph(dfd0_text.replace("\n", "<br/>").replace(" ", "&nbsp;"), code_style))

    story.append(Paragraph("<b>DFD Level 1 — Macro Process Diagram:</b>", h2_style))
    story.append(Paragraph("• <b>Process 1.0 (User Auth)</b>: User enters name, email, password -> AuthController validates -> BCrypt hashes password -> JWT token generated -> Token returned to Client.", bullet_style))
    story.append(Paragraph("• <b>Process 2.0 (Group Management)</b>: User creates group -> GroupController validates members -> Saves Group Document -> Updates Active Group state.", bullet_style))
    story.append(Paragraph("• <b>Process 3.0 (Expense & Split Calculation)</b>: User inputs expense details and split method -> SplitCalculator computes paise shares -> Saves Expense Document -> Recalculates Net Member Balances.", bullet_style))
    story.append(Paragraph("• <b>Process 4.0 (Settlement Processing)</b>: Payer selects receiver and amount -> Settlement recorded -> Deducts outstanding debt -> Updates Dashboard balances instantly.", bullet_style))
    story.append(Paragraph("• <b>Process 5.0 (Reports Generation)</b>: Fetches group expenses -> Category totals & member spending aggregated -> Returns analytics summary.", bullet_style))

    story.append(Spacer(1, 10))

    # --- SECTION 4: TECH STACK & JUSTIFICATION ("WHAT WE USED & WHY") ---
    story.append(Paragraph("4. Technology Stack Justification", h1_style))

    tech_table_data = [
        [Paragraph("<b>Technology</b>", body_style), Paragraph("<b>Role / Purpose</b>", body_style), Paragraph("<b>Why It Was Chosen</b>", body_style)],
        [Paragraph("<b>Android Java & XML</b>", body_style), Paragraph("Mobile Frontend UI", body_style), Paragraph("Robust, industry-standard, backward compatible across Android API 24-36 without Kotlin runtime overhead.", body_style)],
        [Paragraph("<b>Retrofit 2 & Gson</b>", body_style), Paragraph("HTTP Network Client", body_style), Paragraph("Type-safe HTTP requests, automatic JSON-to-Java object conversion, asynchronous background execution.", body_style)],
        [Paragraph("<b>SharedPreferences</b>", body_style), Paragraph("Local Session Storage", body_style), Paragraph("Lightweight, encrypted, key-value storage ideal for persisting JWT auth tokens and active group settings.", body_style)],
        [Paragraph("<b>Node.js & Express</b>", body_style), Paragraph("REST API Backend", body_style), Paragraph("Non-blocking asynchronous I/O, rapid REST API development, native JSON handling.", body_style)],
        [Paragraph("<b>MongoDB & Mongoose</b>", body_style), Paragraph("NoSQL Database & ODM", body_style), Paragraph("Flexible document schema ideal for storing dynamic arrays of group members and expense split objects.", body_style)],
        [Paragraph("<b>JWT (JSON Web Token)</b>", body_style), Paragraph("Authentication Token", body_style), Paragraph("Stateless, secure, scalable session token passed in HTTP Authorization headers.", body_style)],
        [Paragraph("<b>BCrypt.js</b>", body_style), Paragraph("Password Hashing", body_style), Paragraph("Industry-standard salted password hashing algorithm preventing plain-text credential leaks.", body_style)]
    ]
    t_tech = Table(tech_table_data, colWidths=[120, 110, 274])
    t_tech.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), PRIMARY),
        ('TEXTCOLOR', (0,0), (-1,0), colors.white),
        ('GRID', (0,0), (-1,-1), 0.5, BORDER_COLOR),
        ('PADDING', (0,0), (-1,-1), 6),
        ('VALIGN', (0,0), (-1,-1), 'TOP'),
    ]))
    story.append(t_tech)
    story.append(Spacer(1, 15))

    # --- SECTION 5: FINANCIAL CALCULATIONS & ALGORITHMS ---
    story.append(Paragraph("5. Financial Engine & Algorithmic Design", h1_style))
    story.append(Paragraph(
        "<b>1. Integer Paise Representation:</b><br/>"
        "Floating-point representation (e.g., <code>float</code> / <code>double</code>) suffers from binary representation precision loss (e.g., 0.1 + 0.2 = 0.30000000000000004). To eliminate rounding errors, all monetary amounts are stored as integer <b>paise</b> (₹1.00 = 100 paise). Formatting to Rupees occurs solely in the UI display layer.",
        body_style
    ))
    story.append(Paragraph(
        "<b>2. Equal Split Remainder Distribution:</b><br/>"
        "When total paise cannot be divided evenly (e.g. ₹100.00 / 3 members = 10000 paise / 3 = 3333 paise with 1 paise remainder), the base share is 3333 paise, and the 1 paise remainder is distributed to the first member. Result: Member 1 = ₹33.34, Member 2 = ₹33.33, Member 3 = ₹33.33. Total sum = ₹100.00 exactly.",
        body_style
    ))
    story.append(Paragraph(
        "<b>3. Simplified Debts Algorithm (Greedy Settlement Minimization):</b><br/>"
        "Given net balances for group members, the system separates debtors (net balance &lt; 0) and creditors (net balance &gt; 0). It greedily pairs the largest debtor with the largest creditor, creating a minimal settlement transaction until all balances reach zero. This reduces unnecessary group payments.",
        body_style
    ))

    story.append(Spacer(1, 10))

    # --- SECTION 6: VIVA VOCE QUESTIONS & ANSWERS ---
    story.append(PageBreak())
    story.append(Paragraph("6. Viva Voce Examination — Questions & Model Answers", h1_style))
    story.append(Paragraph("Master these questions and responses for your viva voce exam:", body_style))
    story.append(Spacer(1, 6))

    viva_qa = [
        ("Q1: What is the main objective of the BillSplit project?",
         "BillSplit is designed to streamline shared household and trip expense tracking among roommates and groups. It eliminates manual calculation errors, provides paise-exact expense splitting, calculates net balances, minimizes debt transactions, and records payment settlements in real time."),

        ("Q2: Why did you choose Java and XML for Android instead of Kotlin or Jetpack Compose?",
         "Java and XML offer rock-solid stability, standard Android framework compatibility, explicit memory lifecycle management, and lightweight compilation across older and newer Android devices without additional Kotlin runtime dependencies."),

        ("Q3: Why did you store monetary values in integer paise instead of floating-point numbers?",
         "Floating-point data types (float/double) suffer from binary IEEE 754 precision loss during addition/subtraction. Storing currency in integer paise guarantees exact arithmetic, ensuring that the sum of all individual split shares equals the total expense amount down to the single paise."),

        ("Q4: How does your Equal Split algorithm handle remainder paise?",
         "When total paise is divided by the member count, integer division yields a base share and a remainder (remainder = totalPaise % memberCount). The algorithm adds 1 paise to the first 'remainder' members, ensuring sum(shares) == totalAmountPaise exactly."),

        ("Q5: Explain your Simplified Debts algorithm.",
         "The algorithm computes net balance for every member (total paid minus total share plus settlements). Members are separated into Debtors (net balance < 0) and Creditors (net balance > 0). It iteratively matches the largest debtor with the largest creditor, creating a single direct debt transaction for min(debt, credit), reducing total group transactions."),

        ("Q6: Why did you choose Node.js, Express, and MongoDB for the backend?",
         "Node.js with Express provides non-blocking asynchronous event-driven I/O ideal for REST APIs. MongoDB with Mongoose ODM offers a flexible document schema perfectly suited for dynamic JSON arrays of group members and individual split records."),

        ("Q7: How is authentication implemented in BillSplit?",
         "Authentication uses JWT (JSON Web Tokens) and BCrypt.js. When a user registers or logs in, BCrypt hashes/verifies the password. Upon successful validation, the server returns a signed JWT token, which the Android client stores in SessionManager (SharedPreferences) and attaches to subsequent HTTP Authorization headers."),

        ("Q8: How does Retrofit handle networking in the Android app?",
         "Retrofit 2 is a type-safe HTTP client. It uses Gson Converter to automatically serialize Java objects to JSON request bodies and deserialize backend JSON responses into Java model classes asynchronously on background threads."),

        ("Q9: What is the difference between an 'Expense' and a 'Settlement'?",
         "An Expense represents a shared cost (e.g. Rent, Groceries, Hotel) which increases group spending and splits costs among members. A Settlement represents a payment transaction where a debtor transfers money directly to a creditor to clear outstanding balance."),

        ("Q10: How does the Android app connect to the local backend server during development?",
         "On Android Emulators, '10.0.2.2' bridges to host localhost. On physical mobile devices, ADB Reverse Port Forwarding ('adb reverse tcp:5000 tcp:5000') or the computer's local Wi-Fi IP address ('http://10.194.36.146:5000/') routes HTTP requests directly over USB or Wi-Fi."),

        ("Q11: How do you handle email case-sensitivity during login/registration?",
         "Both the Android client and the backend authController normalize email strings by stripping whitespace (.trim()) and converting text to lowercase (.toLowerCase()) prior to MongoDB queries and BCrypt checks."),

        ("Q12: How do you prevent keyboard focus loss when typing custom split amounts?",
         "In AddExpenseActivity, setupCustomInputs() checks a mode key (currentCustomMode) so it only inflates member EditText fields when member count or split mode changes. This prevents re-inflating views on every keystroke, maintaining focus and smooth typing.")
    ]

    for q, a in viva_qa:
        story.append(Paragraph(q, q_style))
        story.append(Paragraph(a, a_style))

    # Build Document
    doc.build(story, canvasmaker=NumberedCanvas)
    print("PDF generated successfully at:", pdf_filename)

if __name__ == "__main__":
    build_pdf()
