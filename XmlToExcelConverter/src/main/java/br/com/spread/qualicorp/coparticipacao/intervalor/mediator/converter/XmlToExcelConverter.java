package br.com.spread.qualicorp.coparticipacao.intervalor.mediator.converter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import br.com.spread.qualicorp.coparticipacao.intervalor.mediator.converter.util.CellUtils;

public class XmlToExcelConverter {

	private static final Logger LOGGER = LogManager.getLogger(XmlToExcelConverter.class);

	private HSSFWorkbook workbook;
	private int rowNum;

	public XmlToExcelConverter() {
		workbook = new HSSFWorkbook();
	}

	public boolean getAndReadXml(String xml, String urlXlsx) {
		DocumentBuilderFactory dbFactory;
		DocumentBuilder dBuilder;
		InputSource is;
		Document doc;
		String nameSheet;
		FileOutputStream fileOut;

		try {
			LOGGER.info("BEGIN");
			LOGGER.info("<<<<< INICIANDO A GERAÇÃO DO ARQUIVO AGORA >>>>");
			dbFactory = DocumentBuilderFactory.newInstance();

			dBuilder = dbFactory.newDocumentBuilder();
			is = new InputSource(new StringReader(xml));
			doc = dBuilder.parse(is);
			nameSheet = createColumns(doc.getElementsByTagName("planilhas"));

			fileOut = new FileOutputStream(urlXlsx + nameSheet + ".xls");
			workbook.write(fileOut);
			workbook.close();
			fileOut.close();

			LOGGER.info("Leitura efetuada com sucesso:");
			LOGGER.info("END");
			return true;
		} catch (SAXException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (ParserConfigurationException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return false;
	}

	private String createColumns(NodeList planilhas) {
		String nameSheet = "";
		CellUtils cellUtils=new CellUtils(workbook);
		
		for (int i = 0; i < planilhas.getLength(); i++) {
			if (planilhas.item(i).getNodeType() == 1) {
				Element el = (Element) planilhas.item(i);
				nameSheet = el.getElementsByTagName("name").item(0).getTextContent();
			}
		}
		HSSFCellStyle styleHeader = workbook.createCellStyle();
		HSSFCellStyle styleBody = workbook.createCellStyle();
		HSSFCellStyle styleBodyLineLast = workbook.createCellStyle();
		HSSFFont fontHeader = workbook.createFont();
		HSSFFont fontBody = workbook.createFont();
		HSSFSheet sheet = workbook.createSheet();
		sheet = workbook.getSheetAt(0);
		HSSFRow row = sheet.createRow(0);

		fontHeader.setBold(true);
		fontBody.setFontName("Calibri");
		fontHeader.setFontName("Calibri");
		fontHeader.setFontHeightInPoints((short) 12);
		fontBody.setFontHeightInPoints((short) 11);
		styleHeader.setFont(fontHeader);
		styleBody.setFont(fontBody);
		HSSFPalette palette = workbook.getCustomPalette();
		HSSFColor myColor = palette.findSimilarColor(192, 192, 192);
		styleHeader.setFillForegroundColor(myColor.getIndex());

		styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styleHeader.setBorderRight(BorderStyle.MEDIUM_DASH_DOT_DOT);
		styleHeader.setBorderTop(BorderStyle.MEDIUM_DASH_DOT_DOT);
		styleHeader.setBorderBottom(BorderStyle.MEDIUM);

		styleBody.setBorderRight(BorderStyle.THIN);
		styleBodyLineLast.setBorderRight(BorderStyle.THIN);
		styleBodyLineLast.setBorderBottom(BorderStyle.MEDIUM);

		rowNum = 1;
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
									HSSFCell cell = row.createCell(k);
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
										HSSFCell cell = row.createCell(o);
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
		return nameSheet;
	}

}
