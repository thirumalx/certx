import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import './App.css'
import { Navigation } from './components/Navigation'
import { Home } from './pages/Home'
import { Scheduler } from './pages/Scheduler'
import { ApplicationManagement } from './components/ApplicationManagement'
import { ClientManagement } from './components/ClientManagement'
import { CertificateManagement } from './components/CertificateManagement'
import { ClientAssignees } from './components/ClientAssignees'

function App() {
  return (
    <Router>
      <Navigation />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/scheduler" element={<Scheduler />} />
        <Route path="/applications" element={<ApplicationManagement />} />
        <Route path="/applications/:applicationId/clients" element={<ClientManagement />} />
        <Route path="/applications/:applicationId/clients/:clientId/certificates" element={<CertificateManagement />} />
        <Route path="/applications/:applicationId/clients/:clientId/assignees" element={<ClientAssignees />} />
      </Routes>
    </Router>
  )
}

export default App
