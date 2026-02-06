import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import './App.css'
import { Navigation } from './components/Navigation'
import { Home } from './pages/Home'
import { ApplicationManagement } from './components/ApplicationManagement'

function App() {
  return (
    <Router>
      <Navigation />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/applications" element={<ApplicationManagement />} />
      </Routes>
    </Router>
  )
}

export default App
