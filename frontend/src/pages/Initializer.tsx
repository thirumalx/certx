import React, { useState, useEffect } from 'react';
import { API_BASE } from '../config';
import './Initializer.css';

/**
 * Initializer component to scan a directory and register certificates.
 * @author Thirumal M
 */
export function Initializer() {
  const [path, setPath] = useState('');
  const [applications, setApplications] = useState<any[]>([]);
  const [selectedApp, setSelectedApp] = useState<string>('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<{ text: string; type: 'success' | 'error' } | null>(null);
  const [results, setResults] = useState<string[]>([]);

  useEffect(() => {
    fetch(`${API_BASE}/application?page=0&size=100&status=ACTIVE`)
      .then(res => res.json())
      .then(data => {
        if (data.content) {
          setApplications(data.content);
          if (data.content.length > 0) {
            setSelectedApp(data.content[0].id.toString());
          }
        }
      })
      .catch(err => console.error('Failed to fetch applications', err));
  }, []);

  const handleInitialize = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!path.trim() || !selectedApp) {
      setMessage({ text: 'Please enter a valid directory path and select an application.', type: 'error' });
      return;
    }

    setLoading(true);
    setMessage(null);
    setResults([]);

    try {
      const response = await fetch(`${API_BASE}/initializer/certificates`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          path,
          applicationId: parseInt(selectedApp)
        }),
      });

      if (!response.ok) {
        throw new Error('Failed to initialize certificates from path.');
      }

      const data = await response.json();
      setResults(data);
      setMessage({ text: `Processing complete. ${data.length} items handled.`, type: 'success' });
    } catch (err) {
      console.error(err);
      setMessage({ text: 'Error initializing certificates. Please check the path and try again.', type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="initializer-container">
      <div className="initializer-card">
        <h1>Initialize to add certificates</h1>
        <p className="subtitle">Scan a directory to register or update certificates. Clients will be created automatically based on filenames.</p>

        <form onSubmit={handleInitialize} className="initializer-form">
          <div className="form-group">
            <label htmlFor="application">Target Application</label>
            <select
              id="application"
              value={selectedApp}
              onChange={(e) => setSelectedApp(e.target.value)}
              disabled={loading}
              required
            >
              <option value="" disabled>Select an Application</option>
              {applications.map((app) => (
                <option key={app.id} value={app.id}>
                  {app.applicationName} ({app.uniqueId})
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="path">Directory Path</label>
            <input
              id="path"
              type="text"
              placeholder="e.g., \home\thirumal\Certificates\"
              value={path}
              onChange={(e) => setPath(e.target.value)}
              disabled={loading}
              required
            />
          </div>

          <button type="submit" className="initialize-btn" disabled={loading || !path.trim()}>
            {loading ? 'Initializing...' : 'Initialize '}
          </button>
        </form>

        {message && (
          <div className={`alert alert-${message.type}`}>
            {message.text}
          </div>
        )}

        {results.length > 0 && (
          <div className="results-section">
            <h3>Processing Results</h3>
            <div className="results-list-container">
              <ul className="results-list">
                {results.map((res, index) => (
                  <li key={index} className={`result-item ${res.startsWith('ERROR') ? 'error' : res.startsWith('UPDATED') ? 'updated' : res.startsWith('CREATED') ? 'created' : 'skip'}`}>
                    {res}
                  </li>
                ))}
              </ul>
            </div>
          </div>
        )}
      </div>

      <section className="initializer-info">
        <h3>How it works</h3>
        <ul>
          <li><strong>Recursive Scan</strong>: Scans the specified folder and all sub-folders.</li>
          <li><strong>Auto-Client</strong>: Filename (e.g., <code>app_server.pfx</code>) becomes Client Name and Unique ID.</li>
          <li><strong>Smart Update</strong>: Checks if certificate already exists at that path and updates it if the file changed.</li>
          <li><strong>Linked Registration</strong>: Automatically links new certificates to the selected application and created clients.</li>
        </ul>
      </section>
    </div>
  );
}
