import { useState } from 'react';
import { schedulerService, type CrlCheckRunResponse, type CertificateReportResponse } from '../services/schedulerService';
import '../styles/Scheduler.css';

const formatTimestamp = (value: string) => {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleString();
};

export function Scheduler() {
  const [running, setRunning] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [reportData, setReportData] = useState<CertificateReportResponse | null>(null);
  const [result, setResult] = useState<CrlCheckRunResponse | null>(null);

  const runCrlCheck = async () => {
    try {
      setRunning(true);
      setError(null);
      const data = await schedulerService.runCrlCheck();
      setResult(data);
    } catch (err) {
      console.error(err);
      setError('Failed to run CRL check. Please try again.');
    } finally {
      setRunning(false);
    }
  };

  const generateReport = async () => {
    try {
      setGenerating(true);
      setError(null);
      setReportData(null);
      const data = await schedulerService.generateReport();
      setReportData(data);
    } catch (err) {
      console.error(err);
      setError('Failed to generate report. Please try again.');
    } finally {
      setGenerating(false);
    }
  };

  return (
    <div className="scheduler-page">
      <header className="scheduler-header">
        <h1>Scheduler Management</h1>
        <p>Manage and trigger manual executions of scheduled tasks.</p>
      </header>

      <div className="scheduler-tasks-grid">
        {/* Card 1: Daily Report */}
        <section className="task-card">
          <div className="task-icon report-icon">📊</div>
          <div className="task-content">
            <h3>Certificate Status Report</h3>
            <p>
              Generates an Excel report including certificates expired today, 
              revoked today, and those expiring in the next {30} days. 
              Sent to the configured report recipient.
            </p>
            <button
              className="scheduler-run-btn"
              onClick={generateReport}
              disabled={generating || running}
            >
              {generating ? 'Generating...' : 'Generate & Send Report'}
            </button>
          </div>
        </section>

        {/* Card 2: CRL Check */}
        <section className="task-card">
          <div className="task-icon crl-icon">🛡️</div>
          <div className="task-content">
            <h3>CRL Revocation Check</h3>
            <p>
              Scans all ACTIVE certificates against their respective Certificate 
              Revocation Lists (CRLs) to ensure their validity status is up to date.
            </p>
            <button
              className="scheduler-run-btn secondary"
              onClick={runCrlCheck}
              disabled={running || generating}
            >
              {running ? 'Running...' : 'Run CRL Check Now'}
            </button>
          </div>
        </section>
      </div>


      {error && <div className="alert alert-error">{error}</div>}
      {reportData && <div className="alert alert-success">{reportData.message}</div>}

      {reportData && reportData.items.length > 0 && (
        <section className="scheduler-report-results">
          <h3>Report Items</h3>
          <div className="report-table-wrapper">
            <table className="report-table">
              <thead>
                <tr>
                  <th>Category</th>
                  <th>Serial Number</th>
                  <th>Client Name</th>
                  <th>Application</th>
                  <th>Email</th>
                  <th>Phone</th>
                  <th>Expiry</th>
                  <th>Revoked</th>
                </tr>
              </thead>
              <tbody>
                {reportData.items.map((item, idx) => (
                  <tr key={idx}>
                    <td><span className={`badge badge-${item.category.toLowerCase().replace(' ', '-')}`}>{item.category}</span></td>
                    <td>{item.serialNumber}</td>
                    <td>{item.clientName || '-'}</td>
                    <td>{item.applicationName || '-'}</td>
                    <td>{item.clientEmail || '-'}</td>
                    <td>{item.clientPhone || '-'}</td>
                    <td>{item.expiryDate ? formatTimestamp(item.expiryDate) : '-'}</td>
                    <td>{item.revokedOn ? formatTimestamp(item.revokedOn) : '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}


      {result && (
        <section className="scheduler-results">
          <div className="results-header">
            <h3>Last Run Summary</h3>
            <span className="results-range">
              {formatTimestamp(result.startedAt)} to {formatTimestamp(result.finishedAt)}
            </span>
          </div>
          <div className="results-grid">
            <div className="result-card">
              <span className="result-value">{result.totalActive}</span>
              <span className="result-label">Active Certificates</span>
            </div>
            <div className="result-card">
              <span className="result-value">{result.processed}</span>
              <span className="result-label">Checked</span>
            </div>
            <div className="result-card">
              <span className="result-value">{result.revoked}</span>
              <span className="result-label">Revoked</span>
            </div>
            <div className="result-card">
              <span className="result-value">{result.skipped}</span>
              <span className="result-label">Skipped</span>
            </div>
            <div className="result-card">
              <span className="result-value">{result.failed}</span>
              <span className="result-label">Failed</span>
            </div>
          </div>

          {result.logs && result.logs.length > 0 && (
            <div className="crl-logs-section">
              <h4>Execution Logs</h4>
              <div className="crl-logs-list">
                {result.logs.map((log, idx) => (
                  <div key={idx} className={`crl-log-item status-${log.status.toLowerCase()}`}>
                    <span className="log-serial">{log.serialNumber}</span>
                    <span className={`log-status badge badge-${log.status.toLowerCase()}`}>{log.status}</span>
                    <span className="log-message">{log.message}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </section>
      )}
    </div>
  );
}
