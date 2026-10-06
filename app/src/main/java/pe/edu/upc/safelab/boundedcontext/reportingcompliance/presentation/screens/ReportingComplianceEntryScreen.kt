package pe.edu.upc.safelab.boundedcontext.reportingcompliance.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import pe.edu.upc.safelab.boundedcontext.reportingcompliance.presentation.components.ReportingSection
import pe.edu.upc.safelab.boundedcontext.reportingcompliance.presentation.components.ReportingSectionNavigation

@Composable
fun ReportingComplianceEntryScreen() {
    var selectedSectionName by rememberSaveable {
        mutableStateOf(ReportingSection.REPORTS.name)
    }

    val selectedSection = ReportingSection.valueOf(selectedSectionName)

    Column(modifier = Modifier.fillMaxSize()) {
        ReportingSectionNavigation(
            selectedSection = selectedSection,
            onSectionSelected = { selectedSectionName = it.name }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedSection) {
                ReportingSection.REPORTS -> ReportsScreen(
                    onGenerateReport = { selectedSectionName = ReportingSection.GENERATE.name },
                    onExportData = { selectedSectionName = ReportingSection.EXPORT.name }
                )
                ReportingSection.GENERATE -> GenerateReportScreen(
                    onViewReports = { selectedSectionName = ReportingSection.REPORTS.name }
                )
                ReportingSection.HISTORY -> HistoricalDataScreen()
                ReportingSection.COMPLIANCE -> ComplianceScreen(
                    onGenerateReport = { selectedSectionName = ReportingSection.GENERATE.name }
                )
                ReportingSection.EXPORT -> ExportDataScreen()
            }
        }
    }
}
