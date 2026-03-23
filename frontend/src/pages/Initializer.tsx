import { useState } from 'react';
import { API_BASE } from '../config';
import '../styles/Initializer.css';

/**
 * Initializer page for certificate system setup.
 * 
 * @author Thirumal M
 */
export function Initializer() {
  const [path, setPath] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<{ text: string; type: 'success' | 'error' } | null>(null);

  const handleInitialize = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!path.trim()) {
      setMessage({ text: 'Please enter a valid directory path.', type: 'error' });
      return;
    }

    setLoading(true);
    setMessage(null);

    try {
      const response = await fetch(`${API_BASE}/initializer/certificates?path=${encodeURIComponent(path)}`, {
        method: 'GET',
      });

      if (!response.ok) {
        throw new Error('Failed to initialize certificates from path.');
      }

      const data = await response.json();
      setMessage({ text: `Successfully initialized ${data.length} certificates.`, type: 'success' });
    } catch (err) {
      console.error(err);
      setMessage({ text: 'Error initializing certificates. Please check the path and try again.', type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="initializer-page">
      <header className="initializer-header">
        <h1>System Initialization</h1>
        <p>Import certificates from a local directory to populate the registry.</p>
      </header>

      <div className="initializer-card">
        <form onSubmit={handleInitialize}>
          <div className="form-group">
            <label htmlFor="certificatePath">Certificate Directory Path</label>
            <input
              id="certificatePath"
              type="text"
              value={path}
              onChange={(e) => setPath(e.target.value)}
              placeholder="e.g., /home/thirumal/certs"
              disabled={loading}
              autoFocus
            />
            <small className="help-text">
              Enter the absolute path to the directory containing certificate files (.cer, .pfx, etc.).
            </small>
          </div>

          <button
            type="submit"
            className="initialize-btn"
            disabled={loading || !path.trim()}
          >
            {loading ? 'Initializing...' : 'Initialize System'}
          </button>
        </form>

        {message && (
          <div className={`alert alert-${message.type}`}>
            {message.text}
          </div>
        )}
      </div>

      <section className="initializer-info">
        <h3>What this does</h3>
        <ul>
          <li>Scans the specified directory for valid certificate files.</li>
          <li>Parses certificate metadata (Subject, Issuer, Expiry, etc.).</li>
          <li>Registers certificates in the database if they don't already exist.</li>
          <li>Links certificates to their respective owners if possible.</li>
        </ul>
      </section>
    </div>
  );
}
