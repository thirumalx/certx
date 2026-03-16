import { useState } from 'react';
import { schedulerService, type CrlCheckRunResponse } from '../services/schedulerService';
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
  const [error, setError] = useState<string | null>(null);
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

  return (
    <div className="scheduler-page">
      <header className="scheduler-header">
        <div>
          <h1>CRL Check Scheduler</h1>
          <p>Run an on-demand revocation check for all active certificates.</p>
        </div>
        <button
          className="scheduler-run-btn"
          onClick={runCrlCheck}
          disabled={running}
          aria-busy={running}
        >
          {running ? 'Running...' : 'Run CRL Check'}
        </button>
      </header>

      <section className="scheduler-info-card">
        <h3>What this does</h3>
        <p>
          The manual run executes the same logic as the 6-hour schedule. It checks each
          ACTIVE certificate against its CRL distribution points and marks any revoked
          certificates as REVOKED.
        </p>
      </section>

      {error && <div className="alert alert-error">{error}</div>}

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
        </section>
      )}
    </div>
  );
}
