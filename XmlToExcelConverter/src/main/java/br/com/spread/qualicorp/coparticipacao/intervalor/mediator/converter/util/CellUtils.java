package br.com.spread.qualicorp.coparticipacao.intervalor.mediator.converter.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;

/**
 * 
 * @author <a href="mailto: edson.apereira@spread.co.br">Edson Alves Pereira</a>
 *
 */
public class CellUtils {

	private static final Logger LOGGER = LogManager.getLogger(CellUtils.class);

	private static final Pattern DOUBLE_REGEXP = Pattern.compile("^([0-9]*)(\\.|\\,)([0-9]*)$");

	private static final Pattern INTEGER_REGEXP = Pattern.compile("^([0-9]+)$");

	private static final Pattern DATE_REGEXP = Pattern.compile("^([0-9]{2})\\/([0-9]{2})\\/([0-9]{4})$");

	private static final int CPF_LENGTH = 11;

	private HSSFWorkbook workbook;

	public CellUtils(HSSFWorkbook workbook) {
		this.workbook = workbook;
	}

	public void defineCellValue(HSSFCell cell, String value) {
		CellStyle cellStyle;
		DataFormat dataFormat;

		try {
			if (isDate(value)) {
				LOGGER.info("Discovering proper type for [{}] - date:", value);

				cellStyle = workbook.createCellStyle();
				dataFormat = workbook.createDataFormat();
				cellStyle.setDataFormat(dataFormat.getFormat("dd/MM/yyyy"));
				cell.setCellStyle(cellStyle);
				cell.setCellValue(stringToDate(value));
			} else if (isDouble(value)) {
				LOGGER.info("Discovering proper type for [{}] - double:", value);

				cellStyle = workbook.createCellStyle();
				dataFormat = workbook.createDataFormat();
				cellStyle.setDataFormat(dataFormat.getFormat("#0.00"));
				cell.setCellStyle(cellStyle);
				cell.setCellValue(stringToDouble(value));
			} else if (isInteger(value)) {
				cellStyle = workbook.createCellStyle();
				dataFormat = workbook.createDataFormat();

				if (isCpf(value)) {
					LOGGER.info("Discovering proper type for [{}] - CPF:", value);
					cellStyle.setDataFormat(dataFormat.getFormat("00000000000"));
				} else {
					LOGGER.info("Discovering proper type for [{}] - integer:", value);
					cellStyle.setDataFormat(dataFormat.getFormat("#0"));
				}

				cell.setCellStyle(cellStyle);
				cell.setCellValue(stringToLong(value));
			} else {
				LOGGER.info("Discovering proper type for [{}] - string:", value);

				cell.setCellValue(value);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}

	}

	private boolean isCpf(String value) {
		if (value.length() == CPF_LENGTH) {
			return true;
		}

		return false;
	}

	private boolean isDouble(String value) {
		Matcher matcher;

		if (StringUtils.isNotBlank(value)) {
			matcher = DOUBLE_REGEXP.matcher(value);

			if (matcher.find()) {
				return true;
			}
		}

		return false;
	}

	private boolean isDate(String value) {
		Matcher matcher;

		if (StringUtils.isNotBlank(value)) {
			matcher = DATE_REGEXP.matcher(value);

			if (matcher.find()) {
				return true;
			}
		}

		return false;
	}

	private boolean isInteger(String value) {
		Matcher matcher;

		if (StringUtils.isNotBlank(value)) {
			matcher = INTEGER_REGEXP.matcher(value);

			if (matcher.find()) {
				return true;
			}
		}

		return false;
	}

	private Double stringToDouble(String value) throws Exception {
		DecimalFormat decimalFormat;
		Double doubleValue;

		try {
			decimalFormat = new DecimalFormat("#0.00");

			doubleValue = decimalFormat.parse(value).doubleValue();

			return doubleValue;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw e;
		}
	}

	private Date stringToDate(String value) throws Exception {
		SimpleDateFormat dateFormat;
		Date dateValue;

		try {
			dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			dateValue = dateFormat.parse(value);

			return dateValue;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw e;
		}
	}

	private Long stringToLong(String value) throws Exception {
		Long longValue;

		try {
			longValue = NumberUtils.toLong(value);

			return longValue;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw e;
		}
	}
}
