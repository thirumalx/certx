import { Link, useLocation } from 'react-router-dom';
import '../styles/Navigation.css';

export function Navigation() {
  const location = useLocation();

  return (
    <nav className="navbar">
      <div className="nav-container">
        <Link to="/" className="nav-logo">
          CertX Management
        </Link>
        <ul className="nav-menu">
          <li className="nav-item">
            <Link
              to="/"
              className={`nav-link ${location.pathname === '/' ? 'active' : ''}`}
            >
              Home
            </Link>
          </li>
          <li className="nav-item">
            <Link
              to="/applications"
              className={`nav-link ${location.pathname === '/applications' ? 'active' : ''}`}
            >
              Applications
            </Link>
          </li>
        </ul>
      </div>
    </nav>
  );
}
