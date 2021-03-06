package epam.project.app.logic.builder;

import epam.project.app.logic.entity.dto.ReportEditDto;
import epam.project.app.logic.entity.report.ReportTags;
import epam.project.app.logic.exception.ReportException;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@Log4j2
public class XmlBuilder implements Building {
    private final DocumentBuilderFactory factory;

    public XmlBuilder() {
        factory = DocumentBuilderFactory.newInstance();
    }

    @Override
    public boolean build(ReportEditDto reportEditDto, String path) {
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element root = doc.createElement(ReportTags.REPORT.getValue());
            root.setAttribute("xmlns:ns", "http://www.w3.org/2001/XMLSchema-instance");
            root.setAttribute("xmlns", "http://report.com/report");
            root.setAttribute("ns:schemaLocation", "http://report.com/report");
            doc.appendChild(root);
            Element person = doc.createElement(ReportTags.PERSON.getValue());
            person.appendChild(doc.createTextNode(reportEditDto.getPerson()));
            Element nationality = doc.createElement(ReportTags.NATIONALITY.getValue());
            nationality.appendChild(doc.createTextNode(reportEditDto.getNationality()));
            Element year = doc.createElement(ReportTags.YEAR.getValue());
            year.appendChild(doc.createTextNode(reportEditDto.getYear()));
            Element quarter = doc.createElement(ReportTags.QUARTER.getValue());
            quarter.appendChild(doc.createTextNode(reportEditDto.getQuarter()));
            Element month = doc.createElement(ReportTags.MONTH.getValue());
            month.appendChild(doc.createTextNode(reportEditDto.getMonth()));
            Element group = doc.createElement(ReportTags.GROUP.getValue());
            group.appendChild(doc.createTextNode(reportEditDto.getGroup()));
            Element activity = doc.createElement(ReportTags.ACTIVITY.getValue());
            activity.appendChild(doc.createTextNode(reportEditDto.getActivity()));
            Element income = doc.createElement(ReportTags.INCOME.getValue());
            income.appendChild(doc.createTextNode(reportEditDto.getIncome()));

            root.appendChild(person);
            root.appendChild(nationality);
            root.appendChild(year);
            root.appendChild(quarter);
            root.appendChild(month);
            root.appendChild(group);
            root.appendChild(activity);
            root.appendChild(income);

            write(doc, path);
        } catch (Exception e) {
            log.error("cannot build xml file", e);
            throw new ReportException("cannot edit report");
        }
        return true;
    }

    private void write(Document doc, String xmlFile) throws TransformerException, IOException {
        OutputStream outputStream = new FileOutputStream(xmlFile);
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        Transformer tr = factory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(outputStream);
        tr.transform(source, result);
        outputStream.close();
    }
}
