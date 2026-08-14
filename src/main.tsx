import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.tsx'
import './styles/global.css'

const rootElement = document.getElementById('root')

if (!rootElement) {
  throw new Error('React could not find the root element in index.html.')
}

// createRoot establishes the boundary where React manages the application's UI.
// StrictMode adds development-only checks that help reveal unsafe React patterns.
createRoot(rootElement).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
