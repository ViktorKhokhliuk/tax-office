package epam.project.app.logic.controller;

import epam.project.app.infra.web.ModelAndView;
import epam.project.app.infra.web.QueryParameterResolver;
import epam.project.app.logic.entity.dto.ReportEditDto;
import epam.project.app.logic.entity.report.Report;
import epam.project.app.logic.entity.report.ReportParameters;
import epam.project.app.logic.entity.report.ReportStatus;
import epam.project.app.logic.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReportEditControllerTest {

    @Mock
    private ReportService reportService;
    @Mock
    private QueryParameterResolver queryParameterResolver;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ReportEditController reportEditController;

    private static final Long ID = 0L;
    private static final Long CLIENT_ID = 0L;
    private static final String TITLE_XML = "report.xml";
    private static final Integer PAGE = 1;
    private static final String DATE = "2022-01-01";
    private static final String TYPE = "income tax";

    @Test
    public void editReport() {
        ReportEditDto reportEditDto = new ReportEditDto();
        reportEditDto.setId(ID);
        reportEditDto.setClientId(CLIENT_ID);
        reportEditDto.setTitle(TITLE_XML);
        String path = "webapp/upload/id" + CLIENT_ID + "/" + TITLE_XML;
        ReportParameters reportParameters = new ReportParameters();

        when(queryParameterResolver.getObject(request, ReportEditDto.class)).thenReturn(reportEditDto);
        when(reportService.getReportParameters(path)).thenReturn(reportParameters);

        ModelAndView modelAndView = reportEditController.editReport(request);
        assertNotNull(modelAndView);
        assertFalse(modelAndView.isRedirect());
        assertEquals("/client/edit.jsp", modelAndView.getView());
        assertEquals(reportParameters, modelAndView.getAttributes().get("reportParameters"));
        assertEquals(reportEditDto, modelAndView.getAttributes().get("dto"));

        verify(reportService).getReportParameters(path);
    }

    @Test
    public void saveReportChangesWhenXmlFile() {
        ReportEditDto reportEditDto = new ReportEditDto();
        reportEditDto.setId(ID);
        reportEditDto.setClientId(CLIENT_ID);
        reportEditDto.setTitle(TITLE_XML);
        reportEditDto.setDate(DATE);
        reportEditDto.setPage(PAGE);
        reportEditDto.setStatusFilter(ReportStatus.ACCEPTED.getTitle());
        reportEditDto.setType(TYPE);
        String path = "webapp/upload/id" + CLIENT_ID + "/" + TITLE_XML;

        when(queryParameterResolver.getObject(request, ReportEditDto.class)).thenReturn(reportEditDto);
        when(reportService.saveReportChanges(reportEditDto, path)).thenReturn(true);
        when(reportService.updateStatusOfReportAfterEdit(ID)).thenReturn(new Report());

        ModelAndView modelAndView = reportEditController.saveReportChanges(request);
        assertNotNull(modelAndView);
        assertTrue(modelAndView.isRedirect());
        assertEquals("/service/filterClientReports", modelAndView.getView());
        assertEquals(DATE, modelAndView.getAttributes().get("date"));
        assertEquals(PAGE, modelAndView.getAttributes().get("page"));
        assertEquals(ReportStatus.ACCEPTED.getTitle(), modelAndView.getAttributes().get("status"));
        assertEquals(TYPE, modelAndView.getAttributes().get("type"));
        assertEquals(CLIENT_ID, modelAndView.getAttributes().get("clientId"));

        verify(reportService).saveReportChanges(reportEditDto, path);
        verify(reportService).updateStatusOfReportAfterEdit(ID);
    }
}
