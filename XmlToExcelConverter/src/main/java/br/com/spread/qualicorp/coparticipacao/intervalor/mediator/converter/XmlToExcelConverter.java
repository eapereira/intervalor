package br.com.spread.qualicorp.coparticipacao.intervalor.mediator.converter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import br.com.spread.qualicorp.coparticipacao.intervalor.mediator.converter.util.CellUtils;

public class XmlToExcelConverter {

	private static final Logger LOGGER = LogManager.getLogger(XmlToExcelConverter.class);

	public XmlToExcelConverter() {

	}

	public boolean getAndReadXml(String xml, String urlXlsx) {
		DocumentBuilderFactory dbFactory;
		DocumentBuilder dBuilder;
		InputSource is;
		Document doc;
		String nameSheet;
		FileOutputStream fileOut;
		NodeList planilhas;
		String spreadsheetName;
		Workbook workbook;

		try {
			LOGGER.info("BEGIN");
			LOGGER.info("<<<<< INICIANDO A GERAÇÃO DO ARQUIVO AGORA >>>>");
			dbFactory = DocumentBuilderFactory.newInstance();

			dBuilder = dbFactory.newDocumentBuilder();
			is = new InputSource(new StringReader(xml));
			doc = dBuilder.parse(is);

			planilhas = doc.getElementsByTagName("planilhas");

			nameSheet = createSheetName(planilhas);

			spreadsheetName = String.format("%s%s.xls", urlXlsx, nameSheet);

			LOGGER.info("Writing spreadsheet [{}]:", spreadsheetName);
			fileOut = new FileOutputStream(spreadsheetName);
			workbook = new HSSFWorkbook();

			createColumns(planilhas, workbook);

			workbook.write(fileOut);
			workbook.close();
			fileOut.close();

			LOGGER.info("END");
			return true;
		} catch (SAXException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (ParserConfigurationException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}

		return false;
	}

	private String createSheetName(NodeList planilhas) {
		String nameSheet = "";

		for (int i = 0; i < planilhas.getLength(); i++) {
			if (planilhas.item(i).getNodeType() == 1) {
				Element el = (Element) planilhas.item(i);
				nameSheet = el.getElementsByTagName("name").item(0).getTextContent();
			}
		}

		return nameSheet;
	}

	private void createColumns(NodeList planilhas, Workbook workbook) {
		CellUtils cellUtils = new CellUtils(workbook);
		int rowNum = 1;

		CellStyle styleHeader = workbook.createCellStyle();
		CellStyle styleBody = workbook.createCellStyle();
		CellStyle styleBodyLineLast = workbook.createCellStyle();
		Font fontHeader = workbook.createFont();
		Font fontBody = workbook.createFont();
		Sheet sheet = workbook.createSheet();
		sheet = workbook.getSheetAt(0);
		Row row = sheet.createRow(0);

		fontHeader.setBold(true);
		fontBody.setFontName("Calibri");
		fontHeader.setFontName("Calibri");
		fontHeader.setFontHeightInPoints((short) 12);
		fontBody.setFontHeightInPoints((short) 11);
		styleHeader.setFont(fontHeader);
		styleBody.setFont(fontBody);
		styleHeader.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());

		styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styleHeader.setBorderRight(BorderStyle.MEDIUM_DASH_DOT_DOT);
		styleHeader.setBorderTop(BorderStyle.MEDIUM_DASH_DOT_DOT);
		styleHeader.setBorderBottom(BorderStyle.MEDIUM);

		styleBody.setBorderRight(BorderStyle.THIN);
		styleBodyLineLast.setBorderRight(BorderStyle.THIN);
		styleBodyLineLast.setBorderBottom(BorderStyle.MEDIUM);

		for (int i = 0; i < planilhas.getLength(); i++) {
			Node node = planilhas.item(i);
			if (node.getNodeType() == 1) {
				Element element = (Element) node;
				NodeList nPlanilha = element.getElementsByTagName("planilha");
				for (int j = 0; j < nPlanilha.getLength(); j++) {
					if (nPlanilha.item(j).getNodeType() == 1) {
						Element el = (Element) nPlanilha.item(j);
						workbook.setSheetName(j, el.getElementsByTagName("name").item(0).getTextContent());
					}
					Node ndplanilha = nPlanilha.item(j);
					if (ndplanilha.getNodeType() == 1) {
						Element planilha = (Element) ndplanilha;
						NodeList nHedear = planilha.getElementsByTagName("header");
						for (int r = 0; r < nHedear.getLength(); r++) {
							if (nHedear.item(r).getNodeType() == 1) {
								Element el = (Element) nHedear.item(r);
								for (int k = 0; k < el.getChildNodes().getLength(); k++) {
									Cell cell = row.createCell(k);
									cell.setCellValue(el.getElementsByTagName("column" + (k + 1)).item(0)
											.getTextContent().toUpperCase());
									cell.setCellStyle(styleHeader);
								}
							}
						}

						NodeList nBody = element.getElementsByTagName("body");

						for (int m = 0; m < nPlanilha.getLength(); m++) {
							Node ndBody = nBody.item(m);
							if (ndBody.getNodeType() == 1) {
								Element body = (Element) ndBody;
								NodeList nInfo = body.getElementsByTagName("info");
								for (int n = 0; n < nInfo.getLength(); n++) {
									Element el = (Element) nInfo.item(n);
									row = sheet.createRow(rowNum++);

									LOGGER.info("Register: [{}]", n);

									for (int o = 0; o < el.getChildNodes().getLength(); o++) {
										sheet.autoSizeColumn(o);
										Cell cell = row.createCell(o);
										String valor = el.getElementsByTagName("column" + (o + 1)).item(0)
												.getTextContent();

										cellUtils.defineCellValue(cell, valor);
									}
								}
							}
						}
					}
				}
			}
		}
	}

}
