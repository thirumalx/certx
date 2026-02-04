import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'

function Home() {
  return <h1>Home Page</h1>
}

function Apps() {
  return <h1>Applications Page</h1>
}

function App() {
  return (
    <Router>
      <nav style={{ padding: '20px', backgroundColor: '#333', color: 'white' }}>
        <a href="/" style={{ marginRight: '20px', color: 'white' }}>Home</a>
        <a href="/applications" style={{ color: 'white' }}>Applications</a>
      </nav>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/applications" element={<Apps />} />
      </Routes>
    </Router>
  )
}

export default App
