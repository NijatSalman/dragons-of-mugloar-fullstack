import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { CssBaseline, ThemeProvider } from '@mui/material'
import '@fontsource/cinzel/700.css'
import '@fontsource/crimson-pro/400.css'
import '@fontsource/crimson-pro/600.css'
import { App } from './App.tsx'
import { GameProvider } from './state/GameProvider.tsx'
import { theme } from './theme.ts'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <GameProvider>
        <App />
      </GameProvider>
    </ThemeProvider>
  </StrictMode>,
)
