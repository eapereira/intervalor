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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Workbook;

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

	// private Workbook workbook;

	private CellStyle doubleCellStyle;

	private CellStyle dateCellStyle;

	private CellStyle longCellStyle;

	private CellStyle cpfCellStyle;

	public CellUtils(Workbook workbook) {
		// this.workbook = workbook;

		DataFormat dataFormat;

		dataFormat = workbook.createDataFormat();
		dateCellStyle = workbook.createCellStyle();
		dateCellStyle.setDataFormat(dataFormat.getFormat("dd/MM/yyyy"));

		dataFormat = workbook.createDataFormat();
		doubleCellStyle = workbook.createCellStyle();
		doubleCellStyle.setDataFormat(dataFormat.getFormat("#0.00"));

		dataFormat = workbook.createDataFormat();
		longCellStyle = workbook.createCellStyle();
		longCellStyle.setDataFormat(dataFormat.getFormat("#0"));

		dataFormat = workbook.createDataFormat();
		cpfCellStyle = workbook.createCellStyle();
		cpfCellStyle.setDataFormat(dataFormat.getFormat("00000000000"));
	}

	public void defineCellValue(Cell cell, String value) {
		try {
			if (isDate(value)) {
				LOGGER.info("Discovering proper type for [{}] - date:", value);

				cell.setCellStyle(dateCellStyle);
				cell.setCellValue(stringToDate(value));
			} else if (isDouble(value)) {
				LOGGER.info("Discovering proper type for [{}] - double:", value);

				cell.setCellStyle(doubleCellStyle);
				cell.setCellValue(stringToDouble(value));
			} else if (isInteger(value)) {
				if (isCpf(value)) {
					LOGGER.info("Discovering proper type for [{}] - CPF:", value);
					cell.setCellStyle(cpfCellStyle);
				} else {
					LOGGER.info("Discovering proper type for [{}] - integer:", value);
					cell.setCellStyle(longCellStyle);
				}

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
