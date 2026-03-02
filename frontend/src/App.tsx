import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import './App.css'
import { Navigation } from './components/Navigation'
import { Home } from './pages/Home'
import { ApplicationManagement } from './components/ApplicationManagement'
import { ClientManagement } from './components/ClientManagement'
import { CertificateManagement } from './components/CertificateManagement'

function App() {
  return (
    <Router>
      <Navigation />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/applications" element={<ApplicationManagement />} />
        <Route path="/applications/:applicationId/clients" element={<ClientManagement />} />
        <Route path="/applications/:applicationId/clients/:clientId/certificates" element={<CertificateManagement />} />
      </Routes>
    </Router>
  )
}

export default App
