import '../styles/Home.css';

export function Home() {
  return (
    <div className="home">
      <div className="home-content">
        <h1>Welcome to CertX Management</h1>
        <p>Manage your applications and certificates with ease.</p>
        <div className="features">
          <div className="feature-card">
            <h3>Applications</h3>
            <p>Create, read, update, and delete your applications.</p>
            <a href="/applications" className="cta-button">
              Manage Applications →
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}
