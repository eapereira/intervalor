package br.com.spread.qualicorp.coparticipacao.intervalor.mediator;

import org.apache.axiom.soap.SOAPBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;

import br.com.spread.qualicorp.coparticipacao.intervalor.mediator.converter.XmlToExcelConverter;

public class XmlToExcelMediator extends AbstractMediator {

	private static final Logger LOGGER = LogManager.getLogger(XmlToExcelMediator.class);

	private String urlDestiny;

	public boolean mediate(MessageContext messageContext) {
		boolean result = false;
		LOGGER.info("<<< REALIZANDO A CHAMADO DA CLASSE XmlToExcelConverter >>>");

		XmlToExcelConverter xmlToExcell = new XmlToExcelConverter();
		SOAPBody body = messageContext.getEnvelope().getBody();
		String bodyString = body.toString();
		String xml = bodyString.substring(bodyString.indexOf("<planilhas>"), bodyString.indexOf("</soapenv:Body>"));

		if ((xml.isEmpty()) || (xml.length() == 0)) {
			LOGGER.info("Parametros nao informados");
			return false;
		}

		result = xmlToExcell.getAndReadXml(xml, this.urlDestiny);

		if (result) {
			LOGGER.info("Leitura efetuada com sucesso:");
		} else {
			LOGGER.info("Existem erros durante o processamento:");
		}
		
		return result;
	}

	public String getUrlDestiny() {
		return this.urlDestiny;
	}

	public void setUrlDestiny(String urlDestiny) {
		this.urlDestiny = urlDestiny;
	}
}
