"""Writes a ReportContent to a CSV or PDF file."""
import csv
from xml.sax.saxutils import escape

from reportlab.lib import colors
from reportlab.lib.pagesizes import A4, landscape
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import cm
from reportlab.platypus import Paragraph, SimpleDocTemplate, Spacer, Table, TableStyle

from app.exports.content import ReportContent

HEADER_BLUE = colors.HexColor("#1E4E8C")
ROW_TINT = colors.HexColor("#EEF3FA")


def write_csv(path: str, content: ReportContent) -> None:
    # utf-8-sig so Excel shows the degree sign correctly
    with open(path, "w", newline="", encoding="utf-8-sig") as file:
        writer = csv.writer(file)
        writer.writerow(content.columns)
        writer.writerows(content.rows)


def write_pdf(path: str, content: ReportContent) -> None:
    page_size = landscape(A4) if content.landscape else A4
    margin = 1.5 * cm
    doc = SimpleDocTemplate(path, pagesize=page_size, leftMargin=margin, rightMargin=margin,
                            topMargin=margin, bottomMargin=margin, title=content.title)
    styles = getSampleStyleSheet()
    cell = ParagraphStyle("cell", parent=styles["BodyText"], fontSize=9, leading=11)
    head = ParagraphStyle("head", parent=cell, textColor=colors.white, fontName="Helvetica-Bold")

    story = [Paragraph(escape(content.title), styles["Title"])]
    story += [Paragraph(escape(line), styles["Normal"]) for line in content.meta]
    story.append(Spacer(1, 0.6 * cm))

    if not content.rows:
        story.append(Paragraph("No data for this city and period.", styles["Italic"]))
    else:
        width = page_size[0] - 2 * margin
        col_width = width / len(content.columns)
        data = [[Paragraph(escape(c), head) for c in content.columns]]
        data += [[Paragraph(escape(str(v)), cell) for v in row] for row in content.rows]
        table = Table(data, colWidths=[col_width] * len(content.columns), repeatRows=1)
        style = [
            ("BACKGROUND", (0, 0), (-1, 0), HEADER_BLUE),
            ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
            ("GRID", (0, 0), (-1, -1), 0.4, colors.HexColor("#B8C4D6")),
            ("TOPPADDING", (0, 0), (-1, -1), 4),
            ("BOTTOMPADDING", (0, 0), (-1, -1), 4),
        ]
        style += [("BACKGROUND", (0, i), (-1, i), ROW_TINT) for i in range(2, len(data), 2)]
        table.setStyle(TableStyle(style))
        story.append(table)

    def footer(canvas, document):
        canvas.saveState()
        canvas.setFont("Helvetica", 8)
        canvas.setFillColor(colors.grey)
        canvas.drawString(margin, 0.8 * cm, "Meteo App - report-service")
        canvas.drawRightString(page_size[0] - margin, 0.8 * cm, f"Page {document.page}")
        canvas.restoreState()

    doc.build(story, onFirstPage=footer, onLaterPages=footer)
