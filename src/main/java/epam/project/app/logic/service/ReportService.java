package epam.project.app.logic.service;

import epam.project.app.infra.web.exception.AppException;
import epam.project.app.logic.entity.report.Report;
import epam.project.app.logic.entity.dto.ReportCreateDto;
import epam.project.app.logic.entity.dto.ReportUpdateDto;
import epam.project.app.logic.entity.user.Filter;
import epam.project.app.logic.repository.ReportRepository;
import liquibase.pro.packaged.S;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;

    public Report insertReport(ReportCreateDto dto) {
        return reportRepository.insertReport(dto).orElseThrow(() -> new AppException("cannot create new Report"));
    }

    public Report updateStatusOfReport(ReportUpdateDto dto) {
        return reportRepository.updateStatusOfReport(dto).orElseThrow(() -> new AppException("cannot update report"));
    }

    public List<Report> getAllReportsByClientId(Long clientId) {
        List<Report> reports = reportRepository.getAllReportsByClientId(clientId);
        if (reports.isEmpty()) {
            throw new AppException("cannot found reports");
        }
        return reports;
    }

    public Report deleteReportById(Long id) {
        return reportRepository.deleteReportById(id).orElseThrow(() -> new AppException("cannot delete report"));
    }

    public Report uploadReport(String fileName, String path, Long clientId, String type) {
        return insertReport(new ReportCreateDto(fileName, path, clientId, type));
    }

    public List<Report> getReportsByFilterParameters(Map<String, String> parameters) {
        return reportRepository.getReportsByParameter(parameters);
    }
}